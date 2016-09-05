package com.example.azat.weathergdg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import com.example.azat.weathergdg.adapter.CityListAdapter;
import com.example.azat.weathergdg.db.CityDatabase;
import com.example.azat.weathergdg.db.DatabaseHelper;
import com.example.azat.weathergdg.dialogs.DialogFactory;
import com.example.azat.weathergdg.service.WeatherService;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_NO_WEATHER=12;

    Cursor cityCursor;
    CityListAdapter cityAdapter;
    DatabaseHelper helper;
    RecyclerView rv;
    Observable<Cursor> getCityObservable;
    Subscription getCityObservableSubscription;

    ArrayList<Integer> deletedCitiesId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCity();
            }
        });

        Intent intent=getIntent();
        if (intent.getIntExtra("RESULT",1)==RESULT_NO_WEATHER){
            Snackbar.make(rv, R.string.no_weather_on_this_moment,Snackbar.LENGTH_LONG).show();
        }
        initCityList();
        deletedCitiesId=new ArrayList<>();


    }


    private void getCityFromDb(){
        helper=new DatabaseHelper(getApplicationContext());

        getCityObservable= CityDatabase.getCityObservable(helper).cache();
        getCityObservableSubscription = getCityObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cursor->{
                    cityAdapter.changeCursor(cursor);
                    cityCursor=cursor;
                },e->dbError(e));
    }
    private void getCityFromDbUnlessDeletedCity(){
        helper=new DatabaseHelper(getApplicationContext());
        Observable<Cursor> cursorObservable= CityDatabase.getCityUnlessDeletedCityObservable(helper,deletedCitiesId);
        cursorObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cursor->{
                    cityAdapter.changeCursor(cursor);
                    cityCursor=cursor;
                },e->dbError(e));
    }
    private void dbError(Throwable ex) {
        Snackbar.make(rv, R.string.database_connection_error,Snackbar.LENGTH_LONG).show();
        if (BuildConfig.DEBUG){
            Log.d("DbError",ex.getMessage());
        }
    }

    private void addCity(){
        Retrofit retrofit=new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BuildConfig.CONNECTION_DEBUG)
                .build();
        WeatherService weatherService=retrofit.create(WeatherService.class);
        DialogFactory.getChangeNameDialog(weatherService,this,BuildConfig.APP_ID,BuildConfig.APP_UNITS).show(getFragmentManager(),"addCityDialog");
    }

    //Вызывается из ChangeNameDialog
    public void addCityToDb(String cityName, String weatherMeta, Date date){
        if (helper==null)
            helper=new DatabaseHelper(getApplicationContext());
        Observable<Long> observable=CityDatabase.addCityObservable(helper,cityName,weatherMeta,date);
        observable.subscribeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(v->{
                    getCityFromDb();
                    Snackbar.make(rv, R.string.city_added,Snackbar.LENGTH_LONG).show();
                },e->{
                    if (e instanceof SQLiteConstraintException)
                        Snackbar.make(rv, R.string.city_exists,Snackbar.LENGTH_LONG).show();
                    else
                        netError(e);
                });
    }

    public void netError(Throwable e) {
        if (e instanceof HttpException){
            HttpException httpEx=(HttpException)e;
            if (httpEx.code()==404){
                cityDontExistsOnServer();
            }
        }
         else{
            Snackbar.make(rv, R.string.check_connection,Snackbar.LENGTH_LONG).show();
            if (BuildConfig.DEBUG){
               // Log.d("NetError",e.printStackTrace()+"");
                e.printStackTrace();
            }
        }
    }

    public void cityDontExistsOnServer(){
        Snackbar.make(rv, R.string.city_not_exists_on_server,Snackbar.LENGTH_LONG).show();
    }

    private void initCityList(){
        rv = (RecyclerView) findViewById(R.id.cityList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        cityAdapter=new CityListAdapter(cityCursor,this);
        rv.setAdapter(cityAdapter);
        ItemTouchHelper.SimpleCallback simpleTouchCallback=new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (cityCursor != null) {
                    cityCursor.moveToPosition(viewHolder.getAdapterPosition());
                    int deletedCityId = cityCursor.getInt(cityCursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                    deletedCitiesId.add(deletedCityId);
                    getCityFromDbUnlessDeletedCity();
                    Snackbar deleteSnackbar = Snackbar.make(rv, R.string.city_deleted, Snackbar.LENGTH_LONG);
                    deleteSnackbar.setAction(R.string.cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deletedCitiesId.remove(deletedCitiesId.lastIndexOf(deletedCityId));
                            deleteSnackbar.dismiss();
                            getCityFromDb();
                        }
                    });
                    deleteSnackbar.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                deleteCity(deletedCityId,true);
                            }
                            else if (event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE){
                                deleteCity(deletedCityId,false);
                            }
                        }
                    });
                    deleteSnackbar.show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(simpleTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv);
    }

    private void deleteCity(int cityId, boolean needUpdate){
        if (helper==null)
            helper=new DatabaseHelper(getApplicationContext());
        Observable<Integer> observable = CityDatabase.deleteCityObservable(helper,cityId );
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i->
                {
                    deletedCitiesId.remove(deletedCitiesId.lastIndexOf(cityId));
                    if (needUpdate)
                        getCityFromDb();
                },e->dbError(e));
    }
    @Override
    protected void onResume() {
        super.onResume();
        getCityFromDb();
        deletedCitiesId=new ArrayList<>();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (helper!=null)
            helper.close();
        if (cityCursor!=null)
            cityCursor.close();
        if (deletedCitiesId!=null && deletedCitiesId.size()>0)
        {
            deleteCity(deletedCitiesId.get(0),false);
            deletedCitiesId=null;
        }
    }
}
