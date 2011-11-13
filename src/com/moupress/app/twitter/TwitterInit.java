package com.moupress.app.twitter;

import java.net.URI;
import java.util.Date;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import com.moupress.app.Const;
import com.moupress.app.NewsAlarmActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;



public class TwitterInit {
	private SharedPreferences prefs;
	private Activity activity;
	private Context context;
    private OAuthConsumer consumer; 
    private OAuthProvider provider;
    
	private final Handler mTwitterHandler = new Handler();
	
	 final Runnable mUpdateTwitterNotification; 
	
	public TwitterInit(Activity activity,final Context context)
	{
		this.activity = activity;
		this.context = context;
		mUpdateTwitterNotification = new Runnable() {
	        public void run() {
	        	Toast.makeText(context, "Tweet sent !", Toast.LENGTH_LONG).show();
	        }
	    };
	    this.prefs = PreferenceManager.getDefaultSharedPreferences(activity);
	}
	
	public void checkAuthentation()
	{
		if (TwitterUtils.isAuthenticated(prefs)) {
    		sendTweet();
    	} else {
//			Intent i = new Intent(context, PrepareRequestTokenActivity.class);
//			i.putExtra("tweet_msg",getTweetMsg());
//			activity.startActivity(i);
    		try {
        		this.consumer = new CommonsHttpOAuthConsumer(Const.CONSUMER_KEY, Const.CONSUMER_SECRET);
        	    this.provider = new CommonsHttpOAuthProvider(Const.REQUEST_URL,Const.ACCESS_URL,Const.AUTHORIZE_URL);
        	} catch (Exception e) {
        		//Log.e(TAG, "Error creating consumer / provider",e);
        		System.out.println("Erro creating consumer / provider" + e);
    		}
        	
        	new OAuthRequestTokenTask(activity,consumer,provider).execute();
    	}
	}
	
	private String getTweetMsg() {
		return "Find really cool android apps! Check it out https://market.android.com/details?id=com.moupress.app&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5tb3VwcmVzcy5hcHAiXQ..";
	}	
	
	public void sendTweet() {
		Thread t = new Thread() {
	        public void run() {
	        	
	        	try {
	        		TwitterUtils.sendTweet(prefs,getTweetMsg());
	        		mTwitterHandler.post(mUpdateTwitterNotification);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
	        }
	    };
	    t.start();
	}

	public void retrieveToken(Uri uri) {
		// TODO Auto-generated method stub
		new RetrieveAccessTokenTask(activity,consumer,provider,prefs).execute(uri);
		
	}
	
	public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void> {

		private Context	context;
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		private SharedPreferences prefs;
		
		public RetrieveAccessTokenTask(Context context, OAuthConsumer consumer,OAuthProvider provider, SharedPreferences prefs) {
			this.context = context;
			this.consumer = consumer;
			this.provider = provider;
			this.prefs=prefs;
		}


		/**
		 * Retrieve the oauth_verifier, and store the oauth and oauth_token_secret 
		 * for future API calls.
		 */
		@Override
		protected Void doInBackground(Uri...params) {
			final Uri uri = params[0];
			final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

			try {
				provider.retrieveAccessToken(consumer, oauth_verifier);

				final Editor edit = prefs.edit();
				edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
				edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
				edit.commit();
				
				String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
				String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
				
				consumer.setTokenWithSecret(token, secret);
				context.startActivity(new Intent(context,NewsAlarmActivity.class));

				executeAfterAccessTokenRetrieval();
				
				//Log.i(TAG, "OAuth - Access Token Retrieved");
				
			} catch (Exception e) {
				//Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
			}

			return null;
		}


		private void executeAfterAccessTokenRetrieval() {
			String msg = getTweetMsg();
			try {
				TwitterUtils.sendTweet(prefs, msg);
			} catch (Exception e) {
				//Log.e(TAG, "OAuth - Error sending to Twitter", e);
				System.out.println("OAuth - Error sending to Twitte"+ e);
			}
		}
	}	

}
