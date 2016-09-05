
package com.example.azat.weathergdg.entity.weatherElements;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WeatherMeta {

    @SerializedName("city")
    @Expose
    private City city;
    @SerializedName("cod")
    @Expose
    private String cod;
    @SerializedName("message")
    @Expose
    private Double message;
    @SerializedName("cnt")
    @Expose
    private Integer cnt;
    @SerializedName("list")
    @Expose
    private List<WeatherOfHours> weatherOfHours = new ArrayList<WeatherOfHours>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public WeatherMeta() {
    }

    /**
     * 
     * @param message
     * @param cnt
     * @param cod
     * @param weatherOfHours
     * @param city
     */
    public WeatherMeta(City city, String cod, Double message, Integer cnt, java.util.List<WeatherOfHours> weatherOfHours) {
        this.city = city;
        this.cod = cod;
        this.message = message;
        this.cnt = cnt;
        this.weatherOfHours = weatherOfHours;
    }

    /**
     * 
     * @return
     *     The city
     */
    public City getCity() {
        return city;
    }

    /**
     * 
     * @param city
     *     The city
     */
    public void setCity(City city) {
        this.city = city;
    }

    /**
     * 
     * @return
     *     The cod
     */
    public String getCod() {
        return cod;
    }

    /**
     * 
     * @param cod
     *     The cod
     */
    public void setCod(String cod) {
        this.cod = cod;
    }

    /**
     * 
     * @return
     *     The message
     */
    public Double getMessage() {
        return message;
    }

    /**
     * 
     * @param message
     *     The message
     */
    public void setMessage(Double message) {
        this.message = message;
    }

    /**
     * 
     * @return
     *     The cnt
     */
    public Integer getCnt() {
        return cnt;
    }

    /**
     * 
     * @param cnt
     *     The cnt
     */
    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    /**
     * 
     * @return
     *     The weatherOfHours
     */
    public java.util.List<WeatherOfHours> getWeatherOfHours() {
        return weatherOfHours;
    }

    /**
     * 
     * @param weatherOfHours
     *     The weatherOfHours
     */
    public void setWeatherOfHours(java.util.List<WeatherOfHours> weatherOfHours) {
        this.weatherOfHours = weatherOfHours;
    }


    public String getTempOnThisMoment(){
        int i=0;
        long currentTime=System.currentTimeMillis();
        while((i<weatherOfHours.size())&&(currentTime>weatherOfHours.get(i).getDt())) {
            i++;
        }
        if (i>=weatherOfHours.size())
        {
            return "";
        }
        return weatherOfHours.get(i).getMain().getTemp()+"°C";
    }

}
