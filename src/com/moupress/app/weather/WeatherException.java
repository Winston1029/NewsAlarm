package com.moupress.app.weather;

/**
 *  Common exception for weather getting errors.
 */
public class WeatherException extends Exception {

    private static final long serialVersionUID = -7139823134945463091L;

    public WeatherException() {
        super();
    }

    public WeatherException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public WeatherException(String detailMessage) {
        super(detailMessage);
    }

    public WeatherException(Throwable throwable) {
        super(throwable);
    }

}
