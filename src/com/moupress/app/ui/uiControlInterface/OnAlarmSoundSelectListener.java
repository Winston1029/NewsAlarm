package com.moupress.app.ui.uiControlInterface;

public interface OnAlarmSoundSelectListener {

	/**
	 * Call back function  when alarm sound is selected/unselected
	 * 
	 * @param alarmSoundPosition alarm sound position in the list
	 * @param selected  when true, alarm sound is selected. when false, alarm sound is unselected
	 * 
	 */
	public void onAlarmSoundSelected(int alarmSoundPosition,boolean selected);
}
