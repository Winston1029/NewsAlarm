package com.moupress.app.weather;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  Parse XML info to Weather, Data Provided by Google Weather API
 */
public class GoogleWeather extends Weather {

    /** Format for dates in the XML */
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    /** Format for times in the XML */
    static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    
    Date date = new Date(0);
    Date time = new Date(0);
    
    // Retrieve API data
    static final String API_URL = "http://www.google.com/ig/api?weather=%s&hl=%s"; /** API URL */
    static final String ENCODING = "UTF-8"; /** Main encoding */
    static final String CHARSET = "charset="; /** Charset pattern */
    
    /**
     *  Creates the weather from the input stream with XML
     *  received from API.
     */
    public GoogleWeather(){
    	super();
    }
    
    public GoogleWeather(Reader xml) throws WeatherException {
    	super();
    	try {
            parse(xml);
        } catch (Exception e) {
            throw new WeatherException("cannot parse xml", e);
        }
    }
    
    public Date getTime() {
        if (this.time.after(this.date)) {   //sometimes time is 0, but the date has correct value
            return this.time;
        } else {
            return this.date;
        }
    }
    
    public void parse(Reader xml) {
            //throws SAXException, ParserConfigurationException, IOException {
    	try {
	        SAXParserFactory factory = SAXParserFactory.newInstance();
	        factory.setNamespaceAware(true);   //WTF??? Harmony's Expat is so...
	        //factory.setFeature("http://xml.org/sax/features/namespaces", false);
	        //factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
	        SAXParser parser = factory.newSAXParser();
	        //explicitly decoding from UTF-8 because Google misses encoding in XML preamble
	        parser.parse(new InputSource(xml), new ApiXmlHandler());
    	} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public Weather query(Location location, Locale locale) throws WeatherException {
		String fullUrl;
		try {
		    fullUrl = String.format(API_URL, 
		            URLEncoder.encode(location.getQuery(), ENCODING),
		            URLEncoder.encode(locale.getLanguage(), ENCODING));
		} catch (UnsupportedEncodingException uee) {
		    throw new RuntimeException(uee);    //should never happen
		}
		URL url;
		try {
		    url = new URL(fullUrl);
		} catch (MalformedURLException mue) {
		    throw new WeatherException("invalid URL: " + fullUrl, mue);
		}
		String charset = ENCODING;
		try {
		    URLConnection connection = url.openConnection();
		    //connection.addRequestProperty("Accept-Charset", "UTF-8");
			//connection.addRequestProperty("Accept-Language", locale.getLanguage());
			charset = getCharset(connection);
			//GoogleWeather weather = new GoogleWeather(new InputStreamReader(
			//        connection.getInputStream(), charset));
			parse(new InputStreamReader(connection.getInputStream(), charset));
			if (getLocation().isEmpty()) {
			    setLocation(location);  //set original location
		    }
		    return this; 
		} catch (UnsupportedEncodingException uee) {
		    throw new WeatherException("unsupported charset: " + charset, uee);
		} catch (IOException ie) {
		    throw new WeatherException("cannot read URL: " + fullUrl, ie);
		} 
	}
    
    private String getCharset(URLConnection connection) {
    	String contentType = connection.getContentType();
    	if (contentType == null) {
            return ENCODING;
        }
        int charsetPos = contentType.indexOf(CHARSET);
        if (charsetPos < 0) {
            return ENCODING;
        }
        charsetPos += CHARSET.length();
        int endPos = contentType.indexOf(';', charsetPos);
        if (endPos < 0) {
            endPos = contentType.length();
        }
        return contentType.substring(charsetPos, endPos);
    }
    
    static enum HandlerState {
        CURRENT_CONDITIONS, FIRST_FORECAST, NEXT_FORECAST;
    }
    
    class ApiXmlHandler extends DefaultHandler {
        
        HandlerState state;
        WeatherCondition condition;
        Temperature temperature;
        
        @Override
        public void startElement(String uri, String localName,
                String qName, Attributes attributes) throws SAXException {
            String data = attributes.getValue("data");
            if ("city".equals(localName)) {
                GoogleWeather.this.location = new Location(data);
            } else if ("forecast_date".equals(localName)) {
                try {
                    GoogleWeather.this.date = DATE_FORMAT.parse(data);
                } catch (ParseException e) {
                    throw new SAXException("invalid 'forecast_date' format: " + data, e);
                }
            } else if ("current_date_time".equals(localName)) {
                try {
                    GoogleWeather.this.time = TIME_FORMAT.parse(data);
                } catch (ParseException e) {
                    throw new SAXException("invalid 'current_date_time' format: " + data, e);
                }
            } else if ("unit_system".equals(localName)) {
                GoogleWeather.this.unit = UnitSystem.valueOf(data);
            } else if ("current_conditions".equals(localName)) {
                state = HandlerState.CURRENT_CONDITIONS;
                addCondition();
            } else if ("forecast_conditions".equals(localName)) {
                switch (state) {
                case CURRENT_CONDITIONS:
                    state = HandlerState.FIRST_FORECAST;
                    break;
                case FIRST_FORECAST:
                    state = HandlerState.NEXT_FORECAST;
                    addCondition();
                    break;
                default:
                    addCondition();
                }
            } else if ("condition".equals(localName)) {
                switch (state) {
                case FIRST_FORECAST:
                    //skipping update of condition, because the current conditions are already set
                    break;
                default:
                    condition.setConditionText(data);
                }
            } else if ("temp_f".equalsIgnoreCase(localName)) {
                if (UnitSystem.US.equals(GoogleWeather.this.unit)) {
                    try {
                        temperature.setCurrent(Integer.parseInt(data), UnitSystem.US);
                    } catch (NumberFormatException e) {
                        throw new SAXException("invalid 'temp_f' format: " + data, e);
                    }
                }
            } else if ("temp_c".equals(localName)) {
                if (UnitSystem.SI.equals(GoogleWeather.this.unit)) {
                    try {
                        temperature.setCurrent(Integer.parseInt(data), UnitSystem.SI);
                    } catch (NumberFormatException e) {
                        throw new SAXException("invalid 'temp_c' format: " + data, e);
                    }
                }
            } else if ("humidity".equals(localName)) {
                condition.setHumidityText(data);
            } else if ("wind_condition".equals(localName)) {
                condition.setWindText(data);
            } else if ("low".equals(localName)) {
                try {
                    temperature.setLow(Integer.parseInt(data), GoogleWeather.this.unit);
                } catch (NumberFormatException e) {
                    throw new SAXException("invalid 'low' format: " + data, e);
                }
            } else if ("high".equals(localName)) {
                try {
                    temperature.setHigh(Integer.parseInt(data), GoogleWeather.this.unit);
                } catch (NumberFormatException e) {
                    throw new SAXException("invalid 'high' format: " + data, e);
                }
            }
        }
        
        //@Override
        //public void endElement(String uri, String localName, String qName)
        //        throws SAXException {
        //    boolean dummy = true;
        //}
        
        void addCondition() {
            condition = new WeatherCondition();
            temperature = new Temperature(GoogleWeather.this.unit);
            condition.setTemperature(temperature);
            GoogleWeather.this.conditions.add(condition);
        }

    }

}
