package com.moupress.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


public class NewsAlarmActivity extends Activity {
    
	private PubSub pubsub;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent mainIntent = this.getIntent();
        Bundle extras = mainIntent.getExtras();
        //check if the activity is launch by user or broadcast receiver

            //Logic to handle the event of launching by user
            if (Const.ISDEBUG) {
                setContentView(R.layout.main);
            }  else {
                setContentView(R.layout.news_alarm_ui);
            }
           
            pubsub = new PubSub(getBaseContext(), this);
       
    }
    
//    @Override
    public void onBackPressed() {
    	// Warning message when Back Button is Pressed
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Are you sure you want to exit?")
    	       .setCancelable(false)
    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   pubsub.exit();
    	        	   NewsAlarmActivity.this.finish();
    	           }
    	       })
    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	builder.create().show();
	    return;
    }

    
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onStop();
		System.out.println("On Pause");
	}
	
	
	

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		
		 Bundle extras = intent.getExtras();
		 if(extras != null)
		 {
			 pubsub.onSnoozed();
		 }
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("On Resume");
	}
}