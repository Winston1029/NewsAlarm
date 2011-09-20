package com.moupress.app.ui;

import java.util.ArrayList;
import java.util.Calendar;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.gesture.GestureOverlayView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.moupress.app.Const;
import com.moupress.app.R;
import com.moupress.app.ui.uiControlInterface.OnAlarmSoundSelectListener;
import com.moupress.app.ui.uiControlInterface.OnAlarmTimeChangeListener;
import com.moupress.app.ui.uiControlInterface.OnSnoozeModeSelectListener;
import com.moupress.app.util.DbHelper;

public class UIMgr
{

    Activity activity;

    public UIMgr(Activity activity)
    {
        this.activity = activity;
        if (Const.ISDEBUG)
        {
            initStreamingControls();
            initSoonzeControls();
            initAlarmControls();
            initAlarmTTSControls();
            initWeatherControls();
        }
        else
        {
            initNewUI();
            initSoonzeControls();
        }

    }

    public Button btn_stream;
    public ImageButton btn_play;
    public TextView txv_stream;
    public boolean bIsPlaying;
    public ProgressBar progressBar;

    // public StreammingMgr uiStreamer;

    private void initStreamingControls()
    {
        txv_stream = (TextView) activity.findViewById(R.id.text_streamed);
        btn_stream = (Button) activity.findViewById(R.id.button_stream);

        btn_play = (ImageButton) activity.findViewById(R.id.button_play);
        btn_play.setEnabled(false);
        progressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
    }

    public GestureOverlayView gesturesView;

    private void initSoonzeControls()
    {
        gesturesView = (GestureOverlayView) activity.findViewById(R.id.gestures);
    }

    public Button btn_set = null;
    public Button btn_cel = null;
    public TextView tv = null;

    private void initAlarmControls()
    {
        btn_set = (Button) activity.findViewById(R.id.Button01);
        btn_cel = (Button) activity.findViewById(R.id.Button02);
        tv = (TextView) activity.findViewById(R.id.TextView);
    }

    public Button btnPlay;
    public Button btnPause;
    public Button btnShutdown;

    private void initAlarmTTSControls()
    {
        btnPlay = (Button) activity.findViewById(R.id.btnStart);
        btnPause = (Button) activity.findViewById(R.id.btnPause);
        btnShutdown = (Button) activity.findViewById(R.id.btnShutdown);
    }

    public ImageButton refreshButton;
    public TextView txv_wind;
    public TextView txv_humidity;
    public TextView txv_updatetime;
    public TextView txv_location;

    private void initWeatherControls()
    {
        refreshButton = (ImageButton) activity.findViewById(R.id.refresh_button);

        txv_location = (TextView) activity.findViewById(R.id.location);
        txv_updatetime = (TextView) activity.findViewById(R.id.update_time);
        txv_humidity = (TextView) activity.findViewById(R.id.humidity);
        txv_wind = (TextView) activity.findViewById(R.id.wind);
    }

    // --------------Production UI Component Start Here -------------------
    private static int ALARM_POSITION = 0;
    private String[] AMPM = { "am", "pm" };

    public ViewFlipper alarmInfoViewSlipper;
    public ListView hsListView;
    private HsLVAdapter hsListAdapter;
    public ListView snoozeListView;
    private HsLVAdapter snoozeAdapter;
    public ListView alarmListView;
    private HsLVAdapter alarmAdapter;
    public ListView soundListView;
    private HsLVAdapter soundAdapter;
    public LayoutInflater viewInflator;

    private WheelView hours;
    private WheelView minutes;
    private WheelView amOrpm;

    private NewsAlarmSlidingUpPanel timeSlidingUpPanel;
    private NewsAlarmSlidingUpPanel buttonBarSlidingUpPanel;

    private NewsAlarmSlidingUpPanel.PanelSlidingListener buttonBarSlidingListener = new NewsAlarmSlidingUpPanel.PanelSlidingListener() {

        @Override
        public void onSlidingUpEnd()
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSlidingDownEnd()
        {
            // TODO Auto-generated method stub
            timeSlidingUpPanel.toggle();
        }
    };

    private NewsAlarmSlidingUpPanel.PanelSlidingListener timeWheelSlidingListener = new NewsAlarmSlidingUpPanel.PanelSlidingListener() {

        @Override
        public void onSlidingUpEnd()
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSlidingDownEnd()
        {
            // TODO Auto-generated method stub
            buttonBarSlidingUpPanel.toggle();
        }
    };

