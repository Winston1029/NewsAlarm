package com.moupress.app.ui.uiControlInterface;

public interface OnAlarmTimeChangeListener {
	
	
	/**
	 * Call Back function when a alarm time in the list is updated
	 * 
	 * @param alarmPosition  alarm position in the list
	 * @param hourOfDay  the hour of updated alarm
	 * @param minute    the minute of updated alarm
	 * @param second    the second of updated alarm
	 * @param millisecond  the millisecond of udpate alarm
	 */
	public void onAlarmTimeChanged(int alarmPosition, int hourOfDay, int minute, int second, int millisecond );
	
	/**
	 * Call back function when a alarm time in the list is selected/unselected
	 * 
	 * @param alarmPosition  alarm position in the list
	 * @param selected  when true, alarm is selected. when false, alarm is unselected
	 * 
	 */
	public void onAlarmTimeSelected(int alarmPosition,boolean selected);
	
}