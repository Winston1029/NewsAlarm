package com.moupress.app.ui;

/**
 * @author Li Ji
 *
 */
public class NewsAlarmListItem {
	
	private int optionIcon;
	
	private String optionTxt;
	
	private boolean optionSelected;
	
	public NewsAlarmListItem(int optionIcon,String OptionTxt,boolean optionSelected){
		setOptionIcon(optionIcon);
		setOptionTxt(OptionTxt);
		setOptionSelected(optionSelected);
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
