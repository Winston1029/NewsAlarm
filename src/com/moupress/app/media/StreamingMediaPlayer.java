package com.moupress.app.media;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

/**
 * Streaming Audio from external URL and Play
 * Only works under Wifi mode now
 */
public class StreamingMediaPlayer {

    private static final int INTIAL_KB_BUFFER =  160*10/8;// larger means longer waiting time at the beginning
	
	//  Track for downloading progress
	public long mediaLengthInKb, mediaLengthInSeconds;
	public int totalKbRead = 0;

	private MediaPlayer 	mediaPlayer;
	private File downloadingMediaFile; 
	private boolean isInterrupted;
	private Context context;
	private StreamingNotifier notifier;
	private int counter = 0;
	
 	public StreamingMediaPlayer(Context  context, StreamingNotifier notifier) {
 		this.context = context;
 		this.notifier = notifier;
	}
	
    /**  
     * Start streaming based on external URL, need to enable INTERNET access in Manifest.xml
     * Use background thread to download data
     * length vars are estimated value now, need to figure a way to improve accuracy
     */  
    public void startStreaming(final String mediaUrl, long	mediaLengthInKb, long	mediaLengthInSeconds) throws IOException {
    	
    	this.mediaLengthInKb = mediaLengthInKb;
    	this.mediaLengthInSeconds = mediaLengthInSeconds;
    	
		Runnable r = new Runnable() {   
	        public void run() {   
	            try {   
	        		downloadAudioIncrement(mediaUrl);
	            } catch (IOException e) {
	            	Log.e(getClass().getName(), "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl, e);
	            	return;
	            }   
	        }   
	    };   
	    new Thread(r).start();
	    Toast.makeText(context, "Streaming Started", Toast.LENGTH_SHORT).show();
    }
    
    /**  
     * Download the url stream to a temporary location and then call the setDataSource  
     * for that local file
     */  
    public void downloadAudioIncrement(String mediaUrl) throws IOException {
    	
    	URLConnection cn = new URL(mediaUrl).openConnection();   
        cn.connect();   
        InputStream stream = cn.getInputStream();
        if (stream == null) {
        	Log.e(getClass().getName(), "Unable to create InputStream for mediaUrl:" + mediaUrl);
        }
        
		downloadingMediaFile = new File(context.getCacheDir(),"downloadMedia.dat");
		
		// Cleanup the previous downloaded file
		// NOTE: downloaded file and playing file are different
		if (downloadingMediaFile.exists()) {
			downloadingMediaFile.delete();
		}

        FileOutputStream out = new FileOutputStream(downloadingMediaFile);   
        byte buf[] = new byte[16384];
        do {
        	int numread = stream.read(buf);   
            if (numread <= 0)   
                break;   
            out.write(buf, 0, numread);
            totalKbRead += numread/1000;
            
            bufferMedia();
           	notifyDataUpdated(); //info UI download in progress
        } while (validateNotInterrupted());
        
   		stream.close();
        
   		if (validateNotInterrupted()) {
	       	notifyDataFinished(); // info UI download finished
        }
    }  

    public boolean validateNotInterrupted() {
		if (isInterrupted) {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
				//mediaPlayer.release();
			}
			return false;
		} else {
			return true;
		}
    }

