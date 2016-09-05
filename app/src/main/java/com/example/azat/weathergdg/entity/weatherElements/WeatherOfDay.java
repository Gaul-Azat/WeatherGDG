package com.example.azat.weathergdg.entity.weatherElements;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WeatherOfDay {

    List<WeatherOfHours> weatherOfHours;

    public WeatherOfDay(List<WeatherOfHours> weatherEntities) {
        this.weatherOfHours = weatherEntities;
    }

    public static List<WeatherOfDay> fromListWeatherEntitiesStartingFromThisMoment(List<WeatherOfHours> list){
        int i=0;
        long currentTime=System.currentTimeMillis();
        while((i<list.size())&&(currentTime>list.get(i).getDt())) {
            i++;
        }
        if (i>=list.size())
        {
            return null;
        }
        List<WeatherOfDay> weatherOfDays =new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(list.get(i).getDt()));
        Integer date = cal.get(Calendar.DATE);
        while (i<list.size()){
            List<WeatherOfHours> weatherOfDay=new ArrayList<>();
            weatherOfDay.add(list.get(i));
            i++;
            boolean b=true;
            while(b){
                cal.setTime(new Date(list.get(i).getDt()));
                Integer t = cal.get(Calendar.DATE);
                if(t==date){
                    weatherOfDay.add(list.get(i));
                    i++;
                }else{
                    weatherOfDays.add(new WeatherOfDay(weatherOfDay));
                    b=false;
                    date=t;
                }
                if (i+1>=list.size()){
                    i++;
                    b=false;
                    weatherOfDays.add(new WeatherOfDay(weatherOfDay));
                }
            }
        }
        return weatherOfDays;
    }

    public List<WeatherOfHours> getWeatherOfHours() {
        return weatherOfHours;
    }

    public void setWeatherOfHours(List<WeatherOfHours> weatherOfHours) {
        this.weatherOfHours = weatherOfHours;
    }
}
