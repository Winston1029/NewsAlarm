package com.moupress.app.util;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

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

        Toast.makeText(mContext, "Saved/Updated Alarm: " + alarmPosition+"\n"+ calendar.getTime(), Toast.LENGTH_LONG).show();

    }

	public void SaveAlarmStatus(int alarmPosition, boolean selected) {
		Insert(Const.ISALARMSET + Integer.toString(alarmPosition),selected);
	}

	public void SaveSnooze(int snoozeMode) {
		Insert(Const.SNOOZE + snoozeMode, snoozeMode);
	}

    public void saveAlarmSelectedDay(boolean[] daySelected, int alarmPosition)
    {
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < daySelected.length; i++)
        {
            if(daySelected[i])
            {
              sBuilder.append(Const.StrDaySelected);
            }
            else {
              sBuilder.append(Const.StrDayNotSelected);  
            }
            
            if(i < daySelected.length -1)
                sBuilder.append(Const.Limit);
            
        }
        Insert(Const.SelectedDay + Integer.toString(alarmPosition), sBuilder.toString());
        //Toast.makeText(mContext, Const.SelectedDay + Integer.toString(alarmPosition)+" \n"+ sBuilder.toString(),Toast.LENGTH_LONG).show();
    }
    public boolean[] getSelectedDay(int alarmPosition)
    {
        String selectedDay = GetString(Const.SelectedDay+  Integer.toString(alarmPosition));
        if(selectedDay == Const.DefString)
            return Const.DaySelected;
        String[] stringSelDay = selectedDay.split(Const.Limit);
        boolean[] boolSelDay  = new boolean[7];
        for (int i = 0; i < stringSelDay.length; i++)
        {
            if(stringSelDay[i].equalsIgnoreCase(Const.StrDaySelected))
            {
                boolSelDay[i] = true;
            }
            
        }
        return boolSelDay;
    }

}
