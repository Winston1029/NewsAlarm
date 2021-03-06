package com.moupress.app.ui;

import java.util.ArrayList;
import java.util.Calendar;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.gesture.GestureOverlayView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.moupress.app.Const;
import com.moupress.app.R;
import com.moupress.app.Const.SHARED_METHODS;
import com.moupress.app.ui.SlideButton.OnChangeListener;
import com.moupress.app.ui.SlideButton.SlideButton;
import com.moupress.app.ui.SlideButton.SlideButtonAdapter;
import com.moupress.app.ui.SlideButton.TextSlideButtonAdapter;
import com.moupress.app.ui.uiControlInterface.OnExitDialogListener;
import com.moupress.app.util.DbHelper;

public class UIMgr {

	Activity activity;
	Context context;

	public UIMgr(Activity activity,Context ctx) {
		this.activity = activity;
		this.context = ctx;
		initAlarmSettings();
		initUI();
		initSoonzeControls();
		initDismissControls();
	}

	/**
	 * Initialize all UIs
	 */
	private void initUI() {
		this.initHomeUI();
		this.initSnoozeUI();
		this.initAlarmTimeUI();
		this.initAlarmSoundUI();
		this.initToolbarUI();
		this.initMainContainer();
	}

	// =======================Home UI==============================================
	public ListView hsListView;
	private AlarmListViewAdapter hsListAdapter;
	private String[] hsDisplayTxt = { "No Weather Info", "No Alarm Set", "Gesture" };
	private int[] hsDisplayIcon = { R.drawable.wheather, R.drawable.alarm, R.drawable.snooze };
	private boolean[] hsSelected = { false, false, false };
	

	/**
	 * Initilise home screen.
	 */
	private void initHomeUI() {
		hsListView = (ListView) activity.findViewById(R.id.hslistview);
		
		hsListAdapter = new AlarmListViewAdapter(hsDisplayTxt, hsDisplayIcon, hsSelected,R.layout.home_screen_item);
		hsListView.setAdapter(hsListAdapter);
		hsListView.setOnItemClickListener(optionListOnItemClickListener);
		//setSnoozeMode();
	}

	// =======================Snooze UI==============================================
	public ListView snoozeListView;
	private AlarmListViewAdapter snoozeAdapter;
	private String[] snoozeDisplayTxt = { "Gesture", "Flip", "Swing" };
	private int[] snoozeDisplayIcon = { R.drawable.gesture, R.drawable.flip,R.drawable.swing };
	private boolean[] snoozeSelected = { true, true, false };

	public boolean[] getSnoozeSelected() {
		return snoozeSelected;
	}


	/**
	 * Initialize snooze screen
	 */
	private void initSnoozeUI() {
		DbHelper dbHelper = new DbHelper(activity);
		for(int i=0;i<snoozeDisplayTxt.length;i++)
		{
			Boolean selected = dbHelper.GetBool(Const.SNOOZEMODE + Integer.toString(i),snoozeSelected[i]);
			if(selected != null)
			snoozeSelected[i] = selected;
		}
		snoozeListView = (ListView) activity.findViewById(R.id.snoozelistview);
		snoozeAdapter = new AlarmListViewAdapter(snoozeDisplayTxt,snoozeDisplayIcon, snoozeSelected,R.layout.home_screen_item);
		snoozeListView.setAdapter(snoozeAdapter);
		snoozeListView.setOnItemClickListener(optionListOnItemClickListener);
		setSnoozeMode();
	}
	
	private void setSnoozeMode() {
		for(int i=0;i<snoozeSelected.length;i++)
		{
			if(snoozeSelected[i]==true)
			{
				hsDisplayTxt[2]=snoozeDisplayTxt[i];
			    //hsListAdapter.notifyDataSetChanged();
			    System.out.println("snoozeReseted! " + hsDisplayTxt[2]);
			    hsListAdapter.updateSnoozeModeSelection(hsDisplayTxt[2], 2);
			    return;
			}
		}
	}

	// =======================Alarm Time UI==============================================
	public ListView alarmListView;
	private AlarmListViewAdapter alarmAdapter;
	private WheelView hours;
	private WheelView minutes;
	private WheelView amOrpm;
	private Button btnUpdateTimeOk;
	private Button btnUpdateTimeCancel;

	private String[] alarmDisplayTxt = { "8:00 am", "9:00 am", "10:00 am" };
	private int[] alarmDisplayIcon = { R.drawable.alarm_time, R.drawable.alarm_time,R.drawable.alarm_time };
	private boolean[] alarmSelected = { true, false, false };
	public boolean[] getAlarmSelected() {
		return alarmSelected;
	}

	private static int ALARM_POSITION = 0;
	private String[] AMPM = { "am", "pm" };
	private boolean bSettingAlarmTimeDisableFlip;

