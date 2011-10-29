package com.moupress.app.alarm;

import com.moupress.app.NewsAlarmActivity;
import com.moupress.app.NewsAlarmService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        
        if(action == null)
       {
	        if(extras != null)
	        {
	            //start the main activity
	            Intent alarmIntent = new Intent(context,NewsAlarmActivity.class );
	            Bundle bundleInfo = new Bundle();
	            bundleInfo.putInt(AlarmManagerMgr.AlarmNumber, extras.getInt(AlarmManagerMgr.AlarmNumber));
	            alarmIntent.putExtras(bundleInfo);
	            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            context.startActivity(alarmIntent);
	            return;
	        }
        }
        else
        {
        	if(action.equals(Intent.ACTION_BOOT_COMPLETED))
        	{
        		System.out.println("On Received is Triggered !! action = "+action+" IntentAction "+Intent.ACTION_BOOT_COMPLETED);

	        	Intent alarmIntent = new Intent(context,NewsAlarmService.class);
	        	//System.out.println("Action! "+action);
	        	context.startService(alarmIntent);
        	}
        }
        
    }
}
