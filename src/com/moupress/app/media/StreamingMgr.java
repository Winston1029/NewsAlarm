package com.moupress.app.media;

import java.io.IOException;

import com.moupress.app.R;
import com.moupress.app.media.StreamingNotifier;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;


public class StreamingMgr {
	
	private StreamingMediaPlayer audioStreamer;
	private StreamingNotifier notifier;
	
	private TextView textStreamed;
	private ImageButton playButton;
	private Button streamingButton;
	private ProgressBar	progressBar;
	
	// Create Handler to call View updates on the main UI thread.
	private final Handler handler = new Handler();
	
	//private Context context;
	
	public boolean bIsPlaying;
	
	public StreamingMgr(Context  context,TextView textStreamed, final ImageButton	playButton, Button	streamButton,ProgressBar	progressBar) 
 	{
		notifier = new MyStreamingNotifier();
		audioStreamer = new StreamingMediaPlayer(context, notifier);
		this.textStreamed = textStreamed;
		this.playButton = playButton;
		this.streamingButton = streamButton;
		this.progressBar = progressBar;
		
		streamingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					startStreaming("http://dl.dropbox.com/u/5758134/sara.mp3",5208, 216);
				} catch (IOException e) {
					e.printStackTrace();
				}
				streamingButton.setEnabled(false);
        }});
		
		playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (getMediaPlayer().isPlaying()) {
					getMediaPlayer().pause();
					playButton.setImageResource(R.drawable.button_play);
				} else {
					getMediaPlayer().start();
					startPlayProgressUpdater();
					playButton.setImageResource(R.drawable.button_pause);
				}
				bIsPlaying = !bIsPlaying;
        }});
		
		notifier = new MyStreamingNotifier();
		audioStreamer = new StreamingMediaPlayer(context, notifier);
	}

	public void startStreaming(String string, int mediaLengthInKb, int mediaLengthInSeconds) throws IOException {
		audioStreamer.startStreaming(string, mediaLengthInKb, mediaLengthInSeconds);
	}

	public StreamingMediaPlayer getMediaPlayer() {
		return audioStreamer;
	}

	public void interrupt() {
		playButton.setEnabled(false);
		audioStreamer.interrupt();
	}
    
    public void startPlayProgressUpdater() {
    	float progress = (((float)audioStreamer.getCurrentPosition()/1000)/audioStreamer.mediaLengthInSeconds);
    	progressBar.setProgress((int)(progress*100));
    	
		if (audioStreamer.isPlaying()) {
			Runnable notification = new Runnable() {
		        public void run() {
		        	startPlayProgressUpdater();
				}
		    };
		    handler.postDelayed(notification,1000);
    	}
    }
    
    public class MyStreamingNotifier implements StreamingNotifier {
    	public MyStreamingNotifier() {
    		
    	}

		@Override
		public void updatedStream() {
			Runnable updater = new Runnable() {
		        public void run() {
		        	textStreamed.setText((audioStreamer.totalKbRead + " Kb read"));
		    		int iLoadedPer = (int)audioStreamer.getStreamingPer();
		    		progressBar.setSecondaryProgress(iLoadedPer);
		        }
		    };
		    handler.post(updater);			
		}

		@Override
		public void finishedStream() {
			Runnable updater = new Runnable() { 
				public void run() {
		        	textStreamed.setText(("Audio full loaded: " + audioStreamer.totalKbRead + " Kb read"));
		        }
		    };
		    handler.post(updater);			
		}

		@Override
		public void playStream() {
			Runnable updater = new Runnable() {
		        public void run() {
					if (audioStreamer.getMediaPlayer() != null) {
						startPlayProgressUpdater();        	
						playButton.setEnabled(true);
					}
		        }
			};
			handler.post(updater);
	    	
		}
    }

}
