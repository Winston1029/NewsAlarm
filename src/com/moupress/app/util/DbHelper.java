package com.moupress.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class DbHelper
{
    
    public static final String sPName = "NewsAlarm";
    
    public static final String  DefString = "";
    public static final Boolean DefBool = false;
    public static final int DefNum = -1;
    
    public static final String ALARM = "Alarm_";
    public static final String Hours = "Alarm_Hours_";
    public static final String Mins = "Alarm_Mins_";
    public static final String ISALARMSET = "IsAlarmSet_";
    public static final String SNOOZE = "Snooze_";
    public static final String GESTURE = "Gesture_";
    
    public static SharedPreferences SP;

    private Context mContext;
    
    public DbHelper(Context context)
    {
        this.mContext = context;
        SP = mContext.getSharedPreferences(sPName, 0);
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
        return SP.getString(key, DefString);
    }
    
    public int GetInt(String key)
    {
        return SP.getInt(key, DefNum);
    }
    public long GetLong(String key)
    {
        return SP.getLong(key, DefNum);
    }
    public Boolean GetBool(String key)
    {
        return SP.getBoolean(key, DefBool);
    }
    
}
