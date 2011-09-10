package com.moupress.app.ui.uiControlInterface;

public interface OnSnoozeModeSelectListener {

	/**
	 * Call back function when snooze mode is selected/unselected
	 * 
	 * @param snoozeMode snooze mode position in the list
	 * @param selected  when true, snooze mode is selected. when false, snooze mode is unselected
	 */
	public void onSnoozeModeSelected(int snoozeMode, boolean selected);
}
