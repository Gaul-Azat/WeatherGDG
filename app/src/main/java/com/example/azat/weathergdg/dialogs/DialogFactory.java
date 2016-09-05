package com.example.azat.weathergdg.dialogs;


import android.widget.TextView;

import com.example.azat.weathergdg.MainActivity;
import com.example.azat.weathergdg.service.WeatherService;

public class DialogFactory {

    public static AddCityDialog getChangeNameDialog(WeatherService service, MainActivity activity, String appId, String units){
        AddCityDialog dialog=new AddCityDialog();
        dialog.setService(service);
        dialog.setAppId(appId);
        dialog.setMainActivity(activity);
        dialog.setUnits(units);
        return dialog;
    }
}
