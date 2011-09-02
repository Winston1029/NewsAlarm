package com.moupress.app.weather;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  Define all weather related data types
 */
public class Weather {
    
    public enum UnitSystem {
        SI, US;
    }
    
    Location location;
    Date time;
    UnitSystem unit;
    List<WeatherCondition> conditions;
    
    public Weather() {
    	location = new Location("");
    	unit = UnitSystem.SI;
    	conditions = new ArrayList<WeatherCondition>();
    }

    public void setLocation(Location location) {this.location = location;}
    public void setTime(Date time) {this.time = time;}
    public void setUnitSystem(UnitSystem unit) {this.unit = unit;}
    public void setConditions(List<WeatherCondition> conditions) {this.conditions = conditions;}
    
    public Location getLocation() {return this.location;}
    public Date getTime() {return this.time;}
    public UnitSystem getUnitSystem() {return this.unit;}
    public List<WeatherCondition> getConditions() {
        if (this.conditions == null) {
            return new ArrayList<WeatherCondition>();
        }
        return this.conditions;
    }
    
    public boolean isEmpty() {
        if (this.time == null || this.time.getTime() == 0) {
            return true;
        }
        if (this.conditions == null || this.conditions.size() == 0) {
            return true;
        }
        return false;
    }
    
    public class WeatherCondition {

        String conditionText;
        Temperature temperature;
        String windText;
        String humidityText;
        
        /**
         *  Sets the condition text.
         */
        public void setConditionText(String text) {
            this.conditionText = text;
        }
        
        /**
         *  Sets the temperature.
         */
        public void setTemperature(Temperature temp) {
            this.temperature = temp;
        }
        
        /**
         *  Sets the wind text.
         */
        public void setWindText(String text) {
            this.windText = text;
        }
        
        /**
         *  Sets the humidity text.
         */
        public void setHumidityText(String text) {
            this.humidityText = text;
        }
        
        //@Override
        public String getConditionText() {
            return this.conditionText;
        }

        //@Override
        public Temperature getTemperature() {
            return this.temperature;
        }

        //@Override
        public Temperature getTemperature(UnitSystem unit) {
            if (this.temperature == null) {
                return null;
            }
            if (this.temperature.getUnitSystem().equals(unit)) {
                return this.temperature;
            }
            return this.temperature.convert(unit);
        }

        //@Override
        public String getWindText() {
            return this.windText;
        }
        
        //@Override
        public String getHumidityText() {
            return this.humidityText;
        }

    }
    
    public class Temperature {

    	/** Unknown temperature value */
        final static int UNKNOWN = Integer.MIN_VALUE;
        
        UnitSystem unit;
        int current = UNKNOWN;
        int low = UNKNOWN;
        int high = UNKNOWN;
        
        public Temperature (UnitSystem unit) {
            this.unit = unit;
        }
        public void setCurrent(int temp, UnitSystem unit) {
            if (this.unit.equals(unit)) {
                this.current = temp;
            } else {
                this.current = convertValue(temp, unit);
            }
        }
        public void setLow(int temp, UnitSystem unit) {
            if (this.unit.equals(unit)) {
                this.low = temp;
            } else {
                this.low = convertValue(temp, unit);
            }
        }
        public void setHigh(int temp, UnitSystem unit) {
            if (this.unit.equals(unit)) {
                this.high = temp;
            } else {
                this.high = convertValue(temp, unit);
            }
        }
        
        public int getCurrent() {
            if (this.current == UNKNOWN) {
                return Math.round((getLow() + getHigh()) / 2f); 
            }
            return this.current;
        }

        public int getHigh() {
            if (this.high == UNKNOWN) {
                if (this.current == UNKNOWN) {
                    return this.low;
                } else {
                    return this.current;
                }
            }
            return this.high;
        }

        public int getLow() {
            if (this.low == UNKNOWN) {
                if (this.current == UNKNOWN) {
                    return this.high;
                } else {
                    return this.current;
                }
            }
            return this.low;
        }

        public UnitSystem getUnitSystem() {
            return this.unit;
        }
        
        /**
         *  Creates new temperature in another unit system.
         */
        public Temperature convert(UnitSystem unit) {
            Temperature result = new Temperature(unit);
            result.setCurrent(this.getCurrent(), this.getUnitSystem());
            result.setLow(this.getLow(), this.getUnitSystem());
            result.setHigh(this.getHigh(), this.getUnitSystem());
            return result;
        }
        
        /**
         *  Converts the value from provided unit system into this temperature set unit system.
         */
        int convertValue(int value, UnitSystem unit) {
            if (this.unit.equals(unit)) {
                return value;
            }   
            if (UnitSystem.SI.equals(unit)) {   //SI -> US
                return Math.round(value * 9f / 5f + 32);
            } else {    //US -> SI
                return Math.round((value - 32) * 5f / 9f);
            }
        }
    }

}