	public void setbSettingAlarmTimeDisableFlip(boolean bSettingAlarmTimeDisableFlip) {
		this.bSettingAlarmTimeDisableFlip = bSettingAlarmTimeDisableFlip;
	}

	private NewsAlarmSlidingUpPanel timeSlidingUpPanel;
	
	private static final String[] weekdays = new String[]{"S","M","T","W","T","F","S"};
	private boolean[][] daySelected = new boolean[][]{{false,true,true,true,true,true,false},
													 {false,true,true,true,true,true,false},
													 {true,false,false,false,false,false,true}};


	private SlideButtonAdapter viewAdapter;
	private SlideButton slideBtn;
	private NewsAlarmDigiClock weekday;

	/**
	 * Initialize Alarm Time Screen
	 */
	private void initAlarmTimeUI() {
		alarmListView = (ListView) activity.findViewById(R.id.alarmlistview);
		alarmAdapter = new AlarmListViewAdapter(alarmDisplayTxt,alarmDisplayIcon, alarmSelected,R.layout.alarm_list_item);
		alarmListView.setAdapter(alarmAdapter);
		alarmListView.setOnItemClickListener(optionListOnItemClickListener);

		btnUpdateTimeOk = (Button) activity.findViewById(R.id.timeaddok);
		btnUpdateTimeCancel = (Button) activity.findViewById(R.id.timeaddcancel);
		btnUpdateTimeOk.setOnClickListener(alarmWheelButtonListener);
		btnUpdateTimeCancel.setOnClickListener(alarmWheelButtonListener);
		timeSlidingUpPanel = (NewsAlarmSlidingUpPanel) activity.findViewById(R.id.timeupdatepanel);
		timeSlidingUpPanel.setOpen(false);

		weekday = (NewsAlarmDigiClock) activity.findViewById(R.id.weekday);
		timeSlidingUpPanel
				.setPanelSlidingListener(new NewsAlarmSlidingUpPanel.PanelSlidingListener() {

					@Override
					public void onSlidingUpEnd() {
						slideBtn.reLoadViews();
						slideBtn.setSlidePosition(weekday.getWeekDayRank()-1);
					}

					@Override
					public void onSlidingDownEnd() {
						bSettingAlarmTimeDisableFlip = false;
						buttonBarSlidingUpPanel.toggle();
					}
				});
		bSettingAlarmTimeDisableFlip = false;
		hours = (WheelView) activity.findViewById(R.id.wheelhour);
		minutes = (WheelView) activity.findViewById(R.id.wheelminute);
		amOrpm = (WheelView) activity.findViewById(R.id.wheelsecond);

		hours.setViewAdapter(new NumericWheelAdapter(activity, 1, 12));
		hours.setCurrentItem(5);
		minutes.setViewAdapter(new NumericWheelAdapter(activity, 0, 59, "%02d"));
		minutes.setCurrentItem(30);
		amOrpm.setViewAdapter(new ArrayWheelAdapter<String>(activity, AMPM));
		
		slideBtn =(SlideButton) activity.findViewById(R.id.slideBtn);
	    slideBtn.setOnChangedListener(new OnChangeListener()
	    {

	    	public void OnChanged(int weekdayPos,boolean direction,View v) {

	    		if(direction == true)
	    		{
	    			daySelected[ALARM_POSITION][weekdayPos]=true;
	    			if(daySelected[ALARM_POSITION][weekdayPos]==true)
		    		((TextView)v).setTextColor(activity.getResources().getColor(R.color.orange_red));
	    		}
	    		else
	    		{
	    			daySelected[ALARM_POSITION][weekdayPos]=false;
	    			if(daySelected[ALARM_POSITION][weekdayPos]==false)
		    		((TextView)v).setTextColor(activity.getResources().getColor(R.color.black));
	    		}
	    	}

			@Override
			public void OnSelected(int weekdayPos,  View v, int mode) {
				//System.out.println("Alarm Position "+ALARM_POSITION);
				if(mode == 0)
				{
					if(daySelected[ALARM_POSITION][weekdayPos]==true)
					{
						daySelected[ALARM_POSITION][weekdayPos]= false;
						((TextView)v).setTextColor(activity.getResources().getColor(R.color.black));
					}
					else
					{
						daySelected[ALARM_POSITION][weekdayPos]= true;
						((TextView)v).setTextColor(activity.getResources().getColor(R.color.orange_red));
					}
				}
				else if(mode == 1)
				{
					
					for(int i =0 ;i< daySelected[ALARM_POSITION].length;i++)
					{
						if(daySelected[ALARM_POSITION][i]==true)
							return;
					}
					 daySelected[ALARM_POSITION][weekdayPos]= true;
					((TextView)v).setTextColor(activity.getResources().getColor(R.color.orange_red));

				}
				else if (mode == 2)
				{
					daySelected[ALARM_POSITION][weekdayPos]= false;
					((TextView)v).setTextColor(activity.getResources().getColor(R.color.black));

				} else if(mode ==3)
				{
					if( daySelected[ALARM_POSITION][weekdayPos]==true)
					((TextView)v).setTextColor(activity.getResources().getColor(R.color.orange_red));
					else if (daySelected[ALARM_POSITION][weekdayPos]== false)
					((TextView)v).setTextColor(activity.getResources().getColor(R.color.black));
				}
			}
	    	
	    });
		viewAdapter = new TextSlideButtonAdapter(weekdays, activity);
		slideBtn.setViewAdapter(viewAdapter);
        bSettingAlarmTimeDisableFlip = false;
        
		alarmAdapter.updateWeekDaysSelection(daySelected[ALARM_POSITION],ALARM_POSITION);
		bSettingAlarmTimeDisableFlip = false;
	}
	

