package com.moupress.app;

import com.moupress.app.alarm.AlarmManagerMgr;
import com.moupress.app.util.FlurryUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


public class NewsAlarmActivity extends Activity {
    
	private PubSub pubsub;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent mainIntent = this.getIntent();
        Bundle extras = mainIntent.getExtras();
        
         Window win = this.getWindow();
	     win.addFlags( WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	     //win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	     win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	     //win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        
        setContentView(R.layout.news_alarm_ui);
        pubsub = new PubSub(getBaseContext(), this);
        
      //check if the activity is launch by user or broadcast receiver
        if(extras != null)
        {
        	int alarmPosition = extras.getInt(AlarmManagerMgr.AlarmNumber);
            Toast.makeText(this, "Alarm : " + alarmPosition, Toast.LENGTH_LONG).show();
            pubsub.onSnoozePub();
            pubsub.afterSnooze(alarmPosition);
        } else {
        	this.startService(new Intent(this, NewsAlarmService.class));
        }
    }
    
    @Override
    public void onBackPressed() {
    	pubsub.showExitDialog();
	    return;
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		 Bundle extras = intent.getExtras();
		 final Uri uri = intent.getData();
		 if(extras != null)
		 {
		     int alarmPosition = extras.getInt(AlarmManagerMgr.AlarmNumber);
			 pubsub.onSnoozePub();
			 
			 if(alarmPosition != Const.SNOOZE_ALARM) pubsub.afterSnooze(alarmPosition);
		 }
		 else if (uri != null && uri.getScheme().equals(Const.OAUTH_CALLBACK_SCHEME)) {
				
				//System.out.println("Callback received : " + uri);
				//System.out.println( "Retrieving Access Token");
				
				//new RetrieveAccessTokenTask(this,consumer,provider,prefs).execute(uri);
				pubsub.retrieveTwitterToken(uri);
				//finish();	
			}
		 
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		pubsub.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryUtil.onStart(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryUtil.onStop(this);
	}
}