    /**
     * Create MediaPlayer and start to play when initial buffer is ready
     * Create new thread to play media
     * Notify UI thread to update relevant views when player starts
     */  
    private void  bufferMedia() {
        if (mediaPlayer == null) {
        	//  Only create the MediaPlayer once we have the minimum buffered data
        	if ( totalKbRead >= INTIAL_KB_BUFFER) {
        		try {
            		startMediaPlayer();
        		} catch (Exception e) {
        			Log.e(getClass().getName(), "Error copying buffered conent.", e);    			
        		}
        	}
        } else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000 ){ 
        	//  NOTE:  When < 1s, media player will not play,
        	//  Flush any left over data still buf data to media file
        	transferBufferToMediaPlayer();
        }
	    notifier.playStream();
    }
    
    private void startMediaPlayer() {
        try {   
        	File playingMediaFile = new File(context.getCacheDir(),"playingMedia" + (counter++) + ".dat");
        	
        	// Seperate downloading file and playing file to prevent deadlock 
        	copyToPlayingFile(downloadingMediaFile,playingMediaFile);
    		
        	Log.e(getClass().getName(),"Buffered File path: " + playingMediaFile.getAbsolutePath());
        	Log.e(getClass().getName(),"Buffered File length: " + playingMediaFile.length()+"");
        	
        	mediaPlayer = createMediaPlayer(playingMediaFile);
        	
    		// start playing as we already have enough buffer
	    	mediaPlayer.start();
        } catch (IOException e) {
        	Log.e(getClass().getName(), "Error initializing the MediaPlayer.", e);
        	return;
        }   
    }
    
    private MediaPlayer createMediaPlayer(File mediaFile) throws IOException {
    	MediaPlayer mPlayer = new MediaPlayer();
    		mPlayer.setOnErrorListener( new MediaPlayer.OnErrorListener() {
		        public boolean onError(MediaPlayer mp, int what, int extra) {
		        	Log.e(getClass().getName(), "Error in MediaPlayer: (" + what +") with extra (" +extra +")" );
		    		return false;
		        }
	    });

    	// A good practise to use FileDescripter rather than direct media file
		FileInputStream fis = new FileInputStream(mediaFile);
		mPlayer.setDataSource(fis.getFD());
		mPlayer.prepare();
		return mPlayer;
    }
    
    /**
     * Transfer the last < 1s buffered data to the MediaPlayer.
     */  
    private void transferBufferToMediaPlayer() {
	    try {
	    	// First determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause
	    	boolean wasPlaying = mediaPlayer.isPlaying();
	    	int curPosition = mediaPlayer.getCurrentPosition();
	    	
	    	// Copy the currently downloaded content to a new buffered File.  Store the old File for deleting later. 
	    	File oldBufferedFile = new File(context.getCacheDir(),"playingMedia" + counter + ".dat");
	    	File bufferedFile = new File(context.getCacheDir(),"playingMedia" + (counter++) + ".dat");

	    	// Auto delete on app exit
	    	bufferedFile.deleteOnExit();   
	    	copyToPlayingFile(downloadingMediaFile,bufferedFile);

	    	// Pause the current player now as we are about to create and start a new one.
	    	mediaPlayer.pause();

	    	// Create a new MediaPlayer rather than try to re-prepare the prior one.
        	mediaPlayer = createMediaPlayer(bufferedFile);
    		mediaPlayer.seekTo(curPosition);
    		
    		// Finish playing for the last <1s
    		boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
        	if (wasPlaying || atEndOfFile){
        		mediaPlayer.start();
        	}

	    	// Delete the previously playing buffered File
	    	oldBufferedFile.delete();
	    	
	    }catch (Exception e) {
	    	Log.e(getClass().getName(), "Error updating to newly loaded content.", e);            		
		}
    }
    
    public void notifyDataUpdated() {
    	notifier.updatedStream();
    }
    
    /** 
     * Download finish 
     * Copy to playing dat file 
     * Delete caching dat file
     */
    public void notifyDataFinished() {
    	transferBufferToMediaPlayer();
    	downloadingMediaFile.delete();
    	notifier.finishedStream();
    }
    
    public MediaPlayer getMediaPlayer() {
    	return mediaPlayer;
	}
    
    public float getStreamingPer() {
		return (float)totalKbRead/(float)mediaLengthInKb * 100;
	}
    
    public void interrupt() {
    	isInterrupted = true;
    	validateNotInterrupted();
    }
    
    /**
     *  Move the file in oldLocation to newLocation.
     */
	public void copyToPlayingFile(File	oldLocation, File	newLocation) throws IOException {
		if ( oldLocation.exists( )) {
			BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );
			BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));
            try {
		        byte[]  buff = new byte[8192];
		        int numChars;
		        while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {
		        	writer.write( buff, 0, numChars );
      		    }
            } catch( IOException ex ) {
				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
            } finally {
                try {
                    if ( reader != null ){                    	
                    	writer.close();
                        reader.close();
                    }
                } catch( IOException ex ){
				    Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() ); 
				}
            }
        } else {
			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
        }
	}

	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	public void pause() {
		mediaPlayer.pause();
	}

	public void start() {
		mediaPlayer.start();
	}

	public float getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	
}
