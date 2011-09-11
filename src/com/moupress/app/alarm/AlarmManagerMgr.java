package com.moupress.app.alarm;

import java.util.Calendar;

import android.R.integer;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.moupress.app.NewsAlarmActivity;
import com.moupress.app.util.DbHelper;

public class AlarmManagerMgr
{
    private Context mContext;
    private Calendar mCalendar = null;
    private Calendar[] lstCalendars = new Calendar[3];
    private AlarmManager alarmMgr = null;
    private DbHelper helper = null;
    public static final String AlarmType = "AlarmType";
    public static final String SnoozeType = "SnoozeType";
    public static final String AlarmNumber = "AlarmNumber";

    public static enum Alarm
    {
        News, ToDo, Music, Etc
    };

    public static enum Soonze
    {
        Gesture, Move, Shake, Etc
    };

    private Button btn_set;
    private Button btn_cel;
    private TextView tv;
    private int alarmNo = 1; // test

    public AlarmManagerMgr(Context context, Button btn_set, Button btn_cel,
            final TextView tv)
    {
        this.mContext = context;
        this.btn_set = btn_set;
        this.btn_cel = btn_cel;
        this.tv = tv;
        helper = new DbHelper(this.mContext);
        mCalendar = Calendar.getInstance();

        btn_set.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v)
            {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                new TimePickerDialog(mContext,
                                     new TimePickerDialog.OnTimeSetListener() {

                                         public void onTimeSet(TimePicker view,
                                                 int hourOfDay, int minute)
                                         {
                                             setAlarm(alarmNo, false,
                                                     hourOfDay, minute, 0, 0);
                                             if (startAlarm(alarmNo))
                                             {
                                                 tv.setText("Hour " + hourOfDay
                                                         + ":" + minute);
                                                 alarmNo++;
                                             }
                                             else
                                             {
                                                 tv.setText("Error Happens! ");
                                             }
                                         }

                                     }, hour, minute, true).show();

            }

        });
        btn_cel.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v)
            {
                if (cancelAlarm(alarmNo - 1))
                {
                    tv.setText("Alarm " + Integer.toString(alarmNo - 1)
                            + " is Cancelled Successfully!");
                }
                else
                {
                    tv.setText("Alarm is Cancelled Unsuccessfully!");
                }

            }

        });
    }

    public AlarmManagerMgr(Context context)
    {
        this.mContext = context;
        helper = new DbHelper(this.mContext);
        mCalendar = Calendar.getInstance();
        initAlarms();
    }

    private void initAlarms()
    {
        // store into database
        // alarm Time
        int hours, mins;
        for (int i = 0; i < lstCalendars.length; i++)
        {
            long alarmTime = helper.GetLong(DbHelper.ALARM
                    + Integer.toString(i));

            if (alarmTime != DbHelper.DefNum)
            {
                mCalendar.setTimeInMillis(alarmTime);
                hours = mCalendar.get(Calendar.HOUR_OF_DAY);
                mins = mCalendar.get(Calendar.MINUTE);
               
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                mCalendar.set(Calendar.HOUR_OF_DAY, hours);
                mCalendar.set(Calendar.MINUTE, mins);
                mCalendar.set(Calendar.SECOND, 0);
                mCalendar.set(Calendar.MILLISECOND, 0);
                lstCalendars[i] = mCalendar;
            }
            else
            {
                mCalendar.setTimeInMillis(System.currentTimeMillis()
                        + AlarmManager.INTERVAL_FIFTEEN_MINUTES);
                lstCalendars[i] = mCalendar;
            }
        }
    }

    public void setAlarm(int alarmPosition, Boolean selected, int hourOfDay,
            int minute, int second, int millisecond)
    {
        if (alarmPosition < 0 || alarmPosition > 2)
        {
            Toast.makeText(mContext, "The alarm is not set!", Toast.LENGTH_LONG).show();
            return;
        }

        mCalendar.setTimeInMillis(System.currentTimeMillis());
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, second);
        mCalendar.set(Calendar.MILLISECOND, millisecond);
        lstCalendars[alarmPosition] = mCalendar;

//        Toast.makeText(
//                mContext,
//                "" + lstCalendars[alarmPosition].get(Calendar.HOUR_OF_DAY)
//                        + ":"
//                        + lstCalendars[alarmPosition].get(Calendar.MINUTE),
//                Toast.LENGTH_LONG).show();
        // Check if the alarm is started. If true, send an updated pendingintent
        // No need to stop it first because the pendingintent will override.
        if (selected)
        {
            startAlarm(alarmPosition);
        }

        saveAlarm(alarmPosition);
    }

    public boolean startAlarm(int alarmPosition)
    {
        try
        {
            alarmMgr = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);

            Intent intent = new Intent(mContext, AlarmReceiver.class);
            Bundle bundle = new Bundle();
            bundle.putString(AlarmManagerMgr.AlarmType,
                    AlarmManagerMgr.Alarm.ToDo.toString());
            bundle.putString(AlarmManagerMgr.SnoozeType,
                    AlarmManagerMgr.Soonze.Shake.toString());
            bundle.putInt(AlarmManagerMgr.AlarmNumber, alarmPosition);
            intent.putExtras(bundle);
            PendingIntent pi = PendingIntent.getBroadcast(mContext,
                    alarmPosition, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // to avoid trigger immediately
            long alarmTime = lstCalendars[alarmPosition].getTimeInMillis();
            if (alarmTime < System.currentTimeMillis())
            {
                while (alarmTime < System.currentTimeMillis())
                {
                    alarmTime += AlarmManager.INTERVAL_DAY;
                }
                lstCalendars[alarmPosition].setTimeInMillis(alarmTime);
                saveAlarm(alarmPosition);
            }
            // setup alarm && repeater
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
            // alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
            // mCalendar.getTimeInMillis(), (10*1000), pi);

            saveAlarmStatus(alarmPosition, true);
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
        Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();

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
        Toast.makeText(mContext, "Alarm £º" + Integer.toString(alarmPosition)
                + " is cancelled", Toast.LENGTH_LONG);
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, alarmPosition,
                intent, 0);
        try
        {
            alarmMgr = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);
            alarmMgr.cancel(pi);
            saveAlarmStatus(alarmPosition, false);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;

    }

    private void saveAlarm(int alarmPosition)
    {
        helper.Insert(DbHelper.ALARM + Integer.toString(alarmPosition),
                this.lstCalendars[alarmPosition].getTimeInMillis());
    }

    private void saveAlarmStatus(int alarmPosition, boolean blnSet)
    {
        helper.Insert(DbHelper.ISALARMSET + Integer.toString(alarmPosition),
                blnSet);
    }

}
