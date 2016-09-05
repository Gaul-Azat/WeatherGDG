package com.example.azat.weathergdg;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.azat.weathergdg.adapter.WeatherListAdapter;
import com.example.azat.weathergdg.adapter.WrapContentLinearLayoutManager;
import com.example.azat.weathergdg.db.CityDatabase;
import com.example.azat.weathergdg.db.DatabaseHelper;
import com.example.azat.weathergdg.entity.weatherElements.WeatherMeta;
import com.example.azat.weathergdg.entity.weatherElements.WeatherOfDay;
import com.example.azat.weathergdg.entity.weatherElements.WeatherOfHours;
import com.example.azat.weathergdg.service.WeatherService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class WeatherScrollingActivity extends AppCompatActivity {

    private static final int RESULT_NO_WEATHER=12;

    String cityName;
    String oldWeather;
    CollapsingToolbarLayout toolbarLayout;
    WeatherListAdapter weatherAdapter;
    WeatherMeta weatherMeta;
    Subscription inetSubscriber;

    SavingFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab!=null)
        {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tryGetWeatherFromServerOrGetFromDb();
                }
            });
        }
        cityName=getIntent().getStringExtra("cityName");
        oldWeather=getIntent().getStringExtra("oldWeather");
        toolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        toolbarLayout.setTitle(" ");
        initWeatherList();
        initToolbar();
        if (savedInstanceState != null) {

            fragment = (SavingFragment) getSupportFragmentManager().getFragment(
                    savedInstanceState, "savingFragment");
            updateWeatherList(fragment.getWeatherMeta());
        }
        else {
            tryGetWeatherFromServerOrGetFromDb();
            fragment=new SavingFragment();
            FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(fragment,"savingFragment");
            fragmentTransaction.commit();
        }

    }

    private void initWeatherList(){
        RecyclerView weatherList=(RecyclerView)findViewById(R.id.weatherList);
        weatherList.setLayoutManager(new WrapContentLinearLayoutManager(this));
        weatherAdapter=new WeatherListAdapter(new ArrayList<>());
        weatherList.setAdapter(weatherAdapter);
    }

    private void initToolbar(){
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        if (appBarLayout!=null)
        {
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = false;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (scrollRange + verticalOffset == 0) {
                        toolbarLayout.setTitle(cityName);
                        isShow = true;
                    } else if(isShow) {
                        toolbarLayout.setTitle(" ");
                        isShow = false;
                    }
                }
            });
        }
    }

    private void tryGetWeatherFromServerOrGetFromDb(){
        Retrofit retrofit=new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BuildConfig.CONNECTION_DEBUG)
                .build();
        WeatherService weatherService=retrofit.create(WeatherService.class);
        Observable<ResponseBody> getWeather=weatherService.getWeather5Day(cityName,BuildConfig.APP_ID,BuildConfig.APP_UNITS);
        inetSubscriber=getWeather.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseBody -> {
                    try {
                        String newWeather=responseBody.string();
                        updateWeatherListFromJSON(newWeather);
                        updateCityInDB(newWeather);
                    }catch (Exception e){
                        if (BuildConfig.DEBUG){
                            Log.d("ConnectionError",e.getMessage());
                        }
                    }
                },e->errorOnDownload(e));

    }

    private void errorOnDownload(Throwable e) {
        Snackbar.make(toolbarLayout,R.string.check_connection,Snackbar.LENGTH_LONG).show();
        if (BuildConfig.DEBUG){
            e.printStackTrace();
        }
        if (weatherAdapter.getItemCount()==0)
            updateWeatherListFromJSON(oldWeather);
    }
    private void updateCityInDB(String json){
        DatabaseHelper databaseHelper=new DatabaseHelper(getApplicationContext());
        Observable<Integer> updateCity=CityDatabase.updateCityObservable(databaseHelper,cityName,json,new Date(System.currentTimeMillis()));
        updateCity.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i->{},ex->dbError(ex));
    }

    private void dbError(Throwable ex) {
        Snackbar.make(toolbarLayout,R.string.database_connection_error,Snackbar.LENGTH_LONG).show();
        if (BuildConfig.DEBUG){
            Log.d("DbError",ex.getMessage());
        }
    }

    private void updateWeatherListFromJSON(String json){
        GsonBuilder builder = new GsonBuilder();
        builder.setLenient();
        Gson gson = builder.create();
        WeatherMeta weatherMeta=gson.fromJson(json,WeatherMeta.class);
        updateWeatherList(weatherMeta);
    }


    private void updateWeatherList(WeatherMeta weatherMeta){
        if (weatherMeta==null){
            return;
        }
        this.weatherMeta=weatherMeta;
        List<WeatherOfDay> weatherByDay= WeatherOfDay.fromListWeatherEntitiesStartingFromThisMoment(weatherMeta.getWeatherOfHours());
        if (weatherByDay==null||weatherByDay.size()==0){
            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("RESULT",RESULT_NO_WEATHER);
            startActivity(intent);
            return;
        }
        weatherAdapter.setWeatherOfDays(weatherByDay);
        WeatherOfHours firstDay=weatherMeta.getWeatherOfHours().get(0);

        TextView toolbarCityName=(TextView) findViewById(R.id.toolbarCityName);
        TextView toolbarDescription=(TextView)findViewById(R.id.toolbarDescription);
        ImageView toolbarImage=(ImageView)findViewById(R.id.toolbarImage);
        TextView toolbarTemp=(TextView)findViewById(R.id.toolbarTemp);
        toolbarCityName.setText(cityName);
        toolbarDescription.setText(firstDay.getFirstWeather().getDescription());
        toolbarTemp.setText(firstDay.getMain().getTemp()+"°C");
        int iconResourceId=this.getResources().getIdentifier("i"+firstDay.getFirstWeather().getIcon(),"drawable",this.getPackageName());
        Picasso.with(toolbarImage.getContext()).load(iconResourceId).resize(150,150).into(toolbarImage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        fragment.setWeatherMeta(weatherMeta);
        if (fragment.isAdded())
            getSupportFragmentManager().putFragment(outState,"savingFragment",fragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (inetSubscriber!=null){
            inetSubscriber.unsubscribe();
        }
    }
}
