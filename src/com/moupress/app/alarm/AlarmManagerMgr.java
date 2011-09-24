package com.moupress.app.alarm;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.moupress.app.Const;
import com.moupress.app.NewsAlarmActivity;
import com.moupress.app.util.DbHelper;

public class AlarmManagerMgr
{
    private Context mContext;
    private Calendar mCalendar = null;
    private Calendar[] lstCalendars = new Calendar[3];
    private AlarmManager alarmMgr = null;
    public static final String AlarmNumber = "AlarmNumber";


    public AlarmManagerMgr(Context context, Calendar[] calendar)
    {
        this.mContext = context;
        mCalendar = Calendar.getInstance();
        this.lstCalendars = calendar;
    }
    

    public void setAlarm(int alarmPosition, Boolean selected, int hourOfDay,
            int minute, int second, int millisecond)
    {
        if (alarmPosition < 0 || alarmPosition > 2)
        {
            //Toast.makeText(mContext, "The alarm is not set!", Toast.LENGTH_LONG).show();
            return;
        }

        mCalendar.setTimeInMillis(System.currentTimeMillis());
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, second);
        mCalendar.set(Calendar.MILLISECOND, millisecond);
        lstCalendars[alarmPosition] = mCalendar;

        // Check if the alarm is started. If true, send an updated pendingintent
        // No need to stop it first because the pendingintent will override.
        if (selected)
        {
            startAlarm(alarmPosition);
            return;
        }
    }

    public boolean startAlarm(int alarmPosition)
    {
        try
        {
            alarmMgr = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);

            Intent intent = new Intent(mContext, AlarmReceiver.class);
            Bundle bundle = new Bundle();
//            bundle.putString(AlarmManagerMgr.AlarmType,
//                    AlarmManagerMgr.Alarm.ToDo.toString());
//            bundle.putString(AlarmManagerMgr.SnoozeType,
//                    AlarmManagerMgr.Soonze.Shake.toString());
            bundle.putInt(AlarmManagerMgr.AlarmNumber, alarmPosition);
            intent.putExtras(bundle);
            PendingIntent pi = PendingIntent.getBroadcast(mContext,
                    alarmPosition, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            
            // to avoid trigger immediately
            long alarmTime = lstCalendars[alarmPosition].getTimeInMillis();
            //if (alarmTime < System.currentTimeMillis())
            if(lstCalendars[alarmPosition].before(Calendar.getInstance()))
            {
                while (alarmTime < System.currentTimeMillis())
                {
                    alarmTime += AlarmManager.INTERVAL_DAY;
                }
                //alarmTime += AlarmManager.INTERVAL_DAY;
                lstCalendars[alarmPosition].setTimeInMillis(alarmTime);
                
            }
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append(lstCalendars[alarmPosition].getTime());
            sBuilder.append("\n");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            sBuilder.append(calendar.getTime());
            //Toast.makeText(mContext, sBuilder.toString(), Toast.LENGTH_LONG).show();
            // setup alarm && repeater
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            // alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
            // mCalendar.getTimeInMillis(), (10*1000), pi);

           // saveAlarmStatus(alarmPosition, true);
        }
        catch (Exception e)
        {
            return false;
        }

        return true;

    }

    public void setActivityParams(Bundle extras)
    {
        // String data = extras.getString(AlarmManagerMgr.AlarmType)
        // +" : "
        // + extras.getString(AlarmManagerMgr.SnoozeType);
        String data = "Alarm : "
                + Integer.toString(extras.getInt(AlarmManagerMgr.AlarmNumber))
                + " is alarming.";
        //Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(mContext, NewsAlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(extras);
        
        // mContext.startActivity(intent);

    }

    private boolean isActivityStarted()
    {
        // to check if activity exists
        Intent intent = new Intent();
        intent.setClassName("com.moupress.app", "NewsAlarmActivity");
        if (mContext.getPackageManager().resolveActivity(intent, 0) == null)
        {
            return false;
        }
        return true;

    }

    public boolean cancelAlarm(int alarmPosition)
    {
       // Toast.makeText(mContext, "Alarm £º" + Integer.toString(alarmPosition)
         //       + " is cancelled", Toast.LENGTH_LONG);
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, alarmPosition,
                intent, 0);
        try
        {
            alarmMgr = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);
            alarmMgr.cancel(pi);
            //saveAlarmStatus(alarmPosition, false);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;

    }

    

	public Calendar getCalendarByPosition(int alarmPosition) {
		return this.lstCalendars[alarmPosition];
	}
}
