package com.moupress.app;


import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.moupress.app.TTS.AlarmTTSMgr;
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
	private String mediaURL;

	private WeatherMgr weatherMgr;

	private AlarmManagerMgr alarmMgr;
	private AlarmTTSMgr alarmTTSMgr;

	private UIMgr uiMgr;
	private UIMgrDebug uiMgrDebug;
	
	private DbHelper dbHelper;

	private OnListViewItemChangeListener onListViewItemChangeListener = new OnListViewItemChangeListener() {
		@Override
		public void onSnoozeModeSelected(int snoozeMode, boolean selected) {
			dbHelper.SaveSnooze(snoozeMode,selected);
		}

		@Override
		public void onAlarmTimeChanged(int alarmPosition, boolean selected,
				int hourOfDay, int minute, int second, int millisecond, boolean[] daySelected) {
			System.out.println("Alarm Time is changed!");
			alarmMgr.setAlarm(alarmPosition, selected, hourOfDay, minute,second, millisecond, daySelected);
			Calendar calendar = alarmMgr.getCalendarByPosition(alarmPosition);
			
			dbHelper.saveAlarmSelectedDay(daySelected, alarmPosition);
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
			//System.out.println("Alarm sound selected!");
			dbHelper.SaveAlarmSound(alarmSoundPosition, selected);
		}
	};
	
	public PubSub(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
		
		initUI();
		System.out.println("UI is initialized!");
		initUtil();
		System.out.println("Utilization is initialized!");
		initSnooze();
		System.out.println("Snooze is initialized!");
		initMedia();
		System.out.println("Media is initialized!");
		initWeather();
		System.out.println("Weather is initialized!");
		initAlarmMgr();
		System.out.println("Alarm is initialized!");
		initAlarmTTSMgr();
		System.out.println("AlarmTTS is initialized!");

	}

	

	public PubSub(Context context, Activity activity, boolean snoozed) {
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

	private void registerSnoozeListener()
	{
		boolean[] snoozeSelected = uiMgr.getSnoozeSelected();
		
		for(int i =0;i<snoozeSelected.length;i++)
		{  
			System.out.println("Snooze Select "+snoozeSelected[i]);
			if(snoozeSelected[i]==true)
				snoozeMgr.registerListener(i,snoozeListener);
		}
	}
	public void onSnoozePub() {
		uiMgr.showSnoozeView();
		registerSnoozeListener();
		AudioManager mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		//mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI);
		boolean[] soundToPlay = uiMgr.getSoundSelected();
		if (soundToPlay != null ) {
			if (soundToPlay[Const.ALARMSOUND_BBC]) {
				mediaURL = Const.BBC_WORLD_SERVICE;
			} else if (soundToPlay[Const.ALARMSOUND_MEDIACORP_933]) {
				mediaURL = Const.MEDIACORP_938_MMS;
			} else {
				mediaURL = Const.DEFAULT_RIGNTONE;
			}
			if (soundToPlay[Const.ALARMSOUND_REMINDER]) {
				alarmTTSMgr.ttsPlayOrResume();
				new Thread( new Runnable(){
					public void run() {
			            while (alarmTTSMgr.isPlaying()) {}	//do nothing, keep checking
			            //streamingMgr.startStreaming(mediaURL, 1000, 600);
			        }
				}).start();
			}
			else {
				System.out.println("Streaming is started!");
				streamingMgr.startStreaming(mediaURL, 1000, 600);
			}
		}
	}
	public void afterSnooze(int alarmPosition)
	{
	    alarmMgr.startAlarm(alarmPosition);
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

	private void unregisterSnoozeListener()
	{
		boolean[] snoozeSelected = uiMgr.getSnoozeSelected();
		for(int i=0;i<snoozeSelected.length;i++)
		{
			if(snoozeSelected[i]==true)
			snoozeMgr.unRegisterListener(i);
		}
	}
	private void initSnooze() {
		snoozeListener = new SnoozeListener() {
			@Override
			public void onSnoozed() {
				System.out.println("Snoozed");
				Toast.makeText(context, "Sleep For 5 Mins", Toast.LENGTH_SHORT).show();
				streamingMgr.interrupt();
				unregisterSnoozeListener();
				uiMgr.setbSettingAlarmTimeDisableFlip(false);
				uiMgr.flipperListView(Const.SCREENS.HomeUI.ordinal());
				
			}

			@Override
			public void onDismissed() {
				streamingMgr.interrupt();
				unregisterSnoozeListener();
				activity.finish();
			}

			@Override
			public void onSnoozedAgain() {
				System.out.println("Snoozed Again !");
				onSnoozePub();
			}
		};
		snoozeMgr = new SnoonzeMgr(context, snoozeListener);
		snoozeMgr.setGestureOverlayView(uiMgr.gesturesView);
		snoozeMgr.setDismissSlide(uiMgr.getDismissSlide());
		
		// snoozeMgr.unRegisterListener(snoozeType);
	}

	private void initMedia() {
		mediaURL = Const.DEFAULT_RIGNTONE;
		streamingMgr = new StreamingMgr(context);
	}

	private void initWeather() {
		weatherMgr = new WeatherMgr(context);
		Handler h = new Handler()
		{

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String weatherConditionText = weatherMgr.getCurrentWeather().get(Const.WEATHERINFO_CURRENT);
				uiMgr.updateHomeWeatherText(weatherConditionText);
			}
		};
		weatherMgr.setWeatherUpdateHandler(h);
		new Thread(weatherMgr).start();
	}

	private void initAlarmMgr() {
		Calendar[] calendars = new Calendar[3];
		boolean[][] selectedDay = new boolean[3][7];
		for (int i = 0; i < calendars.length; i++)
        {
			calendars[i] = getAlarm(i);
			selectedDay[i] = dbHelper.getSelectedDay(i);
        }
		alarmMgr = new AlarmManagerMgr(this.activity, calendars, selectedDay);
		
		// alarmMgr.setAlarm(hourOfDay, minute, second, millisecond);
	}
	
       
	private void initAlarmTTSMgr() {
		if (Const.ISDEBUG) {
			alarmTTSMgr = new AlarmTTSMgr(context, uiMgrDebug.btnPlay,
					uiMgrDebug.btnPause, uiMgrDebug.btnShutdown);
		} else {
			alarmTTSMgr = new AlarmTTSMgr(context, this.activity);
		}
		//alarmTTSMgr.ttsPlayOrResume();

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
		alarmTTSMgr.ttsShutDown();
	}

}
