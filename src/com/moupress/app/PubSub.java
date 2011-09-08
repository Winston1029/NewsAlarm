package com.moupress.app;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.moupress.app.TTS.AlarmTTSMgr;
import com.moupress.app.alarm.AlarmManagerMgr;
import com.moupress.app.media.StreamingMediaPlayer;
import com.moupress.app.media.StreamingMgr;
import com.moupress.app.media.StreamingNotifier;
import com.moupress.app.snoozer.SnoonzeMgr;
import com.moupress.app.snoozer.SnoozeListener;
import com.moupress.app.ui.UIMgr;
import com.moupress.app.weather.WeatherMgr;

public class PubSub {
	
	private Activity activity;
	private Context context;
	
	private SnoonzeMgr snoozeMgr;
	private SnoozeListener snoozeListener;
	
	private StreamingMgr streamingMgr;
	private StreamingMediaPlayer audioStreamer;
	private StreamingNotifier notifier;
	
	private WeatherMgr weatherMgr;
	
	private AlarmManagerMgr alarmMgr;
	private AlarmTTSMgr alarmTTSMgr;
	
	private UIMgr uiMgr;
		
	// Create Handler to call View updates on the main UI thread.
	private final Handler handler = new Handler();
	
	public PubSub(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
		initUI();
		
		//initSnooze();
		initMedia();
		initWeather();
		initAlarmMgr();
		initAlarmTTSMgr();
		
	}
	
	private void initUI() {
		uiMgr = new UIMgr(activity);
	}
	
	private void initSnooze() {
		snoozeListener = new SnoozeListener() {
			@Override
			public void onSnoozed() {
				//System.out.println("Snoozed");
				Toast.makeText(context, "Snooze Detected", Toast.LENGTH_SHORT).show();
			}
		};
		snoozeMgr = new SnoonzeMgr(context, snoozeListener);
		snoozeMgr.setGestureOverlayView(uiMgr.gesturesView);
		//snoozeMgr.registerListener(snoozeType, listener);
		//snoozeMgr.unRegisterListener(snoozeType);
	}
	
	private void initMedia() {
		if (Const.ISDEBUG) {
			streamingMgr = new StreamingMgr(context, uiMgr.txv_stream, uiMgr.btn_play, uiMgr.btn_stream, uiMgr.progressBar);
		} else {
			streamingMgr = new StreamingMgr(context);
		}
		//streamingMgr.getMediaPlayer().startStreaming("http://www.bbc.co.uk/iplayer/console/bbc_world_service",5208, 216);
	}
	
	private void initWeather() {
		if (Const.ISDEBUG) {
			weatherMgr = new WeatherMgr(context, uiMgr.refreshButton, uiMgr.txv_wind, uiMgr.txv_humidity, uiMgr.txv_updatetime, uiMgr.txv_location);
		} else {
			weatherMgr = new WeatherMgr(context);
		}
		weatherMgr.getWeather();
	}
	
	private void initAlarmMgr() {
		if (Const.ISDEBUG) {
			alarmMgr = new AlarmManagerMgr(this.activity, uiMgr.btn_set, uiMgr.btn_cel, uiMgr.tv);
		} else {
			alarmMgr = new AlarmManagerMgr(this.activity);
		}
		//alarmMgr.setAlarm(hourOfDay, minute, second, millisecond);
		//alarmMgr.startAlarm();
		//alarmMgr.cancelAlarm();
	}
	
	private void initAlarmTTSMgr() {
		if (Const.ISDEBUG) {
			alarmTTSMgr = new AlarmTTSMgr(context, uiMgr.btnPlay, uiMgr.btnPause, uiMgr.btnShutdown);
		} else {
			alarmTTSMgr = new AlarmTTSMgr(context);
		}
		//alarmTTSMgr.getTalker().AddMsgToSpeak("Text to speach start");
		//alarmTTSMgr.getTalker().PlayOrResumeSpeak();
	}
	
}
