package com.example.azat.weathergdg.adapter;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.databinding.DataBindingUtil;
import android.view.ViewGroup;

import com.example.azat.weathergdg.MainActivity;
import com.example.azat.weathergdg.WeatherScrollingActivity;
import com.example.azat.weathergdg.databinding.CityCardBinding;
import com.example.azat.weathergdg.db.DatabaseHelper;
import com.example.azat.weathergdg.entity.CityEntity;
import com.example.azat.weathergdg.entity.weatherElements.WeatherMeta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;

public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.CityCardHolder>{

    Cursor dataCursor;
    MainActivity activity;
    Gson gson;
    public CityListAdapter(Cursor dataCursor, MainActivity activity) {
        this.dataCursor = dataCursor;
        this.activity = activity;
        GsonBuilder builder = new GsonBuilder();
        builder.setLenient();
        gson = builder.create();
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
        notifyItemRangeChanged(0,getItemCount());
    }

    private Cursor swapCursor(Cursor cursor) {
        if (dataCursor == cursor) {
            return null;
        }
        Cursor oldCursor = dataCursor;
        this.dataCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    private CityEntity getItem(int position) {
        dataCursor.moveToPosition(position);
        // Load data from dataCursor and return it...
        String cityName=dataCursor.getString(dataCursor.getColumnIndex(DatabaseHelper.COLUMN_CITY));
        String weatherLastUpdate=dataCursor.getString(dataCursor.getColumnIndex(DatabaseHelper.COLUMN_OLD_WEATHER));
        String dateLastUpdate=dataCursor.getString(dataCursor.getColumnIndex(DatabaseHelper.COLUMN_DATE_OLD_WEATHER));

        WeatherMeta weatherMeta=gson.fromJson(weatherLastUpdate,WeatherMeta.class);
        return new CityEntity(cityName,weatherLastUpdate,dateLastUpdate,weatherMeta.getTempOnThisMoment());
    }

    @Override
    public CityCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        CityCardBinding binding=CityCardBinding.inflate(inflater, parent, false);
        return new CityCardHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(CityCardHolder holder, int position) {
        CityEntity city=getItem(position);
        holder.binding.setCity(city);
        holder.binding.cityCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, WeatherScrollingActivity.class);
                intent.putExtra("cityName",city.getName());
                intent.putExtra("oldWeather",city.getWeatherLastUpdate());
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        try {
            if (dataCursor == null || dataCursor.isClosed()) {
                return 0;
            } else {
                return dataCursor.getCount();
            }}
        catch (Throwable e){
                return 0;
            }

    }
    public static class CityCardHolder extends RecyclerView.ViewHolder{

        CityCardBinding binding;
        public CityCardHolder(View itemView) {
            super(itemView);
            binding= DataBindingUtil.bind(itemView);
        }
    }


}
