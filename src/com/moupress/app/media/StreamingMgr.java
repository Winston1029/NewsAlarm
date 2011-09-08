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
	
	public StreamingMgr(Context  context,final TextView textStreamed, final ImageButton	playButton, Button	streamButton,final ProgressBar	progressBar) 
 	{
		this.textStreamed = textStreamed;
		this.playButton = playButton;
		this.streamingButton = streamButton;
		this.progressBar = progressBar;
		
		streamingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					//startStreaming("http://dl.dropbox.com/u/5758134/sara.mp3",5208, 216);
					startStreaming("http://www.bbc.co.uk/iplayer/console/bbc_world_service",5208, 216);
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
		
		notifier = new StreamingNotifier() {

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
			
		};
		audioStreamer = new StreamingMediaPlayer(context, notifier);
	}
	
	public StreamingMgr(Context context) {
		notifier = new StreamingNotifier() {

			@Override
			public void updatedStream() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void finishedStream() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void playStream() {
				// TODO Auto-generated method stub
				
			}
			
		};
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
    	if (Const.ISDEBUG) {
	    	float progress = (((float)audioStreamer.getCurrentPosition()/1000)/audioStreamer.mediaLengthInSeconds);
	    	if (progressBar != null)
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
    }
}
