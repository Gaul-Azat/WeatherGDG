package com.example.azat.weathergdg.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;

public class CityDatabase {

    public static Callable<Cursor> getCity(DatabaseHelper helper){
        return new Callable<Cursor>() {
            @Override
            public Cursor call() throws Exception {
                String query="SELECT * FROM " + DatabaseHelper.TABLE;
                return helper.getReadableDatabase().rawQuery(query,null);
            }
        };
    }

    public static Callable<Cursor> getCityUnlessDeletedCity(DatabaseHelper helper,ArrayList<Integer> deletedCitiesId){
        if (deletedCitiesId==null||deletedCitiesId.size()==0){
            return getCity(helper);
        }
        return new Callable<Cursor>() {
            @Override
            public Cursor call() throws Exception {
                String range="";
                for (int i=0;i<deletedCitiesId.size();i++) {
                    range+=(deletedCitiesId.get(i)+", ");
                }
                range=range.substring(0,range.length()-2);
                String query="SELECT * FROM " + DatabaseHelper.TABLE+" WHERE " + DatabaseHelper.COLUMN_ID+" NOT IN ("+range+")";
                return helper.getReadableDatabase().rawQuery(query,null);
            }
        };
    }

    public static Callable<Long> addCity(DatabaseHelper helper,String cityName, String weatherMeta, Date date){
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                DateFormat dateTimeInstance = SimpleDateFormat.getDateTimeInstance();
                ContentValues cv=new ContentValues();
                cv.put(helper.COLUMN_CITY,cityName);
                cv.put(helper.COLUMN_OLD_WEATHER,weatherMeta);
                cv.put(helper.COLUMN_DATE_OLD_WEATHER,dateTimeInstance.format(date));
                SQLiteDatabase writableDatabase=helper.getWritableDatabase();
                Long addCount=writableDatabase.insertOrThrow(helper.TABLE,null,cv);
                writableDatabase.close();
                helper.close();
                return addCount;
            }
        };
    }

    public static Callable<Integer> deleteCity(DatabaseHelper helper,Integer id){
        return new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SQLiteDatabase writableDatabase=helper.getWritableDatabase();
                int delCount=writableDatabase.delete(helper.TABLE, helper.COLUMN_ID+" = "+id,null);
                writableDatabase.close();
                helper.close();
                return delCount;
            }
        };
    }

    public static Callable<Integer> updateCity(DatabaseHelper helper, String cityName, String newWeather, Date newDate){
        return new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                DateFormat dateTimeInstance = SimpleDateFormat.getDateTimeInstance();
                ContentValues cv=new ContentValues();
                cv.put(helper.COLUMN_CITY,cityName);
                cv.put(helper.COLUMN_OLD_WEATHER,newWeather);
                cv.put(helper.COLUMN_DATE_OLD_WEATHER,dateTimeInstance.format(newDate));
                SQLiteDatabase writableDatabase=helper.getWritableDatabase();
                int updCount=writableDatabase.update(helper.TABLE,cv,helper.COLUMN_CITY+" = ?",new String[]{cityName});
                writableDatabase.close();
                helper.close();
                return updCount;
            }
        };
    }

    public static Observable<Cursor> getCityObservable(DatabaseHelper helper){
        return makeObservable(getCity(helper));
    }

    public static Observable<Cursor> getCityUnlessDeletedCityObservable(DatabaseHelper helper, ArrayList<Integer> deletedCitiesId){
        return makeObservable(getCityUnlessDeletedCity(helper,deletedCitiesId));
    }

    public static Observable<Long> addCityObservable(DatabaseHelper helper,String cityName, String weatherMeta, Date date){
        return makeObservable(addCity(helper,cityName,weatherMeta,date));
    }

    public static Observable<Integer> deleteCityObservable(DatabaseHelper helper, Integer id){
        return makeObservable(deleteCity(helper,id));
    }

    public static Observable<Integer> updateCityObservable(DatabaseHelper helper, String cityName, String newWeather, Date newDate){
        return makeObservable(updateCity(helper,cityName,newWeather,newDate));
    }

    public static <T> Observable<T> makeObservable(final Callable<T> func) {
        return Observable.create(
                new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        try {
                            T observed = func.call();
                            if (observed != null) { // to make defaultIfEmpty work
                                subscriber.onNext(observed);
                            }
                            subscriber.onCompleted();
                        } catch (Exception ex) {
                            subscriber.onError(ex);
                        }
                    }
                });
    }


}
