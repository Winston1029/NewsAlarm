package com.moupress.app.media;

import java.io.IOException;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.moupress.app.Const;
import com.moupress.app.R;
import com.moupress.app.util.NetworkConnection;
import com.spoledge.aacplayer.ArrayAACPlayer;
import com.spoledge.aacplayer.ArrayDecoder;
import com.spoledge.aacplayer.Decoder;


public class StreamingMgr {
	
	private StreamingMediaPlayer audioStreamer;
	private ArrayAACPlayer aacPlayer;
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
	    		aacPlayer = new ArrayAACPlayer( ArrayDecoder.create( Decoder.DECODER_FFMPEG_WMA  ));
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

	public StreamingMediaPlayer getMediaPlayer() {
		return audioStreamer;
	}

	public void interrupt() {
		audioStreamer.interrupt();
	}
    
}