	// =======================Alarm Sound UI==============================================
	public ListView soundListView;
	private AlarmListViewAdapter soundAdapter;
	private String[] soundDisplayTxt = { "BBC News", "933", "Calendar" };
	private int[] soundDisplayIcon = { R.drawable.radio_bbc, R.drawable.music,R.drawable.calendar };
	private boolean[] soundSelected = { true, false, false };
	private static final int BBC_OR_933 = 1;

	/**
	 * Initialize Alarm Sound Screen
	 */
	private void initAlarmSoundUI() {
		DbHelper dbHelper = new DbHelper(activity);
		for(int i=0;i<soundDisplayTxt.length;i++)
		{
			Boolean selected = dbHelper.GetBool(Const.ALARMSOUNDE + Integer.toString(i),soundSelected[i]);
			//System.out.println("Value of Selected "+ selected);
			if(selected != null)
			soundSelected[i] = selected;
		}
		soundListView = (ListView) activity.findViewById(R.id.soundlistview);
		soundAdapter = new AlarmListViewAdapter(soundDisplayTxt,soundDisplayIcon, soundSelected,R.layout.home_screen_item);
		soundListView.setAdapter(soundAdapter);
		soundListView.setOnItemClickListener(optionListOnItemClickListener);
	}
	
	public boolean[] getSoundSelected() {return soundSelected;}

	// ==============Alarm Toolbar UI==============================================
	public Button btnHome;
	public Button btnSoonze;
	public Button btnAlarm;
	public Button btnSound;
	private NewsAlarmSlidingUpPanel buttonBarSlidingUpPanel;
	private LinearLayout indicatorLayout;

	/**
	 * Initialize Toolbar UI
	 */
	private void initToolbarUI() {
		btnHome = (Button) activity.findViewById(R.id.homebtn);
		btnHome.setOnClickListener(toolbarButtonListener);

		btnSoonze = (Button) activity.findViewById(R.id.snoozebtn);
		btnSoonze.setOnClickListener(toolbarButtonListener);

		btnAlarm = (Button) activity.findViewById(R.id.alarmbtn);
		btnAlarm.setOnClickListener(toolbarButtonListener);

		btnSound = (Button) activity.findViewById(R.id.soundbtn);
		btnSound.setOnClickListener(toolbarButtonListener);
		
		indicatorLayout = (LinearLayout)activity.findViewById(R.id.page_indicator_layout);

		buttonBarSlidingUpPanel = (NewsAlarmSlidingUpPanel) activity.findViewById(R.id.removeItemPanel);
		buttonBarSlidingUpPanel.setOpen(true);
		buttonBarSlidingUpPanel
				.setPanelSlidingListener(new NewsAlarmSlidingUpPanel.PanelSlidingListener() {

					@Override
					public void onSlidingUpEnd() {
					}

					@Override
					public void onSlidingDownEnd() {
						bSettingAlarmTimeDisableFlip = true;
						timeSlidingUpPanel.toggle();
					}
				});

		alarmInfoViewSlipper = (ViewFlipper) activity.findViewById(R.id.optionflipper);
	}
	