    private String[] hsDisplayTxt = { "Rain  10C", "No Alarm Set", "Gesture" };
    private int[] hsDisplayIcon = { R.drawable.world, R.drawable.clock,
            R.drawable.disc };
    private boolean[] hsSelected = { false, false, false };
    private String[] snoozeDisplayTxt = { "Gesture", "Flip", "Swing" };
    private int[] snoozeDisplayIcon = { R.drawable.disc, R.drawable.disc,
            R.drawable.disc };
    private boolean[] snoozeSelected = { true, true, true };
    private String[] alarmDisplayTxt = { "8:00 am", "9:00 am", "10:00 am" };
    private int[] alarmDisplayIcon = { R.drawable.clock, R.drawable.clock,
            R.drawable.clock };
    private boolean[] alarmSelected = { false, false, false };
    private String[] soundDisplayTxt = { "BBC News", "WSJ", "Reminders" };
    private int[] soundDisplayIcon = { R.drawable.radio, R.drawable.radio,
            R.drawable.radio };
    private boolean[] soundSelected = { true, true, true };

    public Button btnHome;
    public Button btnSoonze;
    public Button btnAlarm;
    public Button btnSound;

    private Button btnUpdateTimeOk;
    private Button btnUpdateTimeCancel;

    private OnAlarmSoundSelectListener onAlarmSoundSelectListener;
    private OnAlarmTimeChangeListener onAlarmTimeChangeListener;
    private OnSnoozeModeSelectListener onSnoozeModeSelectListener;

