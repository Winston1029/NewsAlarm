package com.moupress.app.util.facebook;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class FacebookUtil {

	private Activity activity;
	private Context context;
	
	public static final String APP_ID = "257802944268897";
    private static final String[] PERMISSIONS = new String[] {"publish_stream"};
	private Facebook facebook;
	private String mfbToken;
	
	
	public FacebookUtil(Activity activity, Context context) {
		this.activity = activity;
		this.context = context;
		facebook = new Facebook(APP_ID);
	}
	
	public void PostMessage(String fbMessage) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
	    mfbToken = prefs.getString("fbToken", "");
	
	    if(mfbToken.equals("")){
	        fbAuthAndPost(fbMessage);
	    }else{
	        updateStatus(mfbToken, fbMessage);
	    }
	}
	
	private void fbAuthAndPost(final String message){
		
	    facebook.authorize(activity, PERMISSIONS, new DialogListener() {
	
	        @Override
	        public void onComplete(Bundle values) {
	            Log.d(this.getClass().getName(),"Facebook.authorize Complete: ");
	            saveFBToken(facebook.getAccessToken(), facebook.getAccessExpires());
	            updateStatus(values.getString(Facebook.TOKEN), message);
	            Toast.makeText(context, "Updated status on Facebook", Toast.LENGTH_SHORT);
	        }
	
	        @Override
	        public void onFacebookError(FacebookError error) {
	            Log.d(this.getClass().getName(),"Facebook.authorize Error: "+error.toString());
	        }
	
	        @Override
	        public void onError(DialogError e) {
	            Log.d(this.getClass().getName(),"Facebook.authorize DialogError: "+e.toString());
	        }
	
	        @Override
	        public void onCancel() {
	            Log.d(this.getClass().getName(),"Facebook authorization canceled");
	        }
	    });
	}
	
	//updating Status
	private void updateStatus(String accessToken, String message){
	    try {
	        Bundle bundle = new Bundle();
	        bundle.putString("message",  message);
	        bundle.putString(Facebook.TOKEN,accessToken);
	        String response = facebook.request("me/feed",bundle,"POST");
	        Log.d("UPDATE RESPONSE",""+response);
	    } catch (MalformedURLException e) {
	        Log.e("MALFORMED URL",""+e.getMessage());
	    } catch (IOException e) {
	        Log.e("IOEX",""+e.getMessage());
	    }
	}
	
	private void saveFBToken(String token, long tokenExpires){
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
	    prefs.edit().putString("fbToken", token).commit();
	}
	
	public void onComplete (int requestCode, int resultCode, Intent data) {
		facebook.authorizeCallback(requestCode, resultCode, data);
    }
}
