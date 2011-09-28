package com.moupress.app;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;

import com.moupress.app.TTS.AlarmTTSMgr;
import com.moupress.app.TTS.CalendarTask;
import com.moupress.app.alarm.AlarmManagerMgr;
import com.moupress.app.media.StreamingMgr;
import com.moupress.app.snoozer.SnoonzeMgr;
import com.moupress.app.snoozer.SnoozeListener;
import com.moupress.app.ui.OnListViewItemChangeListener;
import com.moupress.app.ui.UIMgr;
import com.moupress.app.ui.UIMgrDebug;
import com.moupress.app.util.DbHelper;
import com.moupress.app.weather.WeatherMgr;

public class PubSub {
	private Activity activity;
	private Context context;

	private SnoonzeMgr snoozeMgr;
	private SnoozeListener snoozeListener;

	private StreamingMgr streamingMgr;

	private WeatherMgr weatherMgr;

	private AlarmManagerMgr alarmMgr;
	private AlarmTTSMgr alarmTTSMgr;

	private UIMgr uiMgr;
	private UIMgrDebug uiMgrDebug;
	
	private DbHelper dbHelper;

	private OnListViewItemChangeListener onListViewItemChangeListener = new OnListViewItemChangeListener() {
		@Override
		public void onSnoozeModeSelected(int snoozeMode, boolean selected) {
			dbHelper.SaveSnooze(snoozeMode);
		}

		@Override
		public void onAlarmTimeChanged(int alarmPosition, Boolean selected,
				int hourOfDay, int minute, int second, int millisecond) {
			System.out.println("Alarm Time is changed!");
			alarmMgr.setAlarm(alarmPosition, selected, hourOfDay, minute,second, millisecond);
			Calendar calendar = alarmMgr.getCalendarByPosition(alarmPosition);
			dbHelper.saveAlarm(calendar, alarmPosition);
			uiMgr.updateHomeAlarmText();

		}

		@Override
		public void onAlarmTimeSelected(int alarmPosition, boolean selected) {
			System.out.println("Alarm Time is selected/unselected!");
			if (selected)
				alarmMgr.startAlarm(alarmPosition);
			else
				alarmMgr.cancelAlarm(alarmPosition);
			
			dbHelper.SaveAlarmStatus(alarmPosition, selected);
			uiMgr.updateHomeAlarmText();
		}

		@Override
		public void onAlarmSoundSelected(int alarmSoundPosition,boolean selected) {
		}

	};
	
	public PubSub(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
		
		initUI();

		initUtil();
		initSnooze();
		initMedia();
		initWeather();
		initAlarmMgr();
		initAlarmTTSMgr();

	}

	

	public PubSub(Context context, Activity activity, boolean snoozed) {
		this.context = context;
		this.activity = activity;

		initUI();
		initUtil();
		initAlarmTTSMgr();
		initSnooze();
		initMedia();
		initWeather();
		initAlarmMgr();
		//initAlarmTTSMgr();
	}

	public void onSnoozed() {
		uiMgr.showSnoozeView();
		streamingMgr.startStreaming(Const.BBC_WORLD_SERVICE, 1000, 600);
	}

	private void initUtil() {
		this.dbHelper = new DbHelper(this.activity);
		
	}
	
	private void initUI() {

		if (Const.ISDEBUG) {
			uiMgrDebug = new UIMgrDebug(activity);
		} else {
			uiMgr = new UIMgr(activity);
			uiMgr.registerListViewItemChangeListener(this.onListViewItemChangeListener);
		}
	}

	private void initSnooze() {
		snoozeListener = new SnoozeListener() {
			@Override
			public void onSnoozed() {
				System.out.println("Snoozed");
				// Toast.makeText(context, "Snooze Detected",
				// Toast.LENGTH_SHORT).show();
				streamingMgr.interrupt();
				snoozeMgr.unRegisterListener(SnoonzeMgr.GESTURE_SNOOZE_TYPE);
			}
		};
		snoozeMgr = new SnoonzeMgr(context, snoozeListener);
		snoozeMgr.setGestureOverlayView(uiMgr.gesturesView);
		snoozeMgr.registerListener(SnoonzeMgr.GESTURE_SNOOZE_TYPE,
				snoozeListener);
		// snoozeMgr.unRegisterListener(snoozeType);
	}

	private void initMedia() {
		streamingMgr = new StreamingMgr(context);
		//String mediaUrl = Const.BBC_WORLD_SERVICE;
		//String mediaUrl = Const.MEDIACORP_938_MMS;
		//streamingMgr.startStreaming(mediaUrl, 1000, 600);
	}

	private void initWeather() {
		if (Const.ISDEBUG) {
			weatherMgr = new WeatherMgr(context, uiMgrDebug.refreshButton,
					uiMgrDebug.txv_wind, uiMgrDebug.txv_humidity,
					uiMgrDebug.txv_updatetime, uiMgrDebug.txv_location);
		} else {
			weatherMgr = new WeatherMgr(context);
			uiMgr.updateHomeWeatherText(weatherMgr.getWeather());
		}
	}

	private void initAlarmMgr() {
		Calendar[] calendars = new Calendar[3];
		for (int i = 0; i < calendars.length; i++)
        {
			calendars[i] = getAlarm(i);
        }
		alarmMgr = new AlarmManagerMgr(this.activity, calendars);
		
		// alarmMgr.setAlarm(hourOfDay, minute, second, millisecond);
		// alarmMgr.startAlarm();
		// alarmMgr.cancelAlarm();
	}
	
       
	private void initAlarmTTSMgr() {
		if (Const.ISDEBUG) {
			alarmTTSMgr = new AlarmTTSMgr(context, uiMgrDebug.btnPlay,
					uiMgrDebug.btnPause, uiMgrDebug.btnShutdown);
		} else {
			alarmTTSMgr = new AlarmTTSMgr(context);
		}
		// alarmTTSMgr.getTalker().AddMsgToSpeak("Text to speach start");
		// alarmTTSMgr.getTalker().PlayOrResumeSpeak();
	}

	private Calendar getAlarm(int alarmPosition)
    {
        Calendar calendar = Calendar.getInstance();
        int hours = dbHelper.GetInt(Const.Hours
                + Integer.toString(alarmPosition));
        int mins = dbHelper.GetInt(Const.Mins
                + Integer.toString(alarmPosition));
        calendar.setTimeInMillis(System.currentTimeMillis());
        if(hours == Const.DefNum|| mins == Const.DefNum)
        {
             return  Calendar.getInstance();
        }
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, mins);
        return calendar;
    }

	
	public void exit() {
		streamingMgr.interrupt();
	}

}
