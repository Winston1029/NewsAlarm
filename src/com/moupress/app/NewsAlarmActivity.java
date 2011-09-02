package com.moupress.app;

import java.util.Calendar;

import com.moupress.app.alarm.AlarmManagerMgr;
import com.moupress.app.alarm.AlarmReceiver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;


public class NewsAlarmActivity extends Activity {
    /** Called when the activity is first created. */
	private PubSub pubsub;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        pubsub = new PubSub(getBaseContext(), this);
    }
    
    public void testBtnOnClick(View v)
    {
    	System.out.println("Test Button Clicked!");
    	//Intent intent = new Intent(this,AlarmReceiver.class);
    }
   
}