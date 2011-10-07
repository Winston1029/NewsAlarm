package com.moupress.app.ui;

/**
 * @author Li Ji
 *
 */
public class NewsAlarmListItem {
	
	private int optionIcon;
	
	private String optionTxt;
	
	private boolean optionSelected;
	
	private boolean[] weekDaysSelection;
	
	public NewsAlarmListItem(int optionIcon,String OptionTxt,boolean optionSelected, boolean[] weekDaysSelection){
		setOptionIcon(optionIcon);
		setOptionTxt(OptionTxt);
		setOptionSelected(optionSelected);
		setWeekDaysSelection(weekDaysSelection);
	}

	public boolean[] getWeekDaysSelection() {
		return weekDaysSelection;
	}

	public void setWeekDaysSelection(boolean[] weekDaysSelection) {
		this.weekDaysSelection = weekDaysSelection;
	}

	public int getOptionIcon() {
		return optionIcon;
	}

	public void setOptionIcon(int optionIcon) {
		this.optionIcon = optionIcon;
	}

	public String getOptionTxt() {
		return optionTxt;
	}

	public void setOptionTxt(String optionTxt) {
		this.optionTxt = optionTxt;
	}

	public boolean isOptionSelected() {
		return optionSelected;
	}

	public void setOptionSelected(boolean optionSelected) {
		this.optionSelected = optionSelected;
	}

}