	//================Main Container==========================================
	public LinearLayout llMainContainer = null;
	/**
	 * Register the main container with onTouchlistener	
	 */
	private void initMainContainer() {
	    llMainContainer = (LinearLayout)this.activity.findViewById(R.id.mainContainer);
	    llMainContainer.setOnTouchListener(new View.OnTouchListener() {
	        float XStart = 0;
            float XEnd = 0;
            int toDisplayChildId = 0;
            static final int EFFECTIVE_MOVEMENT = 50;
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                
                switch(event.getAction())
                {
                  case MotionEvent.ACTION_DOWN:
                       //System.out.println("Action Down On Touch!! "+event.getX()+" "+event.getY()+" Action "+event.getAction());
                       XStart = XEnd = event.getX();
                       return true;
                   case MotionEvent.ACTION_MOVE:
                       //System.out.println("Action Move On Touch!! "+event.getY()+" Action "+event.getAction());
                       XEnd = event.getX();
                       return true;
                   case MotionEvent.ACTION_UP:
                       if(XEnd > XStart + EFFECTIVE_MOVEMENT)
                       {
                           System.out.println("from left: " + XStart + "to right: " + XEnd + " Page: "+ alarmInfoViewSlipper.getDisplayedChild());
                           toDisplayChildId = alarmInfoViewSlipper.getDisplayedChild();
                           if(toDisplayChildId != 0)
//                               flipperListView(3);
//                           else
                               flipperListView(toDisplayChildId - 1);
                       }
                       else if(XEnd < XStart - EFFECTIVE_MOVEMENT){
                         System.out.println("from right: " + XStart + "to left: " + XEnd);
                           toDisplayChildId = alarmInfoViewSlipper.getDisplayedChild();
                           if(toDisplayChildId != 3)
//                               flipperListView(0);
//                           else
                               flipperListView(toDisplayChildId + 1);
                           
                           //Test Snoozed View
//                           if(toDisplayChildId ==3)
//                        	   showSnoozeView();
                       }
                       XEnd = XStart = 0;
                       return true;
                   case MotionEvent.ACTION_CANCEL:
                       return true;
                    default:
                        //System.out.println("Touch Event : " + event.getAction());
                        return true;
                }
            }
        });
	}

