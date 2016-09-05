package com.example.azat.weathergdg;



import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.example.azat.weathergdg.entity.weatherElements.WeatherMeta;

public class SavingFragment extends Fragment {

    WeatherMeta weatherMeta;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    public WeatherMeta getWeatherMeta() {
        return weatherMeta;
    }

    public void setWeatherMeta(WeatherMeta weatherMeta) {
        this.weatherMeta = weatherMeta;
    }
}
