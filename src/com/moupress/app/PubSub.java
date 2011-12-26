package com.moupress.app;


import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
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
import com.moupress.app.ui.uiControlInterface.OnExitDialogListener;
import com.moupress.app.util.DbHelper;
import com.moupress.app.util.FlurryUtil;
import com.moupress.app.util.facebook.FacebookUtil;
import com.moupress.app.util.twitter.TwitterInit;
import com.moupress.app.weather.WeatherMgr;

public class PubSub {
	private Activity activity;
	private Context context;
	private Service service;

	private SnoonzeMgr snoozeMgr;
	private SnoozeListener snoozeListener;

	private StreamingMgr streamingMgr;
	private String mediaURL;

	private WeatherMgr weatherMgr;

	private AlarmManagerMgr alarmMgr;
	private AlarmTTSMgr alarmTTSMgr;

	private UIMgr uiMgr;
	
	private DbHelper dbHelper;
	
	private TwitterInit twitter;
	private FacebookUtil facebook;

	private OnListViewItemChangeListener onListViewItemChangeListener = new OnListViewItemChangeListener() {
		@Override
		public void onSnoozeModeSelected(int snoozeMode, boolean selected) {
			dbHelper.SaveSnooze(snoozeMode,selected);
		}

		@Override
		public void onAlarmTimeChanged(int alarmPosition, boolean selected,
				int hourOfDay, int minute, int second, int millisecond, boolean[] daySelected) {
			System.out.println("Alarm Time is changed! " + hourOfDay);
			alarmMgr.setAlarm(alarmPosition, selected, hourOfDay, minute,second, millisecond, daySelected);
			Calendar calendar = alarmMgr.getCalendarByPosition(alarmPosition);
			
			//System.out.println("Hours of the day Before Save" + calendar.get(Calendar.HOUR_OF_DAY));
			dbHelper.saveAlarmSelectedDay(daySelected, alarmPosition);
			dbHelper.saveAlarm(calendar, alarmPosition);
			//System.out.println("Hours of the day After Save"+ dbHelper.GetInt(Const.Hours + Integer.toString(alarmPosition)));
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
		initUtil(activity);
		initSnooze();
		initMedia();
		initWeather();
		initAlarmMgr(activity);
		initAlarmTTSMgr();
		initSharing();
	}

	private void initSharing() {
		twitter = new TwitterInit(activity, context);
		facebook = new FacebookUtil(activity, context);
	}

	public PubSub(Context context, Service service)
	{
		this.context = context;
		this.service = service;
		initUtil(service);
		initAlarmMgr(service);
	}

//	public PubSub(Context context, Activity activity, boolean snoozed) {
//		this.context = context;
//		this.activity = activity;
//
//		initUI();
//		initUtil(activity);
//		initSnooze();
//		initMedia();
//		initWeather();
//		initAlarmMgr(this.activity);
//		initAlarmTTSMgr();
//	}

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
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI);
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
			        }
				}).start();
			}
			else {
				//System.out.println("Streaming is started!");
				streamingMgr.startStreaming(mediaURL, 1000, 600);
			}
		}
	}
	public void afterSnooze(int alarmPosition)
	{
		boolean isAlarmSet = dbHelper.GetBool(Const.ISALARMSET + alarmPosition , false);
		if (isAlarmSet) {
			FlurryUtil.logEvent("Pubsub_afterSnooze", "AlarmPos", alarmPosition + "");
		    alarmMgr.startAlarm(alarmPosition);
		}
		
	}
	private void initUtil(Context ctx) {
		this.dbHelper = new DbHelper(ctx);
	}
	
	private void initUI() {
		uiMgr = new UIMgr(activity,context);
		uiMgr.registerListViewItemChangeListener(this.onListViewItemChangeListener);
		uiMgr.registerExitDialogFinishListener(this.onExitDialogListener);
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
				alarmMgr.snoozeAlarm(Const.SNOOZE_DUR);
			}

			@Override
			public void onDismissed() {
				streamingMgr.interrupt();
				unregisterSnoozeListener();
				activity.finish();
			}

		};
		snoozeMgr = new SnoonzeMgr(context, snoozeListener);
		snoozeMgr.setGestureOverlayView(uiMgr.gesturesView);
		snoozeMgr.setDismissSlide(uiMgr.getDismissSlide());
		
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
				super.handleMessage(msg);
				String weatherConditionText = weatherMgr.getCurrentWeather().get(Const.WEATHERINFO_CURRENT);
				uiMgr.updateHomeWeatherText(weatherConditionText);
			}
		};
		weatherMgr.setWeatherUpdateHandler(h);
		new Thread(weatherMgr).start();
	}

	private void initAlarmMgr(Context ctx) {
		Calendar[] calendars = new Calendar[3];
		boolean[][] selectedDay = new boolean[3][7];
		boolean[] alarmSelected = new boolean[3];
		for (int i = 0; i < calendars.length; i++)
        {
			calendars[i] = getAlarm(i);
			selectedDay[i] = dbHelper.getSelectedDay(i);
			alarmSelected[i] = dbHelper.GetBool(Const.ISALARMSET + Integer.toString(i), false);
        }
		alarmMgr = new AlarmManagerMgr(ctx, calendars, selectedDay);
		alarmMgr.loadAlarms(alarmSelected);
		// alarmMgr.setAlarm(hourOfDay, minute, second, millisecond);
	}
	
       
	private void initAlarmTTSMgr() {
		alarmTTSMgr = new AlarmTTSMgr(context, this.activity);
	}

	private Calendar getAlarm(int alarmPosition)
    {
        Calendar calendar = Calendar.getInstance();
        int hours = dbHelper.GetInt(Const.Hours
                + Integer.toString(alarmPosition),Const.defAlarmDisplayHours[alarmPosition]);
        int mins = dbHelper.GetInt(Const.Mins
                + Integer.toString(alarmPosition), Const.defAlarmDisplayMins[alarmPosition]);
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
	
	
//====================Exit Dialog =====================
	private OnExitDialogListener onExitDialogListener = new OnExitDialogListener()
	{

		@Override
		public void onExitDialogFinish() {
			exit();
			activity.finish();
		}

		@Override
		public void onFacebookSelected() {
			System.out.println("Facebook is selected !");
			
			uiMgr.showSharedMsgDialogBox(Const.FACEBOOK_TITLE, Const.FACEBOOK_BUTTON, Const.SHARED_METHODS.Facebook);
		}

		@Override
		public void onTwitterSelected() {
			System.out.println("Twitter is selected !");
			//twitter.checkAuthentation();
			uiMgr.showSharedMsgDialogBox(Const.TWITTER_TITLE, Const.TWITTER_BUTTON,Const.SHARED_METHODS.Twitter);
		}

		@Override
		public void onSharedMsgSend(Const.SHARED_METHODS method,String msg) {
			
			if(method == Const.SHARED_METHODS.Twitter)
			{
				twitter.setMessage(msg);
				twitter.checkAuthentation();
			}
			else if(method == Const.SHARED_METHODS.Facebook)
			{
				facebook.PostMessage(msg);
			}
		}
	};
	public void showExitDialog()
	{
		uiMgr.showExitDialog();
	}

	public void retrieveTwitterToken(Uri uri) {
		// TODO Auto-generated method stub
		twitter.retrieveToken(uri);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		facebook.onComplete(requestCode, resultCode, data);
	}

}
