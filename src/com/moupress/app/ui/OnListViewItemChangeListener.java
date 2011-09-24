package com.moupress.app.ui;

public interface OnListViewItemChangeListener {
	/**
	 * Call back function when snooze mode is selected/unselected
	 * 
	 * @param snoozeMode snooze mode position in the list
	 * @param selected  when true, snooze mode is selected. when false, snooze mode is unselected
	 */
	public void onSnoozeModeSelected(int snoozeMode, boolean selected);
	
	/**
	 * Call Back function when a alarm time in the list is updated
	 * 
	 * @param alarmPosition  alarm position in the list
	 * @param hourOfDay  the hour of updated alarm
	 * @param minute    the minute of updated alarm
	 * @param second    the second of updated alarm
	 * @param millisecond  the millisecond of udpate alarm
	 */
	public void onAlarmTimeChanged(int alarmPosition, Boolean selected, int hourOfDay, int minute, int second, int millisecond );
	
	/**
	 * Call back function when a alarm time in the list is selected/unselected
	 * 
	 * @param alarmPosition  alarm position in the list
	 * @param selected  when true, alarm is selected. when false, alarm is unselected
	 * 
	 */
	public void onAlarmTimeSelected(int alarmPosition,boolean selected);
	
	/**
	 * Call back function  when alarm sound is selected/unselected
	 * 
	 * @param alarmSoundPosition alarm sound position in the list
	 * @param selected  when true, alarm sound is selected. when false, alarm sound is unselected
	 * 
	 */
	public void onAlarmSoundSelected(int alarmSoundPosition,boolean selected);

}
