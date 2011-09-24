package com.moupress.app.ui;

import android.app.Activity;
import android.gesture.GestureOverlayView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.moupress.app.Const;
import com.moupress.app.R;

public class UIMgrDebug {
	Activity activity;
	
	public UIMgrDebug(Activity activity) {
		this.activity = activity;
		if (Const.ISDEBUG) {
			initStreamingControls();
			initSoonzeControls();
			initAlarmControls();
			initAlarmTTSControls();
			initWeatherControls();
		}
	}
	
	public Button btn_stream;
	public ImageButton btn_play;
	public TextView txv_stream;
	public boolean bIsPlaying;
	public ProgressBar progressBar;

	// public StreammingMgr uiStreamer;

	private void initStreamingControls() {
		txv_stream = (TextView) activity.findViewById(R.id.text_streamed);
		btn_stream = (Button) activity.findViewById(R.id.button_stream);

		btn_play = (ImageButton) activity.findViewById(R.id.button_play);
		btn_play.setEnabled(false);
		progressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
	}

	public GestureOverlayView gesturesView;

	private void initSoonzeControls() {
		gesturesView = (GestureOverlayView) activity
				.findViewById(R.id.gestures);
	}

	public Button btn_set = null;
	public Button btn_cel = null;
	public TextView tv = null;

	private void initAlarmControls() {
		btn_set = (Button) activity.findViewById(R.id.Button01);
		btn_cel = (Button) activity.findViewById(R.id.Button02);
		tv = (TextView) activity.findViewById(R.id.TextView);
	}

	public Button btnPlay;
	public Button btnPause;
	public Button btnShutdown;

	private void initAlarmTTSControls() {
		btnPlay = (Button) activity.findViewById(R.id.btnStart);
		btnPause = (Button) activity.findViewById(R.id.btnPause);
		btnShutdown = (Button) activity.findViewById(R.id.btnShutdown);
	}

	public ImageButton refreshButton;
	public TextView txv_wind;
	public TextView txv_humidity;
	public TextView txv_updatetime;
	public TextView txv_location;

	private void initWeatherControls() {
		refreshButton = (ImageButton) activity
				.findViewById(R.id.refresh_button);

		txv_location = (TextView) activity.findViewById(R.id.location);
		txv_updatetime = (TextView) activity.findViewById(R.id.update_time);
		txv_humidity = (TextView) activity.findViewById(R.id.humidity);
		txv_wind = (TextView) activity.findViewById(R.id.wind);
	}


}
