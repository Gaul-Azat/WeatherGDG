package com.example.azat.weathergdg.entity;


public class CityEntity {

    private String name;

    private String weatherLastUpdate;

    private String dateLastUpdate;

    private String tempOnThisMoment;

    public CityEntity(String name, String weatherLastUpdate, String dateLastUpdate, String tempOnThisMoment) {
        this.name = name;
        this.weatherLastUpdate = weatherLastUpdate;
        this.dateLastUpdate = dateLastUpdate;
        this.tempOnThisMoment = tempOnThisMoment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeatherLastUpdate() {
        return weatherLastUpdate;
    }

    public void setWeatherLastUpdate(String weatherLastUpdate) {
        this.weatherLastUpdate = weatherLastUpdate;
    }

    public String getDateLastUpdate() {
        return dateLastUpdate;
    }

    public void setDateLastUpdate(String dateLastUpdate) {
        this.dateLastUpdate = dateLastUpdate;
    }

    public String getTempOnThisMoment() {
        return tempOnThisMoment;
    }

    public void setTempOnThisMoment(String tempOnThisMoment) {
        this.tempOnThisMoment = tempOnThisMoment;
    }
}
