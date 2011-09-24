package com.moupress.app.util;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;

import com.moupress.app.Const;

public final class DbHelper
{
    public static SharedPreferences SP;

    private Context mContext;
    
    public DbHelper(Context context)
    {
        this.mContext = context;
        SP = mContext.getSharedPreferences(Const.sPName, 0);
    }
    
    public void Insert(String key, String value)
    {
       SP.edit().putString(key, value).commit();
    }
    
    public void Insert(String key, int value)
    {
       SP.edit().putInt(key, value).commit();
    }
    public void Insert(String key, long value)
    {
       SP.edit().putLong(key, value).commit();
    }
    public void Insert(String key, Boolean value)
    {
       SP.edit().putBoolean(key, value).commit();
    }
    
    public String GetString(String key)
    {
        return SP.getString(key, Const.DefString);
    }
    
    public int GetInt(String key)
    {
        return SP.getInt(key, Const.DefNum);
    }
    public long GetLong(String key)
    {
        return SP.getLong(key, Const.DefNum);
    }
    public Boolean GetBool(String key)
    {
        return SP.getBoolean(key, Const.DefBool);
    }
    
    public void saveAlarm(Calendar calendar,int alarmPosition)
    {
        
        Insert(Const.ALARM + Integer.toString(alarmPosition),
        		calendar.getTimeInMillis());
        Insert(Const.Hours + Integer.toString(alarmPosition),
        		calendar.get(Calendar.HOUR_OF_DAY));
        Insert(Const.Mins + Integer.toString(alarmPosition),
        		calendar.get(Calendar.MINUTE));

        //Toast.makeText(mContext, "Saved/Updated Alarm: " + alarmPosition, Toast.LENGTH_LONG).show();

    }

	public void SaveAlarmStatus(int alarmPosition, boolean selected) {
		Insert(Const.ISALARMSET + Integer.toString(alarmPosition),selected);
	}

	public void SaveSnooze(int snoozeMode) {
		Insert(Const.SNOOZE + snoozeMode, snoozeMode);
		
	}

}
