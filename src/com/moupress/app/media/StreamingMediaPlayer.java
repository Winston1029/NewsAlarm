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
import java.util.List;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.moupress.app.Const;
import com.moupress.app.util.NetworkConnection;
import com.spoledge.aacplayer.ArrayAACPlayer;
import com.spoledge.aacplayer.ArrayDecoder;
import com.spoledge.aacplayer.Decoder;
import com.spoledge.aacplayer.PlayerCallback;

/**
 * Streaming Audio from external URL and Play
 */
public class StreamingMediaPlayer {

	//  Track for downloading progress
	public long mediaLengthInKb, mediaLengthInSeconds;
	public int totalKbRead = 0;

	private MediaPlayer 	mediaPlayer;
	private File downloadingMediaFile; 
	private List<String> playlistUrls;
	private boolean isInterrupted;
	private Context context;
	private int counter = 0;
	private NetworkConnection nc;
	
 	public StreamingMediaPlayer(Context  context) {
 		this.context = context;
 		nc = new NetworkConnection(Const.BBC_WORLD_SERVICE,context);
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
	        	if(nc.checkInternetConnection()==true) {
		            try {   
		        		downloadAudioIncrement(mediaUrl);
		            } catch (IOException e) {
		            	Log.e(getClass().getName(), "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl, e);
		            	return;
		            }
	        	}
	            else
	            	return;
	        }   
	    };   
	    
	    new Thread(r).start();
	    Toast.makeText(context, "Streaming Started", Toast.LENGTH_SHORT).show();
    }
    
    /**  
     * Download the url stream to a temporary location and then call the setDataSource  
     * for that local file
     */  
    private void downloadAudioIncrement(String mediaUrl) throws IOException {
    	String playURL = "";
    	InputStream stream = null;
		
    	downloadPlaylist(mediaUrl);
		if (playlistUrls.size() > 0) {
			playURL = playlistUrls.remove(0);
		} else {
			throw new IOException("Empty playlist downloaded");
		}
    
	
    	URLConnection cn = new URL(playURL).openConnection();   
        cn.connect();   
        stream = cn.getInputStream();
    	
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
        
        do {
        	byte buf[] = new byte[16384];
        	int numread = stream.read(buf);   
            if (numread <= 0)   isInterrupted = true;   
            out.write(buf, 0, numread);
    		totalKbRead += numread/1000;
    		
            bufferMedia();
        } while (validateNotInterrupted());
        
    	//Download finish :copy to playing dat file, delete caching dat file
   		stream.close();
		transferBufferToMediaPlayer();
		downloadingMediaFile.delete();
    }
        
    private boolean downloadPlaylist(String url) throws IOException {
    	if (url.indexOf("m3u") > -1 || url.indexOf("pls") > -1) {
    		URLConnection cn = new URL(url).openConnection();
            cn.connect();
            InputStream stream = cn.getInputStream();
            if (stream == null) {
              return false;
            }

            File downloadingMediaFile = new File(context.getCacheDir(), "playlist_data");
            FileOutputStream out = new FileOutputStream(downloadingMediaFile);
            byte buf[] = new byte[16384];
            int bytesRead;
            while ((bytesRead = stream.read(buf)) > 0) {
              out.write(buf, 0, bytesRead);
            }

            stream.close();
            out.close();
            PlaylistParser parser;
            if (url.indexOf("m3u") > -1) {
              parser = new M3uParser(downloadingMediaFile);
            } else if (url.indexOf("pls") > -1) {
              parser = new PlsParser(downloadingMediaFile);
            } else {
              return false;
            }
            playlistUrls = parser.getUrls();
            return true;
    	}
    	else {
    		throw new IOException("URL is not a proper playing list for this app");
    	}
    	
    }

    private boolean validateNotInterrupted() {
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
     * @throws IOException 
     */  
    private void  bufferMedia() throws IOException {
        if (mediaPlayer == null) {
        	//  Only create the MediaPlayer once we have the minimum buffered data
        	if ( totalKbRead >= Const.INTIAL_KB_BUFFER) {
        		try {
        			File playingMediaFile = new File(context.getCacheDir(),"playingMedia" + (counter++) + ".dat");
                	// Seperate downloading file and playing file to prevent deadlock 
                	copyToPlayingFile(downloadingMediaFile,playingMediaFile);
                	
                	mediaPlayer = createMediaPlayer(playingMediaFile);
            		// start playing as we already have enough buffer
        	    	mediaPlayer.start();
        		} catch (Exception e) {
        			Log.e(getClass().getName(), "Error initializing the MediaPlayer.", e);    			
        		}
        	}
        } else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000 ){ 
        	transferBufferToMediaPlayer();
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

    /**
     *  Move the file in oldLocation to newLocation.
     */
	private void copyToPlayingFile(File	oldLocation, File	newLocation) throws IOException {
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
	
	public void interrupt() {isInterrupted = true;}
	
	public MediaPlayer getMediaPlayer() {return mediaPlayer;}

	public boolean isPlaying() {return mediaPlayer.isPlaying();}

	public void pause() {mediaPlayer.pause();}

	public void start() {mediaPlayer.start();}

	public float getCurrentPosition() {	return mediaPlayer.getCurrentPosition();}

	
}
