/*
** AACPlayer - Freeware Advanced Audio (AAC) Player for Android
** Copyright (C) 2011 Spolecne s.r.o., http://www.spoledge.com
**  
** This program is free software; you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation; either version 3 of the License, or
** (at your option) any later version.
** 
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
** 
** You should have received a copy of the GNU General Public License
** along with this program. If not, see <http://www.gnu.org/licenses/>.
**/
package com.spoledge.aacplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import android.util.Log;


/**
 * This is the parent of PCM Feeders.
 */
public class PCMFeed implements Runnable {

    private short[] samples;
    private int n;
    
    protected int sampleRate;
    protected int channels;
    protected int bufferSizeInMs;
    protected int bufferSizeInBytes;


    /**
     * The callback - may be null.
     */
    protected PlayerCallback playerCallback;


    /**
     * True iff the AudioTrack is playing.
     */
    protected boolean isPlaying;

    protected boolean stopped;


    /**
     * The local variable in run() method set by method acquireSamples().
     */
    protected short[] lsamples;


    /**
     * Total samples written to AudioTrack.
     */
    protected int writtenTotal = 0;


    /**
     * Creates a new PCMFeed object.
     * @param sampleRate the sampling rate in Hz (e.g. 44100)
     * @param channels the number of channels - only allowed values are 1 (mono) and 2 (stereo).
     * @param bufferSizeInBytes the size of the audio buffer in bytes
     * @param playerCallback the callback - may be null
     */
    protected PCMFeed( int sampleRate, int channels, int bufferSizeInBytes, PlayerCallback playerCallback ) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bufferSizeInBytes = bufferSizeInBytes;
        this.bufferSizeInMs = bytesToMs( bufferSizeInBytes, sampleRate, channels );
        this.playerCallback = playerCallback;
    }

    /**
     * Stops the PCM feeder.
     * This method just asynchronously notifies the execution thread.
     * This can be called in any state.
     */
    public synchronized void stop() {
        stopped = true;
        notify();
    }


    /**
     * Converts milliseconds to bytes of buffer.
     * @param ms the time in milliseconds
     * @return the size of the buffer in bytes
     */
    public static int msToBytes( int ms, int sampleRate, int channels ) {
        return (int)(((long) ms) * sampleRate * channels / 500);
    }


    /**
     * Converts milliseconds to samples of buffer.
     * @param ms the time in milliseconds
     * @return the size of the buffer in samples
     */
    public static int msToSamples( int ms, int sampleRate, int channels ) {
        return (int)(((long) ms) * sampleRate * channels / 1000);
    }


    /**
     * Converts bytes of buffer to milliseconds.
     * @param bytes the size of the buffer in bytes
     * @return the time in milliseconds
     */
    public static int bytesToMs( int bytes, int sampleRate, int channels ) {
        return (int)(500L * bytes / (sampleRate * channels));
    }


    /**
     * Converts samples of buffer to milliseconds.
     * @param samples the size of the buffer in samples (all channels)
     * @return the time in milliseconds
     */
    public static int samplesToMs( int samples, int sampleRate, int channels ) {
        return (int)(1000L * samples / (sampleRate * channels));
    }


    ////////////////////////////////////////////////////////////////////////////
    // OnPlaybackPositionUpdateListener
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Called on the listener to notify it that the previously set marker
     * has been reached by the playback head.
     */
    public void onMarkerReached( AudioTrack track ) {
    }


    /**
     * Called on the listener to periodically notify it that the playback head
     * has reached a multiple of the notification period. 
     */
    public void onPeriodicNotification( AudioTrack track ) {
        if (playerCallback != null) {
            int buffered = writtenTotal - track.getPlaybackHeadPosition()*channels;
            int ms = samplesToMs( buffered, sampleRate, channels );

            playerCallback.playerPCMFeedBuffer( isPlaying, ms, bufferSizeInMs );
        }
    }

    /**
     * This is called by main thread when a new data are available.
     *
     * @param samples the array containing the PCM data
     * @param n the length of the PCM data
     * @return true if ok, false if the execution thread is not responding
     */
    public synchronized boolean feed( short[] samples, int n ) {
        while (this.samples != null && !stopped) {
            try { wait(); } catch (InterruptedException e) {}
        }

        this.samples = samples;
        this.n = n;

        notify();

        return !stopped;
    }

    /**
     * The main execution loop which should be executed in its own thread.
     */
    public void run() {

        AudioTrack atrack = new AudioTrack(
                                AudioManager.STREAM_MUSIC,
                                sampleRate,
                                channels == 1 ? AudioFormat.CHANNEL_CONFIGURATION_MONO :AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                bufferSizeInBytes,
                                AudioTrack.MODE_STREAM );
        //atrack.setPositionNotificationPeriod( msToSamples( 200, sampleRate, channels ));

        isPlaying = false;
        while (!stopped) {
            // fetch the samples into our "local" variable lsamples:
            int ln = acquireSamples();

            if (stopped) {
                releaseSamples();
                break;
            }

            // samples written to AudioTrack in this round:
            int writtenNow = 0;

            do {
                if (writtenNow != 0) {
                    try { Thread.sleep( 50 ); } catch (InterruptedException e) {}
                }

                int written = atrack.write( lsamples, writtenNow, ln );

                if (written < 0) {
                    stopped = true;
                    break;
                }

                writtenTotal += written;
                int buffered = writtenTotal - atrack.getPlaybackHeadPosition()*channels;

                if (!isPlaying) {
                    if (buffered*2 >= bufferSizeInBytes) {
                        atrack.play();
                        isPlaying = true;
                    }
                    else {
                    }
                }

                writtenNow += written;
                ln -= written;
            } while (ln > 0);

            releaseSamples();
        }

        if (isPlaying) atrack.stop();
        atrack.flush();
        atrack.release();

    }



    ////////////////////////////////////////////////////////////////////////////
    // Protected
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Acquires samples into variable lsamples.
     * @return the actual size (in shorts) of the lsamples
     */
    protected synchronized int acquireSamples() {
        while (n == 0 && !stopped) {
            try { wait(); } catch (InterruptedException e) {}
        }

        lsamples = samples;
        int ln = n;

        samples = null;
        n = 0;
        notify();

        return ln;
    }


    /**
     * Releases the lsamples variable.
     * This method is called always after processing the acquired lsamples.
     */
    protected void releaseSamples() {
        // nothing to be done
    }

}