//All Listener Events===================================
	public GestureOverlayView gesturesView;
	public ViewFlipper alarmInfoViewSlipper;
	private OnListViewItemChangeListener onListViewItemChangeListener;

	/**
	 * Initialize Display alarm time text
	 */
	private void initAlarmSettings(int dummy) {
		hsDisplayTxt[1] = "No Alarm Set";
		
		DbHelper helper = new DbHelper(this.activity);
		for (int i = 0; i < alarmDisplayTxt.length; i++) {
			
		}
	}
	private void initAlarmSettings() {
		DbHelper helper = new DbHelper(this.activity);
		Calendar cal = Calendar.getInstance();
		int hours, mins;
		int nextAlarm = 0;
		int dayIndex = 7;//max is 7
		hsDisplayTxt[1] = "No Alarm Set";
		boolean[] daySelectedLocal = new boolean[7];
		// alarm Time
		for (int i = 0; i < alarmDisplayTxt.length; i++) {
			Boolean Selected = helper.GetBool(Const.ISALARMSET+ Integer.toString(i),alarmSelected[i]);
			if(Selected != null)
			alarmSelected[i] = Selected;
			
			boolean[] selectedArray = helper.getSelectedDay(i);
			
			if(selectedArray != null) daySelectedLocal = selectedArray;
			
			hours = helper.GetInt(Const.Hours + Integer.toString(i),Const.defAlarmDisplayHours[i]);
			mins = helper.GetInt(Const.Mins + Integer.toString(i),Const.defAlarmDisplayMins[i]);
			
			if (hours == Const.DefNum || mins == Const.DefNum) {
			    cal.setTimeInMillis(System.currentTimeMillis());
                hours = cal.get(Calendar.HOUR_OF_DAY);
                mins = cal.get(Calendar.MINUTE);
                //since daySelected is default to false
                //we need also modify Current day as selected
                //boolean[] daySelected = Const.DaySelected;
                //daySelected[cal.get(Calendar.DAY_OF_WEEK)] = true;
                //helper.saveAlarmSelectedDay(daySelected, i);
			}
			
			//Convert Hours24 to Hours12
			if(hours==0)
			{
				alarmDisplayTxt[i] = Integer.toString(hours+12) + ":"
                + String.format("%02d", mins) + " " + this.AMPM[0];
			}
			else if(hours ==12)
			{
				 alarmDisplayTxt[i] = Integer.toString(hours) + ":"
	                + String.format("%02d", mins) + " " + this.AMPM[1];
			}
			else if(hours<12)
            {
                alarmDisplayTxt[i] = Integer.toString(hours) + ":"
                + String.format("%02d", mins) + " " + this.AMPM[0];
            }
            else {
                alarmDisplayTxt[i] = Integer.toString(hours%12) + ":"
                + String.format("%02d", mins) + " " + this.AMPM[1];
            }
			
			this.daySelected[i] = daySelectedLocal;
			if(alarmSelected[i])
			{
    			int index =0;
    			boolean isCurrentDayLessThanNow = false;
    			cal.setTimeInMillis(System.currentTimeMillis());
    			int test = cal.get(Calendar.DAY_OF_WEEK)-1;
    	        for (int t = 0; t < daySelectedLocal.length; t++)
    	        {
    	            index = (t+test)%7;
    	            //to get the nearest selected day
    	            if(daySelectedLocal[index])
    	            {
    	                if(t < dayIndex)
    	                {
    	                    //need to check if the time is past if index == 0
    	                    if( t == 0)
    	                    {
    	                        if((hours < cal.get(Calendar.HOUR_OF_DAY))
    	                                ||(hours == cal.get(Calendar.HOUR_OF_DAY)&&mins <= cal.get(Calendar.MINUTE)))
    	                        {
    	                            isCurrentDayLessThanNow = true;
    	                            continue;
                                }
    	                    }
    	                    hsDisplayTxt[1] = alarmDisplayTxt[i];
    	                    nextAlarm = hours*60+ mins;
    	                    dayIndex = t;
    	                    break;
    	                }
    	                if(t == dayIndex)
    	                {
    	                    if(t== 0) {
        	                    if(hours < cal.get(Calendar.HOUR_OF_DAY))
        	                        continue;
                                else if(hours == cal.get(Calendar.HOUR_OF_DAY)&&mins <= cal.get(Calendar.MINUTE)) {
                                    continue;
                                }
    	                    }
    	                    int nowAlarm = hours*60+ mins;
    	                    if(nextAlarm > nowAlarm)
    	                    {
    	                        hsDisplayTxt[1] = alarmDisplayTxt[i];
    	                        nextAlarm = nowAlarm;
    	                    }
    	                    break;
    	                }
    	                if(t > dayIndex)
    	                {
    	                    break;
    	                }
    	            }
    	            //if special condition
    	            if(dayIndex == 7)
    	            {
    	                if(isCurrentDayLessThanNow)
    	                {
                            if(nextAlarm > 0)
                            {
                                if(nextAlarm > hours*60+ mins)
                                {
                                    hsDisplayTxt[1] = alarmDisplayTxt[i];
                                    nextAlarm = hours*60+ mins; 
                                }
                              
                            }
                            else {//nextAlarm == 0
                                hsDisplayTxt[1] = alarmDisplayTxt[i];
                                nextAlarm = hours*60+ mins; 
                            }
    	                }
    	            }
    	        }
    	        
			}
		}
		if (dayIndex == 7) {
			
		}
	}

	
	/**
	 * List view Item click
	 */
	
	AdapterView.OnItemClickListener optionListOnItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			switch (parent.getId()) {
			case R.id.snoozelistview:
				
				break;
			case R.id.soundlistview:
				
				break;
			case R.id.alarmlistview:
	            ALARM_POSITION = position;
				
				//slideBtn.buildViewForMeasuring();
				
				if(buttonBarSlidingUpPanel.getOpen())
					buttonBarSlidingUpPanel.toggle();
				break;
			case R.id.hslistview:
				hsListViewClicked(position);
			}
		}
	};

	/**
	 * Alarm Wheel's Setting Button Click
	 */
	Button.OnClickListener alarmWheelButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.timeaddok:
				alarmAdapter.updateTxtArrayList("" + (hours.getCurrentItem()+1)
						+ ":" + String.format("%02d", minutes.getCurrentItem())
						+ " " + (amOrpm.getCurrentItem() == 0 ? "am" : "pm"),
						ALARM_POSITION);
				//System.out.println("Current Hour is "+hours.getCurrentItem());
				alarmAdapter.updateWeekDaysSelection(daySelected[ALARM_POSITION],ALARM_POSITION);
				timeSlidingUpPanel.toggle();
				// Call Back function on Alarm Time Change
				int hours24;
				 //amOrpm.getCurrentItem() == 0  (hours.getCurrentItem()+1) : (hours.getCurrentItem() + 13);
				if(amOrpm.getCurrentItem()==0)
				{
					hours24 = hours.getCurrentItem()+1;
					if(hours24==12)
						hours24=0;
				}
				else
				{
					hours24 = hours.getCurrentItem()+13;
					if(hours24==24)
						hours24=12;
				}
				onListViewItemChangeListener.onAlarmTimeChanged(ALARM_POSITION,
						alarmSelected[ALARM_POSITION], hours24,
						minutes.getCurrentItem(), 0, 0, daySelected[ALARM_POSITION]);
				//Get Weekdays selected
				break;
			case R.id.timeaddcancel:
				timeSlidingUpPanel.toggle();
				break;
			}
		}
	};

	/**
	 * Toolbar button listener
	 */
	Button.OnClickListener toolbarButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.homebtn:
				flipperListView(Const.SCREENS.HomeUI.ordinal());
				break;
			case R.id.snoozebtn:
				flipperListView(Const.SCREENS.SnoozeUI.ordinal());
				break;
			case R.id.alarmbtn:
				flipperListView(Const.SCREENS.AlarmTimeUI.ordinal());
				break;
			case R.id.soundbtn:
				flipperListView(Const.SCREENS.AlarmSoundUI.ordinal());
				break;
			default:// no match
				System.out.println("btn is from nowhere.");
			}
		}
	};
	
	/*
	 * Option Selected/Unselected  
	 */
	
	 class OnListItemClickListener implements OnClickListener 
	 {
		 private int position;
		 private View convertView;
		 private ViewGroup parent;
		
		 public OnListItemClickListener(int position,View convertView, ViewGroup parent)
		 {
			 this.position = position;
			 this.convertView = convertView;
			 this.parent = parent;
		 }
		 
		@Override
		public void onClick(View v) {
			//System.out.println("Position "+position + " parent "+ parent.getId());
			switch (parent.getId()) {
			case R.id.snoozelistview:
				toggleSelectListItem(snoozeAdapter, snoozeSelected, position);
				// Call back function for Snooze Mode selected/unselected
				setSnoozeMode();
				onListViewItemChangeListener.onSnoozeModeSelected(position,
						snoozeSelected[position]);
				break;
			case R.id.soundlistview:
				if (position <= BBC_OR_933 && soundSelected[position] == false && soundSelected[1 - position] == true) {
					// make BBC and 993 broadcasting mutual exclusive
					toggleSelectListItem(soundAdapter, soundSelected, 1 - position);
					onListViewItemChangeListener.onAlarmSoundSelected(1-position,
							soundSelected[1-position]);
				}
				toggleSelectListItem(soundAdapter, soundSelected, position);
				// Call back function for Alarm Sound selected/unselected
				//System.out.println("Sound Selected "+soundSelected[0]+" "+soundSelected[1]);
				onListViewItemChangeListener.onAlarmSoundSelected(position,
						soundSelected[position]);
				break;
			case R.id.alarmlistview:
				toggleSelectListItem(alarmAdapter, alarmSelected, position);
				// Call back function for alarm time selected/unselected
				onListViewItemChangeListener.onAlarmTimeSelected(position,
						alarmSelected[position]);
				break;
			case R.id.hslistview:
				System.out.println("home List View is here");
			}
		}
	 }
	 
	 /*
	  *  Toggle Select List Item
	  */

	private void toggleSelectListItem(AlarmListViewAdapter listAdapter,boolean[] chked, int pos) {
		chked[pos] = !chked[pos];
		listAdapter.invertSelect(pos);
		listAdapter.notifyDataSetChanged();
	}

	/**
	 * Home Screen List Item Click Response
	 * 
	 * @param position
	 */
	private void hsListViewClicked(int position) {
		switch (position) {
		case 0:
			// display weather info
			break;
		case 1:
			flipperListView(Const.SCREENS.AlarmTimeUI.ordinal());
			break;
		case 2:
			flipperListView(Const.SCREENS.SnoozeUI.ordinal());
			break;
		}
	}

	/**
	 * common adapter used in all listview
	 * 
	 * @author Saya
	 * 
	 */
	private class AlarmListViewAdapter extends BaseAdapter {

		private ArrayList<NewsAlarmListItem> optionArrayList;
		private LayoutInflater viewInflator;
		private int resItemId;

		public AlarmListViewAdapter(String[] displayStrings, int[] displayInts,boolean[] displayChecked, int resItemId) {
			viewInflator = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			optionArrayList = new ArrayList<NewsAlarmListItem>();
			loadArrayList(displayStrings, displayInts, displayChecked);
			this.resItemId = resItemId;
			// txtisplays = displayStrings;
			// icons = displayInts;
		}

		public void updateWeekDaysSelection(boolean[] daySelected, int alarmposition) {
			
			optionArrayList.get(alarmposition).setWeekDaysSelection(daySelected);
			this.notifyDataSetChanged();
		}
		
		public void updateSnoozeModeSelection(String displayTxt, int alarmPosition)
		{
			optionArrayList.get(alarmPosition).setOptionTxt(displayTxt);
			this.notifyDataSetChanged();
		}

		public void loadArrayList(String[] displayStrings, int[] displayInts,
				boolean[] displayChecked) {
			for (int i = 0; i < displayStrings.length; i++) {
				addToArrayList(displayStrings[i], displayInts[i],displayChecked[i],daySelected[i]);
			}
		}
		

		public void addToArrayList(String displayString, int displayInt,
				boolean displayChk, boolean[] daySelected ) {

			optionArrayList.add(new NewsAlarmListItem(displayInt,displayString, displayChk,daySelected ));
		}

		public void updateTxtArrayList(String displayString, int position) {
			if (displayString.length() == 0) {
				optionArrayList.get(position).setOptionTxt(Const.NON_WEATHER_MSG);
			} else
				optionArrayList.get(position).setOptionTxt(displayString);
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return optionArrayList.size();
		}

		@Override
		public Object getItem(int position) {
			return optionArrayList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = viewInflator.inflate(resItemId,null);
			}
			ImageView imgView = (ImageView) convertView.findViewById(R.id.alarmitemicon);
			imgView.setImageResource(optionArrayList.get(position).getOptionIcon());
			TextView textView = (TextView) convertView.findViewById(R.id.alarmitemtxt);
			textView.setText(optionArrayList.get(position).getOptionTxt());
//			if(parent.getId()==R.id.hslistview)
//			System.out.println("It is called!");
			ImageView chkImgView = (ImageView) convertView.findViewById(R.id.checked);
			chkImgView.setOnClickListener(new OnListItemClickListener(position,convertView,parent));
			
			if (optionArrayList.get(position).isOptionSelected()) {
				chkImgView.setImageResource(R.drawable.checkbtn);
				//chkImgView.setVisibility(View.VISIBLE);
			} else {
				chkImgView.setImageResource(R.drawable.uncheckbtn);
				//chkImgView.setVisibility(View.INVISIBLE);
			}
			if(parent.getId()==R.id.hslistview)
			{
				chkImgView.setVisibility(View.INVISIBLE);
			}
			if(parent.getId()==R.id.alarmlistview)
				loadSubTextView((LinearLayout)convertView.findViewById(R.id.weekdaylist),R.layout.weekday_small,position);
			return convertView;
		}

		private void loadSubTextView(LinearLayout linearLayout, int viewId, int position) 
		{
			linearLayout.removeAllViews();
			for(int i=0;i<weekdays.length;i++)
			{
				TextView tv = (TextView) viewInflator.inflate(viewId, null);
				tv.setText(weekdays[i]);
				if(optionArrayList.get(position).getWeekDaysSelection()[i]==true)
				tv.setTextColor(activity.getResources().getColor(R.color.white));
				else
				tv.setTextColor(activity.getResources().getColor(R.color.grey));
				linearLayout.addView(tv);
			}
		}

		public void invertSelect(int position) {
			optionArrayList.get(position).setOptionSelected(!optionArrayList.get(position).isOptionSelected());
		}
	}

	/**
	 * UI Flipper Animation
	 * When user is setting alarm time using timeSlidingUpPanel, disable flip
	 * 
	 * @param toDisplayedChild
	 */
	public void flipperListView(final int toDisplayedChild) {
		final int currentChild = alarmInfoViewSlipper.getDisplayedChild();
		if (!bSettingAlarmTimeDisableFlip) {
			if (alarmInfoViewSlipper.getDisplayedChild() > toDisplayedChild) {
				alarmInfoViewSlipper.setInAnimation(activity, R.anim.slidein);
				alarmInfoViewSlipper.setOutAnimation(activity, R.anim.slideout);
				alarmInfoViewSlipper.setDisplayedChild(toDisplayedChild);
			} else if (alarmInfoViewSlipper.getDisplayedChild() < toDisplayedChild) {
				alarmInfoViewSlipper.setInAnimation(activity,R.anim.slideinfromright);
				alarmInfoViewSlipper.setOutAnimation(activity,R.anim.slideouttoleft);
				alarmInfoViewSlipper.setDisplayedChild(toDisplayedChild);
			}
			buttonBarSlidingUpPanel.setVisibility(View.VISIBLE);
			this.indicatorLayout.setVisibility(View.VISIBLE);
			//if((alarmInfoViewSlipper.getInAnimation())!=null)
			alarmInfoViewSlipper.getInAnimation().setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					System.out.println("In Animation End!!");
					selectPageIndicator(toDisplayedChild);

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}});
			
			//if((alarmInfoViewSlipper.getOutAnimation()!=null))
			alarmInfoViewSlipper.getOutAnimation().setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					System.out.println("Out Animation Start!!");
					unSelectPageIndicator(currentChild);
					if(currentChild==Const.SCREENS.SnoozeUI.ordinal())
					{
						System.out.println("Snooze Screen is shown");
						if(!setDefaultSelection(snoozeSelected))
						{
							//snoozeSelected[0] = true;
							toggleSelectListItem(snoozeAdapter, snoozeSelected, 0);
							// Call back function for Snooze Mode selected/unselected
							onListViewItemChangeListener.onSnoozeModeSelected(0,snoozeSelected[0]);
							
							Toast.makeText(activity, "No Snooze Mode Selected, Use Gesture by Default", Toast.LENGTH_SHORT).show();
						}
							
					}
					else if (currentChild==Const.SCREENS.AlarmSoundUI.ordinal())
					{
						if(!setDefaultSelection(soundSelected))
						{
							Toast.makeText(activity, "No Alarm Sound Selected, Use Default Sound", Toast.LENGTH_SHORT).show();
						}
					}
				}});
		}
	}
	
	private boolean setDefaultSelection(boolean[] selected)
	{
		boolean set = false;
		for(int i=0;i<selected.length;i++)
		{
			if(selected[i]==true)
			{
				set = true;
				return true;
			}
		}
		return set;
	}
	
	private void selectPageIndicator(int pageNo)
	{
		if(pageNo >= 0 && pageNo <this.indicatorLayout.getChildCount())
		{
			((ImageView)this.indicatorLayout.getChildAt(pageNo)).setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.page_select));
		}
	}
	
	private void unSelectPageIndicator(int pageNo)
	{
		if(pageNo >=0 && pageNo < this.indicatorLayout.getChildCount())
		{
			((ImageView)this.indicatorLayout.getChildAt(pageNo)).setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.page_unselect));
		}
	}
	
	private void initSoonzeControls() {
		gesturesView = (GestureOverlayView) activity.findViewById(R.id.gestures);
	}
	// =============================Consumed from otherClasses=============================================
	public void registerListViewItemChangeListener(OnListViewItemChangeListener onListViewItemChangeListener) {
		this.onListViewItemChangeListener = onListViewItemChangeListener;
	}

	public void updateHomeWeatherText(String displayString) {
		hsListAdapter.updateTxtArrayList(displayString, 0);
	}

	//==============================OnSnoozed View ============================
	//private SlideButtonAdapter dismissViewAdapter;
	private SlideButtonAdapter dismissViewAdapter;
	private SlideButton dismissSlide;
	
	public SlideButton getDismissSlide() {
		return dismissSlide;
	}

	
	private String[] dismiss = {"   ","DISMISS"," "};
	
	private void initDismissControls()
	{
		dismissSlide = (SlideButton)activity.findViewById(R.id.dismissSlide);
		dismissViewAdapter = new TextSlideButtonAdapter(dismiss, activity);
		dismissSlide.setViewAdapter(dismissViewAdapter);
		
		dismissSlide.setSlidePosition(-1);
	}
	
	public void showSnoozeView() {

		//initDismissControls();
		
		flipperListView(Const.SCREENS.OnSnoozedUI.ordinal());
		
		//dismissViewAdapter.testTxtSwitcher();
		buttonBarSlidingUpPanel.setVisibility(View.INVISIBLE);
		this.indicatorLayout.setVisibility(View.INVISIBLE);
		//llMainContainer.setOnTouchListener(null);
		bSettingAlarmTimeDisableFlip = true;
	}

	public void updateHomeAlarmText() {
		initAlarmSettings();
		hsListAdapter.updateTxtArrayList(hsDisplayTxt[1], 1);
	}
	
	
	//=======================Exit Dialog===================================//
	private OnExitDialogListener onExitDialogListener;
	private int[] image = {R.drawable.tweet,R.drawable.facebook};
	private String[] socialNetowrks = {"Tweet","Facebook"};
	private AlertDialog alertDialog;

	public void showExitDialog() {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	ListView ls = (ListView) inflater.inflate(R.layout.quit_dialog, (ViewGroup) activity.findViewById(R.id.sharingapps));
       	ExitDialogListAdapter exitDialogListAdapter = new ExitDialogListAdapter();
    	ls.setAdapter(exitDialogListAdapter);
    	ls.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				if(position == 0)
				{
					//alertDialog.dismiss();
					onExitDialogListener.onTwitterSelected();
				}
				else if (position == 1)
				{
					onExitDialogListener.onFacebookSelected();
				}
				
			}});
    	
    	builder.setView(ls);
    	
    	builder.setMessage(Const.DIALOG_TITLE)
    	       .setCancelable(false)
    	       .setPositiveButton(Const.DIALOG_QUIT, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   onExitDialogListener.onExitDialogFinish();
    	           }
    	       })
    	       .setNegativeButton(Const.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	alertDialog = builder.create();
    	alertDialog.show();
    	
	}
	
	public void showSharedMsgDialogBox(String title,String ButtonTxt,final SHARED_METHODS method)
	{
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	final EditText editText = new EditText(context);
    	editText.setText(Const.SHARED_MSG);
    	builder.setView(editText);
    	builder.setMessage(title)
	       .setCancelable(false)
	       .setPositiveButton(ButtonTxt, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   //onExitDialogListener.onExitDialogFinish(true);
	        	   onExitDialogListener.onSharedMsgSend(method,editText.getText().toString());
	           }
	       })
	       .setNegativeButton(Const.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	           }
	       });
    	builder.create().show();
	}

	public void registerExitDialogFinishListener(
			OnExitDialogListener onExitDialogListener) {
		// TODO Auto-generated method stub
		this.onExitDialogListener = onExitDialogListener;
	}
	
	private class ExitDialogListAdapter extends BaseAdapter{
		private LayoutInflater viewInflator;
		
		public ExitDialogListAdapter()
		{
			viewInflator = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return socialNetowrks.length;
		}

		@Override
		public Object getItem(int pos) {
			// TODO Auto-generated method stub
			return socialNetowrks[pos];
		}

		@Override
		public long getItemId(int pos) {
			// TODO Auto-generated method stub
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = viewInflator.inflate(R.layout.dialog_list_item,null);
			}
			ImageView imgV = (ImageView) convertView.findViewById(R.id.socialnetworkicon);
			imgV.setImageResource(image[pos]);
			TextView txtV = (TextView) convertView.findViewById(R.id.socialnetowrktxt);
			txtV.setText(socialNetowrks[pos]);
			
			return convertView;
		}
	}

}