    private void initNewUI()
    {
        initAlarmSettings();

        alarmInfoViewSlipper = (ViewFlipper) activity.findViewById(R.id.optionflipper);
        hsListView = (ListView) activity.findViewById(R.id.hslistview);
        hsListAdapter = new HsLVAdapter(hsDisplayTxt, hsDisplayIcon, hsSelected);
        hsListView.setAdapter(hsListAdapter);
        hsListView.setOnItemClickListener(optionListOnItemClickListener);

        snoozeListView = (ListView) activity.findViewById(R.id.snoozelistview);
        snoozeAdapter = new HsLVAdapter(snoozeDisplayTxt, snoozeDisplayIcon,
                                        snoozeSelected);
        snoozeListView.setAdapter(snoozeAdapter);
        snoozeListView.setOnItemClickListener(optionListOnItemClickListener);

        alarmListView = (ListView) activity.findViewById(R.id.alarmlistview);
        alarmAdapter = new HsLVAdapter(alarmDisplayTxt, alarmDisplayIcon,
                                       alarmSelected);
        alarmListView.setAdapter(alarmAdapter);
        alarmListView.setOnItemClickListener(optionListOnItemClickListener);

        alarmListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                ALARM_POSITION = position;

                buttonBarSlidingUpPanel.toggle();
                // timeSlidingUpPanel.toggle();
                return true;
            }
        });

        soundListView = (ListView) activity.findViewById(R.id.soundlistview);
        soundAdapter = new HsLVAdapter(soundDisplayTxt, soundDisplayIcon,
                                       soundSelected);
        soundListView.setAdapter(soundAdapter);
        soundListView.setOnItemClickListener(optionListOnItemClickListener);

        btnHome = (Button) activity.findViewById(R.id.homebtn);
        btnHome.setOnClickListener(slidingUpPanelButtonBarButtonListener);

        btnSoonze = (Button) activity.findViewById(R.id.snoozebtn);
        btnSoonze.setOnClickListener(slidingUpPanelButtonBarButtonListener);

        btnAlarm = (Button) activity.findViewById(R.id.alarmbtn);
        btnAlarm.setOnClickListener(slidingUpPanelButtonBarButtonListener);

        btnSound = (Button) activity.findViewById(R.id.soundbtn);
        btnSound.setOnClickListener(slidingUpPanelButtonBarButtonListener);

        btnUpdateTimeOk = (Button) activity.findViewById(R.id.timeaddok);
        btnUpdateTimeCancel = (Button) activity.findViewById(R.id.timeaddcancel);
        btnUpdateTimeOk.setOnClickListener(slidingUpPanelTimeUpdateButtonListener);
        btnUpdateTimeCancel.setOnClickListener(slidingUpPanelTimeUpdateButtonListener);

        timeSlidingUpPanel = (NewsAlarmSlidingUpPanel) activity.findViewById(R.id.timeupdatepanel);
        buttonBarSlidingUpPanel = (NewsAlarmSlidingUpPanel) activity.findViewById(R.id.removeItemPanel);
        buttonBarSlidingUpPanel.setPanelSlidingListener(buttonBarSlidingListener);
        timeSlidingUpPanel.setPanelSlidingListener(timeWheelSlidingListener);

        hours = (WheelView) activity.findViewById(R.id.wheelhour);
        minutes = (WheelView) activity.findViewById(R.id.wheelminute);
        amOrpm = (WheelView) activity.findViewById(R.id.wheelsecond);

        hours.setViewAdapter(new NumericWheelAdapter(activity, 0, 12));
        minutes.setViewAdapter(new NumericWheelAdapter(activity, 0, 59, "%02d"));
        amOrpm.setViewAdapter(new ArrayWheelAdapter<String>(activity, AMPM));
    }

    // alarms
    private void initAlarmSettings()
    {
        DbHelper helper = new DbHelper(this.activity);
        Calendar cal = Calendar.getInstance();
        int hours, mins;
        int nextAlarmPosition = -1;
        long nextAlarm = 0;
        // alarm Time
        for (int i = 0; i < alarmDisplayTxt.length; i++)
        {
            alarmSelected[i] = helper.GetBool(DbHelper.ISALARMSET
                    + Integer.toString(i));

            hours = helper.GetInt(DbHelper.Hours + Integer.toString(i));
            mins = helper.GetInt(DbHelper.Mins + Integer.toString(i));
            cal.setTimeInMillis(System.currentTimeMillis());
            if (hours != DbHelper.DefNum && mins != DbHelper.DefNum)
            {
                cal.set(Calendar.HOUR_OF_DAY, hours);

                cal.set(Calendar.MINUTE, mins);
            }
            hours = cal.get(Calendar.HOUR);
            mins = cal.get(Calendar.MINUTE);
            switch (cal.get(Calendar.AM_PM))
            {
                case Calendar.AM:
                    alarmDisplayTxt[i] = Integer.toString(hours) + ":"
                            + String.format("%02d", mins) + " " + this.AMPM[0];
                    break;
                case Calendar.PM:
                    alarmDisplayTxt[i] = Integer.toString(hours) + ":"
                            + String.format("%02d", mins) + " " + this.AMPM[1];
                    break;
                default:
                    break;
            }

            if (alarmSelected[i])
            {
                long tmp = cal.getTimeInMillis();
                if (tmp > System.currentTimeMillis())
                    tmp += AlarmManager.INTERVAL_DAY;
                if (nextAlarm != 0)
                {
                    if (nextAlarm > tmp)
                    {
                        nextAlarm = tmp;
                        nextAlarmPosition = i;
                    }
                }
                else
                {
                    nextAlarm = tmp;
                    nextAlarmPosition = i;
                }

            }
        }
        hsDisplayTxt[1] = alarmDisplayTxt[nextAlarmPosition];

    }
    public void notifyAlarmChange()
    {
        initAlarmSettings();
        hsListAdapter.updateTxtArrayList(hsDisplayTxt[1], 1);
        //Toast.makeText(this.activity, hsDisplayTxt[1], Toast.LENGTH_LONG).show();
    }
    AdapterView.OnItemClickListener optionListOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id)
        {

            switch (parent.getId())
            {
                case R.id.snoozelistview:
                    revertSelectListItem(snoozeAdapter, snoozeSelected,
                            position);
                    // Call back function for Snooze Mode selected/unselected
                    onSnoozeModeSelectListener.onSnoozeModeSelected(position,
                            snoozeSelected[position]);
                    break;
                case R.id.soundlistview:
                    revertSelectListItem(soundAdapter, soundSelected, position);
                    // Call back function for Alarm Sound selected/unselected
                    onAlarmSoundSelectListener.onAlarmSoundSelected(position,
                            soundSelected[position]);
                    break;
                case R.id.alarmlistview:
                    revertSelectListItem(alarmAdapter, alarmSelected, position);
                    // Call back function for alarm time selected/unselected
                    onAlarmTimeChangeListener.onAlarmTimeSelected(position,
                            alarmSelected[position]);
                    break;
                case R.id.hslistview:
                    hsListViewClicked(position);
            }
        }
    };

    Button.OnClickListener slidingUpPanelTimeUpdateButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v)
        {

            switch (v.getId())
            {
                case R.id.timeaddok:
                    alarmAdapter.updateTxtArrayList(
                            ""
                                    + hours.getCurrentItem()
                                    + ":"
                                    + String.format("%02d",
                                            minutes.getCurrentItem())
                                    + " "
                                    + (amOrpm.getCurrentItem() == 0 ? "am"
                                            : "pm"), ALARM_POSITION);
                    timeSlidingUpPanel.toggle();
                    // Call Back function on Alarm Time Change
                    int hours24 = amOrpm.getCurrentItem() == 0 ? hours.getCurrentItem()
                            : hours.getCurrentItem() + 12;
                    onAlarmTimeChangeListener.onAlarmTimeChanged(
                            ALARM_POSITION, alarmSelected[ALARM_POSITION],
                            hours24, minutes.getCurrentItem(), 0, 0);
                    break;
                case R.id.timeaddcancel:
                    timeSlidingUpPanel.toggle();
                    break;
            };
        }

    };

    Button.OnClickListener slidingUpPanelButtonBarButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v)
        {
            switch (v.getId())
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
                default:// no match
                    System.out.println("btn is from nowhere.");
            }
        }
    };

    public void registerAlarmSoundSelectListener(
            OnAlarmSoundSelectListener onAlarmSoundSelectListener)
    {
        this.onAlarmSoundSelectListener = onAlarmSoundSelectListener;
    }

    public void registerAlarmTimeChangeListener(
            OnAlarmTimeChangeListener onAlarmTimeChangeListener)
    {
        this.onAlarmTimeChangeListener = onAlarmTimeChangeListener;
    }

    public void registerSnoozeModeSelectListener(
            OnSnoozeModeSelectListener onSnoozeModeSelectListener)
    {
        this.onSnoozeModeSelectListener = onSnoozeModeSelectListener;
    }

    public void revertSelectListItem(HsLVAdapter listAdapter, boolean[] chked,
            int pos)
    {
        chked[pos] = !chked[pos];
        listAdapter.invertSelect(pos);
        listAdapter.notifyDataSetChanged();
    }

    protected void flipperListView(int toDisplayedChild)
    {
        if (alarmInfoViewSlipper.getDisplayedChild() > toDisplayedChild)
        {
            alarmInfoViewSlipper.setInAnimation(activity, R.anim.slidein);
            alarmInfoViewSlipper.setOutAnimation(activity, R.anim.slideout);
            alarmInfoViewSlipper.setDisplayedChild(toDisplayedChild);
        }
        else if (alarmInfoViewSlipper.getDisplayedChild() < toDisplayedChild)
        {
            alarmInfoViewSlipper.setInAnimation(activity,
                    R.anim.slideinfromright);
            alarmInfoViewSlipper.setOutAnimation(activity,
                    R.anim.slideouttoleft);
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

    public void showSnoozeView()
    {
        flipperListView(4);
    }

    public void updateWeatherUI(String displayString)
    {
        hsListAdapter.updateTxtArrayList(displayString, 0);
    }

    private void hsListViewClicked(int position)
    {
        switch (position)
        {
            case 0:
                // display weather info
                break;
            case 1:
                alarmBtnClick();
                break;
            case 2:
                snoozeBtnClick();
                break;
        }
    }

    private class HsLVAdapter extends BaseAdapter
    {
        // private String[] txtisplays ;
        // private int[] icons ;

        private ArrayList<NewsAlarmListItem> optionArrayList;

        public HsLVAdapter(String[] displayStrings, int[] displayInts,
                boolean[] displayChecked)
        {
            viewInflator = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            optionArrayList = new ArrayList<NewsAlarmListItem>();
            loadArrayList(displayStrings, displayInts, displayChecked);
            // txtisplays = displayStrings;
            // icons = displayInts;
        }

        public void loadArrayList(String[] displayStrings, int[] displayInts,
                boolean[] displayChecked)
        {
            for (int i = 0; i < displayStrings.length; i++)
            {
                addToArrayList(displayStrings[i], displayInts[i],
                        displayChecked[i]);
            }
        }

        public void addToArrayList(String displayString, int displayInt,
                boolean displayChk)
        {

            optionArrayList.add(new NewsAlarmListItem(displayInt,
                                                      displayString, displayChk));
        }

        public void updateTxtArrayList(String displayString, int position)
        {
            if (displayString.length() == 0)
            {
                optionArrayList.get(position).setOptionTxt(
                        Const.NON_WEATHER_MSG);
            }
            else
                optionArrayList.get(position).setOptionTxt(displayString);
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount()
        {
            return optionArrayList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return optionArrayList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = viewInflator.inflate(R.layout.home_screen_item,
                        null);

            }
            ImageView imgView = (ImageView) convertView.findViewById(R.id.alarmitemicon);
            imgView.setImageResource(optionArrayList.get(position).getOptionIcon());
            TextView textView = (TextView) convertView.findViewById(R.id.alarmitemtxt);
            textView.setText(optionArrayList.get(position).getOptionTxt());
            ImageView chkImgView = (ImageView) convertView.findViewById(R.id.checked);
            chkImgView.setImageResource(R.drawable.checkbtn);
            if (optionArrayList.get(position).isOptionSelected())
            {

                chkImgView.setVisibility(View.VISIBLE);
            }
            else
            {
                chkImgView.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        public void invertSelect(int position)
        {
            optionArrayList.get(position).setOptionSelected(
                    !optionArrayList.get(position).isOptionSelected());
        }
    }
}
