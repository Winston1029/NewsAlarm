package com.moupress.app.alarm;

import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.MonthDisplayHelper;
import android.widget.Toast;

import com.moupress.app.NewsAlarmActivity;

public class AlarmManagerMgr
{
    private Context mContext;
    private Calendar mCalendar = null;
    private Calendar[] lstCalendars = new Calendar[3];
    private boolean[][] SelectedDay = new boolean[3][7];
    private AlarmManager alarmMgr = null;
    public static final String AlarmNumber = "AlarmNumber";


    public AlarmManagerMgr(Context context, Calendar[] calendar, boolean[][] selectedDay)
    {
        this.mContext = context;
        mCalendar = Calendar.getInstance();
        this.lstCalendars = calendar;
        this.SelectedDay = selectedDay;
    }


    public void setAlarm(int alarmPosition, Boolean selected, int hourOfDay,
            int minute, int second, int millisecond, boolean[] daySelected)
    {
        if (alarmPosition < 0 || alarmPosition > 2)
        {
            //Toast.makeText(mContext, "The alarm is not set!", Toast.LENGTH_LONG).show();
            return;
        }
        this.SelectedDay[alarmPosition] = daySelected;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        mCalendar.set(Calendar.MILLISECOND, millisecond);
        lstCalendars[alarmPosition] = calendar;
        
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
            //long alarmTime = lstCalendars[alarmPosition].getTimeInMillis();
            //if (alarmTime < System.currentTimeMillis())
//            if(lstCalendars[alarmPosition].before(Calendar.getInstance()))
//            {
//                while (alarmTime < System.currentTimeMillis())
//                {
//                    alarmTime += AlarmManager.INTERVAL_DAY;
//                }
//                //alarmTime += AlarmManager.INTERVAL_DAY;
//                lstCalendars[alarmPosition].setTimeInMillis(alarmTime);
//                
//            }
            long alarmTime = getNearestAlarmTime(alarmPosition);
            if(alarmTime < 0)
            {
                Toast.makeText(mContext,"Alarm was not set" , Toast.LENGTH_LONG).show();
            }
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
           // Toast.makeText(mContext,"Alarm is set successfully." , Toast.LENGTH_LONG).show();
            StringBuilder sBuilder = new StringBuilder();
//            sBuilder.append(lstCalendars[alarmPosition].getTime());
//            sBuilder.append("\n");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(alarmTime);
            sBuilder.append(calendar.getTime());
            Toast.makeText(mContext, sBuilder.toString(), Toast.LENGTH_LONG).show();
            // setup alarm && repeater
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

    private long getNearestAlarmTime(int alarmPosition)
    {
        int index=0;
        Calendar calendar = Calendar.getInstance();
        calendar = lstCalendars[alarmPosition];
        //get return a strange value: Friday is 6. Hence minus 1.
        int test = calendar.get(Calendar.DAY_OF_WEEK)-1;
        for (int i = 0; i < this.SelectedDay[alarmPosition].length; i++)
        {
            index = (i+test)%7;
            if(SelectedDay[alarmPosition][index])
            {
                if(calendar.getTimeInMillis() > System.currentTimeMillis())
                {
                    //lstCalendars[alarmPosition] = mCalendar;
                    //startAlarm(alarmPosition);
                    return calendar.getTimeInMillis();
                }
            }
            //Every round, need add 1 day to calendar 
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }   
        //if come to this point. The only possibility is that the alarm time is set to today with time less than now
        if(SelectedDay[alarmPosition][test])
        {
            return calendar.getTimeInMillis();
        }
        return -1;
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
