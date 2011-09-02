package com.moupress.app.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver
{
	public AlarmReceiver()
	{
		System.out.println("Alarm Receiver is created! ");
	}
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	System.out.println("On Received is Triggered !! ");
        Bundle extras = intent.getExtras();
        if(extras != null)
        {
            AlarmManagerMgr AMController = new AlarmManagerMgr(context);
            AMController.setActivityParams(extras);
            return;
        }
        else
        {
            //Create the default settings for alarm
        }
    }
}
