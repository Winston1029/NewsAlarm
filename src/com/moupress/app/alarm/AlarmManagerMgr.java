package com.moupress.app.alarm;

import java.util.Calendar;

import com.moupress.app.NewsAlarmActivity;
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

public class AlarmManagerMgr
{
    private Context mContext;
    private Calendar mCalendar = null;
    private AlarmManager alarmMgr = null;
    public static final String AlarmType = "AlarmType";
    public static final String SnoozeType = "SnoozeType";
    public static enum Alarm{ News, ToDo, Music, Etc };
    public static enum Soonze{ Gesture, Move, Shake, Etc };
    
    private Button btn_set;
    private Button btn_cel;
    private TextView tv;
    
    public AlarmManagerMgr(Context context, Button btn_set, Button btn_cel, final TextView tv)
    {
        this.mContext = context;
        this.btn_set = btn_set;
        this.btn_cel = btn_cel;
        this.tv = tv;
        
        btn_set.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View v) {
            	Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                new TimePickerDialog(mContext,new TimePickerDialog.OnTimeSetListener(){
                  
                    public void onTimeSet(TimePicker view, int hourOfDay,
                            int minute) {
                        setAlarm(hourOfDay,minute,0,0);
                        if(startAlarm())
                        {
                            tv.setText("Hour "+hourOfDay+":"+minute);                            
                        }else
                        {
                            tv.setText("Error Happens! ");                            
                        }
                    }
                    
                },hour,minute,true).show();
                
            }
            
        });
        btn_cel.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View v) {
                if(cancelAlarm())
                {
                    tv.setText("Alarm is Cancelled Successfully!");
                }
                else
                {
                    tv.setText("Alarm is Cancelled Unsuccessfully!");
                }
                
            }
            
        });
    }
    
    public AlarmManagerMgr(Context context) {
    	this.mContext = context;
    }
    
    public void setAlarm(int hourOfDay, int minute, int second, int millisecond)
    {
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, second);
        mCalendar.set(Calendar.MILLISECOND, millisecond);
    }
    
    public boolean startAlarm()
    { 
        try {
            alarmMgr = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);
            
            Intent intent = new Intent(mContext,AlarmReceiver.class);
            Bundle bundle = new Bundle();
            bundle.putString(AlarmManagerMgr.AlarmType, AlarmManagerMgr.Alarm.ToDo.toString());
            bundle.putString(AlarmManagerMgr.SnoozeType, AlarmManagerMgr.Soonze.Shake.toString());
            intent.putExtras(bundle);
            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
           
            //setup alarm && repeater
            alarmMgr.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pi);
            //alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), (10*1000), pi);
        }
        catch(Exception e)
        {
            return false;
        }
        
        return true; 
        
    }

    public void setActivityParams(Bundle extras)
    {
        String data = extras.getString(AlarmManagerMgr.AlarmType)
                        +" : "
                        + extras.getString(AlarmManagerMgr.SnoozeType);
       // Toast.makeText(mContext, data, Toast.LENGTH_LONG).show();
        
        Intent intent = new Intent(mContext, NewsAlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(extras);
        mContext.startActivity(intent);
        
    }
    
    private boolean isActivityStarted()
    {
        //to check if activity exists
        Intent intent = new Intent();  
        intent.setClassName("com.moupress.app", "NewsAlarmActivity");        
        if(mContext.getPackageManager().resolveActivity(intent, 0) == null) {  
            return false;
        }
        return true;
        
    }

    public boolean cancelAlarm()
    {
        Intent intent = new Intent(mContext,AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        try{
            alarmMgr = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);
            alarmMgr.cancel(pi);
        }catch(Exception e)
        {
            return false;
        }
        return true;
        
    }
    
}
