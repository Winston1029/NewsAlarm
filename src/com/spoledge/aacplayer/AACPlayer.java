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

import java.io.InputStream;

/**
 * This is the AACPlayer parent class.
 * It uses Decoder to decode AAC stream into PCM samples.
 * This class is not thread safe.
 */
public class AACPlayer {

    public static final int DEFAULT_EXPECTED_KBITSEC_RATE = 64;
    public static final int DEFAULT_AUDIO_BUFFER_CAPACITY_MS = 1500;
    public static final int DEFAULT_DECODE_BUFFER_CAPACITY_MS = 700;

    ////////////////////////////////////////////////////////////////////////////
    // Attributes
    ////////////////////////////////////////////////////////////////////////////

    protected boolean stopped;

    protected int audioBufferCapacityMs;
    protected int decodeBufferCapacityMs;
    protected PlayerCallback playerCallback;

    // variables used for computing average bitrate
    private int sumKBitSecRate = 0;
    private int countKBitSecRate = 0;
    private int avgKBitSecRate = 0;

    private ArrayDecoder decoder;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////



    /**
     * Creates a new player.
     * @param playerCallback the callback, can be null
     */
    public AACPlayer( ) {
    	this.stopped				= false;
    	this.playerCallback 		= null;
    	this.decoder				= ArrayDecoder.create( ArrayDecoder.DECODER_FFMPEG_WMA  );
    	this.audioBufferCapacityMs 	= DEFAULT_AUDIO_BUFFER_CAPACITY_MS;
    	this.decodeBufferCapacityMs	= DEFAULT_DECODE_BUFFER_CAPACITY_MS;
    }



    /**
     * Plays a stream asynchronously.
     * This method starts a new thread.
     * @param url the URL of the stream or file
     */
    public void playAsync( final String url ) {
    	new Thread(new Runnable() {
            public void run() {
                try {
                	if (url.startsWith( "mms://" )) {
                        play( new MMSInputStream( url ) );
                    }
                }
                catch (Exception e) {
                    if (playerCallback != null) playerCallback.playerException( e );
                }
            }
        }).start();
    }


    /**
     * Plays a stream synchronously.
     * @param is the input stream
     * @param expectedKBitSecRate the expected average bitrate in kbit/sec; -1 means unknown
     */
    public final void play( InputStream is ) throws Exception {
        if (playerCallback != null) playerCallback.playerStarted();

        sumKBitSecRate = 0;
        countKBitSecRate = 0;

        playImpl( is, DEFAULT_EXPECTED_KBITSEC_RATE );
    }


    /**
     * Stops the execution thread.
     */
    public void stop() {
        stopped = true;
    }


    ////////////////////////////////////////////////////////////////////////////
    // Protected
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Plays a stream synchronously.
     * This is the implementation method calle by every play() and playAsync() methods.
     * @param is the input stream
     * @param expectedKBitSecRate the expected average bitrate in kbit/sec
     */
    protected void playImpl( InputStream is, int expectedKBitSecRate ) throws Exception {
        ArrayBufferReader reader = new ArrayBufferReader(
                                        computeInputBufferSize( expectedKBitSecRate, decodeBufferCapacityMs ),
                                        is );
        new Thread( reader ).start();

        PCMFeed pcmfeed = null;
        Thread pcmfeedThread = null;


        try {
        	ArrayDecoder.Info info = decoder.start( reader );


            if (info.getChannels() > 2) {
                throw new RuntimeException("Too many channels detected: " + info.getChannels());
            }

            // 3 buffers for result samples:
            //   - one is used by decoder
            //   - one is used by the PCMFeeder
            //   - one is enqueued / passed to PCMFeeder - non-blocking op
            short[][] decodeBuffers = createDecodeBuffers( 3, info );
            short[] decodeBuffer = decodeBuffers[0]; 
            int decodeBufferIndex = 0;

            pcmfeed = createArrayPCMFeed( info );
            pcmfeedThread = new Thread( pcmfeed );
            pcmfeedThread.start();

            do {
                info = decoder.decode( decodeBuffer, decodeBuffer.length );
                int nsamp = info.getRoundSamples();

                if (nsamp == 0 || stopped) break;
                if (!pcmfeed.feed( decodeBuffer, nsamp ) || stopped) break;

                int kBitSecRate = computeAvgKBitSecRate( info );
                if (Math.abs(expectedKBitSecRate - kBitSecRate) > 1) {
                    reader.setCapacity( computeInputBufferSize( kBitSecRate, decodeBufferCapacityMs ));
                    expectedKBitSecRate = kBitSecRate;
                }

                decodeBuffer = decodeBuffers[ ++decodeBufferIndex % 3 ];
            } while (!stopped);
        }
        finally {
            stopped = true;

            if (pcmfeed != null) pcmfeed.stop();
            decoder.stop();
            reader.stop();

            if (pcmfeedThread != null) pcmfeedThread.join();

            if (playerCallback != null) playerCallback.playerStopped(0);
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    // Private
    ////////////////////////////////////////////////////////////////////////////

    private short[][] createDecodeBuffers( int count, ArrayDecoder.Info info ) {
        int size = PCMFeed.msToSamples( decodeBufferCapacityMs, info.getSampleRate(), info.getChannels());

        short[][] ret = new short[ count ][];

        for (int i=0; i < ret.length; i++) {
            ret[i] = new short[ size ];
        }

        return ret;
    }


    private PCMFeed createArrayPCMFeed( ArrayDecoder.Info info ) {
        int size = PCMFeed.msToBytes( audioBufferCapacityMs, info.getSampleRate(), info.getChannels());

        return new PCMFeed( info.getSampleRate(), info.getChannels(), size, playerCallback );
    }


    protected int computeAvgKBitSecRate( ArrayDecoder.Info info ) {
        // do not change the value after a while - avoid changing of the out buffer:
        if (countKBitSecRate < 64) {
            int kBitSecRate = computeKBitSecRate( info );
            int frames = info.getRoundFrames();

            sumKBitSecRate += kBitSecRate * frames;
            countKBitSecRate += frames;
            avgKBitSecRate = sumKBitSecRate / countKBitSecRate;
        }

        return avgKBitSecRate;
    }


    private static int computeKBitSecRate( ArrayDecoder.Info info ) {
        if (info.getRoundSamples() <= 0) return -1;

        return computeKBitSecRate( info.getRoundBytesConsumed(), info.getRoundSamples(),
                                   info.getSampleRate(), info.getChannels());
    }


    private static int computeKBitSecRate( int bytesconsumed, int samples, int sampleRate, int channels ) {
        long ret = 8L * bytesconsumed * channels * sampleRate / samples;

        return (((int)ret) + 500) / 1000;
    }


    protected static int computeInputBufferSize( int kbitSec, int durationMs ) {
        return kbitSec * durationMs / 8;
    }

}
