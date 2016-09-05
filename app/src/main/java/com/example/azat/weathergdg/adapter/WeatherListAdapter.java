package com.example.azat.weathergdg.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.azat.weathergdg.R;
import com.example.azat.weathergdg.databinding.CityCardBinding;
import com.example.azat.weathergdg.databinding.WeatherCardBinding;
import com.example.azat.weathergdg.entity.weatherElements.WeatherOfDay;
import com.example.azat.weathergdg.entity.weatherElements.WeatherOfHours;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.WeatherCardHolder>{

    List<WeatherOfDay> weatherOfDays;

    public WeatherListAdapter(List<WeatherOfDay> weatherOfDays) {
        this.weatherOfDays = weatherOfDays;
    }

    public void setWeatherOfDays(List<WeatherOfDay> weatherOfDays) {
        this.weatherOfDays = weatherOfDays;
        notifyItemRangeChanged(0,getItemCount());
    }

    @Override
    public WeatherCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        WeatherCardBinding binding=WeatherCardBinding.inflate(inflater, parent, false);
        return new WeatherCardHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(WeatherCardHolder holder, int position) {
        WeatherOfDay weatherOfDay=weatherOfDays.get(position);
        holder.binding.setWeather(weatherOfDay.getWeatherOfHours().get(0));
        int i=0;
        int count=weatherOfDay.getWeatherOfHours().size();
        if(holder.binding.weatherTimeList.getChildCount()>0)
        {
            holder.binding.weatherTimeList.removeAllViews();
        }
        for(;i<count;i+=2){
            View v=LayoutInflater.from(holder.binding.weatherTimeList.getContext()).inflate(R.layout.weather_time_card,holder.binding.weatherTimeList,false);
            TextView weatherTime=(TextView)v.findViewById(R.id.weather_time);
            ImageView imageView=(ImageView)v.findViewById(R.id.weather_time_ic);
            TextView weatherTimeTemp=(TextView)v.findViewById(R.id.weather_time_temp);
            WeatherOfHours entity=weatherOfDay.getWeatherOfHours().get(i);
            SimpleDateFormat format=new SimpleDateFormat("HH");
            String time=format.format(new Date(entity.getDt()))+":00";
            weatherTime.setText(time);
            weatherTimeTemp.setText(entity.getMain().getTemp()+"°C");
            int iconResourceId=imageView.getContext().getResources().getIdentifier("i"+entity.getFirstWeather().getIcon(),"drawable",imageView.getContext().getPackageName());
            Picasso.with(imageView.getContext()).load(iconResourceId).resize(150,150).into(imageView);
            holder.binding.weatherTimeList.addView(v);
        }
    }

    @Override
    public int getItemCount() {
        return (weatherOfDays==null)?0:weatherOfDays.size();
    }

    public static class WeatherCardHolder extends RecyclerView.ViewHolder{
        WeatherCardBinding binding;
        public WeatherCardHolder(View itemView) {
            super(itemView);
            binding= DataBindingUtil.bind(itemView);
        }
    }
}
