package com.moupress.app.alarm;

import com.moupress.app.NewsAlarmActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	System.out.println("On Received is Triggered !! ");
        Bundle extras = intent.getExtras();
        
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
}
