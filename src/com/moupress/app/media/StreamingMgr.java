package com.moupress.app.media;

import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

import com.moupress.app.Const;
import com.spoledge.aacplayer.AACPlayer;


public class StreamingMgr {
	
	private StreamingMediaPlayer audioStreamer;
	private AACPlayer aacPlayer;
	private MediaPlayer defaultAlarmPlayer;
	private StreamingNotifier notifier;
	
	private Context context;
	
	public boolean bIsPlaying;
	
	public StreamingMgr(Context context) {
		this.context = context;
		//nc = new NetworkConnection(Const.BBC_WORLD_SERVICE,context);
	}

	public void startStreaming(String mediaURL, int mediaLengthInKb, int mediaLengthInSeconds) {
		try {
			if (mediaURL.equals(Const.DEFAULT_RIGNTONE)) {
				playDefaultAlarmSound();
			} else if (mediaURL.startsWith("mms://")) {
	    		aacPlayer = new AACPlayer();
	    		aacPlayer.playAsync(mediaURL );
			} else {
				audioStreamer = new StreamingMediaPlayer(context);
				audioStreamer.startStreaming(mediaURL, mediaLengthInKb, mediaLengthInSeconds);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void interrupt() {
		if (audioStreamer != null) audioStreamer.interrupt();
		if (aacPlayer != null) aacPlayer.stop();
		if (defaultAlarmPlayer != null) defaultAlarmPlayer.stop();
	}
	
	private void playDefaultAlarmSound() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); 
		if(alert == null){
	        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	        if(alert == null){
	            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
	        }
	    }
		defaultAlarmPlayer = new MediaPlayer();
		try {
			defaultAlarmPlayer.setDataSource(context, alert);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			defaultAlarmPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			defaultAlarmPlayer.setLooping(true);
			try {
				defaultAlarmPlayer.prepare();
			} catch (IOException e) {
				e.printStackTrace();
			}
			defaultAlarmPlayer.start();
		}
	}
    
}
