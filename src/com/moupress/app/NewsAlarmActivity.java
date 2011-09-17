package com.moupress.app;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.moupress.app.alarm.AlarmManagerMgr;


public class NewsAlarmActivity extends Activity {
    
	private PubSub pubsub;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent mainIntent = this.getIntent();
        Bundle extras = mainIntent.getExtras();
        //check if the activity is launch by user or broadcast receiver
        if(extras != null)
        {
            //Logic to handle the event of alarming
            //get which alarm is alarming. 
            //Via alarm number, get the snoozer type/alarm type, etc
            //and set the UI correspondingly
            int alarmNo = extras.getInt(AlarmManagerMgr.AlarmNumber);
            
            Toast.makeText(this, "Alarm : " + alarmNo, Toast.LENGTH_LONG).show();

        }
        else {
            //Logic to handle the event of launching by user
            if (Const.ISDEBUG) {
                setContentView(R.layout.main);
            }  else {
                setContentView(R.layout.news_alarm_ui);
            }
           
            pubsub = new PubSub(getBaseContext(), this);
        }
        
       
    }
    
}