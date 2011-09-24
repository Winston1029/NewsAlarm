package com.moupress.app.media;

import java.io.IOException;

import android.content.Context;
import com.spoledge.aacplayer.AACPlayer;


public class StreamingMgr {
	
	private StreamingMediaPlayer audioStreamer;
	private AACPlayer aacPlayer;
	private StreamingNotifier notifier;
	
	private Context context;
	
	public boolean bIsPlaying;
	
	public StreamingMgr(Context context) {
		this.context = context;
		//nc = new NetworkConnection(Const.BBC_WORLD_SERVICE,context);
	}

	public void startStreaming(String mediaURL, int mediaLengthInKb, int mediaLengthInSeconds) {
		try {
			if (mediaURL.startsWith("mms://")) {
	    		aacPlayer = new AACPlayer();
	    		aacPlayer.playAsync(mediaURL );
			}
			else {
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
	}
    
}
