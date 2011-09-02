package com.moupress.app.weather;

public class Location {

    String text;
            
    /**
     *  Create the location.
     *  @param locationText query and the text value.
     */
    public Location (String text) {
        this.text = text;
    }
    
    public Location() {
    	this.text = "";
	}

	//@Override
    public String getQuery() {
        return this.text;
    }

    //@Override
    public String getText() {
        return this.text;
    }
    
    //@Override
    public boolean isEmpty() {
        return this.text == null || this.text.length() == 0;
    }

}
