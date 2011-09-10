package com.moupress.app.ui;

import android.app.Activity;
import android.content.Context;
import android.gesture.GestureOverlayView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.moupress.app.Const;
import com.moupress.app.R;

public class UIMgr {

	Activity activity;
	
	public UIMgr(Activity activity) {
		this.activity = activity;
		if (Const.ISDEBUG) {
			initStreamingControls();
			initSoonzeControls();
			initAlarmControls();
			initAlarmTTSControls();
			initWeatherControls();
		} else {
			initNewUI();
		}
		
	}
	
	public Button btn_stream;
	public ImageButton btn_play;
	public TextView txv_stream;
	public boolean bIsPlaying;
	public ProgressBar progressBar;
	//public StreammingMgr uiStreamer;
	
	private void initStreamingControls() {
    	txv_stream = (TextView) activity.findViewById(R.id.text_streamed);
		btn_stream = (Button) activity.findViewById(R.id.button_stream);

		btn_play = (ImageButton) activity.findViewById(R.id.button_play);
		btn_play.setEnabled(false);
		progressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
    }

    
    public GestureOverlayView gesturesView;
    private void initSoonzeControls() {
    	gesturesView = (GestureOverlayView) activity.findViewById(R.id.gestures);
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
    	btnPlay = (Button)activity.findViewById(R.id.btnStart);
        btnPause = (Button)activity.findViewById(R.id.btnPause);
        btnShutdown = (Button)activity.findViewById(R.id.btnShutdown);
    }
    
    public ImageButton refreshButton;
    public TextView txv_wind;
    public TextView txv_humidity;
    public TextView txv_updatetime;
    public TextView txv_location;
    private void initWeatherControls() {
    	refreshButton = (ImageButton)activity.findViewById(R.id.refresh_button);
    	
    	txv_location = (TextView) activity.findViewById(R.id.location);
    	txv_updatetime = (TextView) activity.findViewById(R.id.update_time);
    	txv_humidity = (TextView) activity.findViewById(R.id.humidity);
		txv_wind = (TextView) activity.findViewById(R.id.wind);
    }
    
    public ViewFlipper alarmInfoViewSlipper;
    public ListView hsListView;
    public ListView snoozeListView;
    public ListView alarmListView;
    public ListView soundListView;
    public LayoutInflater viewInflator;
	private String[] hsDisplayTxt =  {"Rain  10C","10:00pm","Gesture"};
	private int[] hsDisplayIcon =  {R.drawable.world,R.drawable.clock,R.drawable.disc};
	private String[] snoozeDisplayTxt =  {"Gesture","Flip","Swing"};
	private int[] snoozeDisplayIcon =  {R.drawable.disc,R.drawable.disc,R.drawable.disc};
	private String[] alarmDisplayTxt =  {"8:00 am","9:00 am","10:00 am"};
	private int[] alarmDisplayIcon =  {R.drawable.clock,R.drawable.clock,R.drawable.clock};
	private String[] soundDisplayTxt =  {"BBC News","Wall Street Journal","Personal Notes"};
	private int[] soundDisplayIcon =  {R.drawable.radio,R.drawable.radio,R.drawable.radio};
    public Button btnHome;
    public Button btnSoonze;
    public Button btnAlarm;
    public Button btnSound;

	private void initNewUI() {
    	alarmInfoViewSlipper = (ViewFlipper)activity.findViewById(R.id.optionflipper);
        hsListView = (ListView) activity.findViewById(R.id.hslistview);
        hsListView.setAdapter(new HsLVAdapter(hsDisplayTxt,hsDisplayIcon));
        snoozeListView = (ListView)activity.findViewById(R.id.snoozelistview);
        snoozeListView.setAdapter(new HsLVAdapter(snoozeDisplayTxt,snoozeDisplayIcon));
        alarmListView = (ListView)activity.findViewById(R.id.alarmlistview);
        alarmListView.setAdapter(new HsLVAdapter(alarmDisplayTxt,alarmDisplayIcon));
        soundListView = (ListView)activity.findViewById(R.id.soundlistview);
        soundListView.setAdapter(new HsLVAdapter(soundDisplayTxt,soundDisplayIcon) );
        btnHome = (Button)activity.findViewById(R.id.homebtn);
        btnHome.setOnClickListener(SlidingUpPanelButtonListener);
        btnSoonze = (Button)activity.findViewById(R.id.snoozebtn);
        btnSoonze.setOnClickListener(SlidingUpPanelButtonListener);
        btnAlarm = (Button)activity.findViewById(R.id.alarmbtn);
        btnAlarm.setOnClickListener(SlidingUpPanelButtonListener);
        btnSound = (Button)activity.findViewById(R.id.soundbtn);
        btnSound.setOnClickListener(SlidingUpPanelButtonListener);
        
    }
    
	Button.OnClickListener SlidingUpPanelButtonListener = new OnClickListener(){

        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case R.id.homebtn:
                    homeBtnClick();
                    break;
                case R.id.snoozebtn:
                    snoozeBtnClick();
                    break;
                case R.id.alarmbtn:
                    alarmBtnClick();
                    break;
                case R.id.soundbtn:
                    soundBtnClick();
                    break;
                default://no match
                    System.out.println("btn is from nowhere."); 
            }
            
        }
	
	};
    
    protected void flipperListView(int toDisplayedChild)
    {
        if( alarmInfoViewSlipper.getDisplayedChild() > toDisplayedChild)
        {
           alarmInfoViewSlipper.setInAnimation(activity, R.anim.slidein);
           alarmInfoViewSlipper.setOutAnimation(activity, R.anim.slideout);
           alarmInfoViewSlipper.setDisplayedChild(toDisplayedChild); 
        }
        else if( alarmInfoViewSlipper.getDisplayedChild() < toDisplayedChild)
        {
           alarmInfoViewSlipper.setInAnimation(activity, R.anim.slideinfromright);
           alarmInfoViewSlipper.setOutAnimation(activity, R.anim.slideouttoleft);
           alarmInfoViewSlipper.setDisplayedChild(toDisplayedChild); 
        }
    }
    protected void homeBtnClick()
    {
    	System.out.println("Home Button On Click!");
        flipperListView(0);
    }
    
    protected void snoozeBtnClick()
    {
    	System.out.println("Snooze Button On Click!");
        flipperListView(1);
    }
    
    protected void alarmBtnClick()
    {
    	System.out.println("Alarm Button On Click!");
        flipperListView(2);
    }
    
    protected void soundBtnClick()
    {
    	System.out.println("Sound Button On Click!");
        flipperListView(3);
    }
    
    private class HsLVAdapter extends BaseAdapter{
    	private String[] txtisplays ;
    	private int[] icons ;
    	
    	public  HsLVAdapter(String[] displayStrings,int[] displayInts)
    	{
    		viewInflator = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		txtisplays = displayStrings;
    		icons = displayInts;
    	}
		@Override
		public int getCount() {
			return txtisplays.length;
		}

		@Override
		public Object getItem(int position) {
			return txtisplays[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null)
			{
				convertView = viewInflator.inflate(R.layout.home_screen_item, null);
				
			}
			ImageView imgView = (ImageView) convertView.findViewById(R.id.alarmitemicon);
			imgView.setImageResource(icons[position]);
			TextView textView = (TextView) convertView.findViewById(R.id.alarmitemtxt);
			textView.setText(txtisplays[position]);
			return convertView;
		}
    }
}
