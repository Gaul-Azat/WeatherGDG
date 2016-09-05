package com.example.azat.weathergdg.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.azat.weathergdg.BuildConfig;
import com.example.azat.weathergdg.MainActivity;
import com.example.azat.weathergdg.R;
import com.example.azat.weathergdg.service.WeatherService;

import java.io.IOException;
import java.util.Date;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddCityDialog extends DialogFragment{

    WeatherService service;
    String appId;
    String units;
    MainActivity mainActivity;
    EditText addCityEdit;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b=  new  AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_city_message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Observable<ResponseBody> getWeather=service.getWeather5Day(addCityEdit.getText().toString(),appId,units);
                                getWeather.subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(current-> {
                                            try {
                                                addCityToDb(current.string());
                                                dialog.dismiss();
                                            }
                                            catch (IOException e){
                                                if (BuildConfig.DEBUG)
                                                    Log.d("Error",e.getMessage());
                                            }
                                        },e->{mainActivity.netError(e);
                                        });
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );

        LayoutInflater i = getActivity().getLayoutInflater();

        View v = i.inflate(R.layout.add_city_dialog,null);
        addCityEdit=(EditText)v.findViewById(R.id.addCityEdit);
        b.setView(v);
        return b.create();
    }

    private void addCityToDb(String weatherMeta){
        if(getCityNameFromRespond(weatherMeta)==null){
            return;
        }
        mainActivity.addCityToDb(getCityNameFromRespond(weatherMeta),weatherMeta, new Date(System.currentTimeMillis()));
    }


    public void setService(WeatherService service) {
        this.service = service;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getCityNameFromRespond(String respond){
        int i=respond.indexOf(",",0);
        i =respond.indexOf(",",i+1)-1;
        int l=respond.indexOf("name")+7;
        if (l>i) {
            mainActivity.cityDontExistsOnServer();
            return null;
        }
        return respond.substring(l,i);
    }
}
