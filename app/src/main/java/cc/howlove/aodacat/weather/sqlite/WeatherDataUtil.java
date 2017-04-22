package cc.howlove.aodacat.weather.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import cc.howlove.aodacat.weather.entity.WeatherDataEntity;
import cc.howlove.aodacat.weather.logutil.LogUtil;

/**
 * Created by anymore on 17-4-22.
 */

public class WeatherDataUtil {
    private static final String tag = "WeatherDataUtil";
    private WeatherDataSQLOpenHelper mSqlOpenHelper;
    private SQLiteDatabase mDatabase;
    private Gson mGson;

    public WeatherDataUtil(Context context, String dbName) {
        mSqlOpenHelper = new WeatherDataSQLOpenHelper(context,dbName,null,1);
        mDatabase = mSqlOpenHelper.getWritableDatabase();
        mGson = new Gson();
    }
    public void addWeatherData(WeatherDataEntity entity){
        String cityname = entity.getResult().getToday().getCity();
        String weatherdata = mGson.toJson(entity);
        LogUtil.v(tag,weatherdata);
        ContentValues values = new ContentValues();
        values.put("cityname",cityname);
        values.put("weatherdata",weatherdata);
        int result = mDatabase.update("weatherdatas",values,"cityname = ?",new String[]{cityname});
        if (result == 0){
            mDatabase.insert("weatherdatas",null,values);
            LogUtil.v(tag,"不存在此城市的数据,添加成功");
        }else {
            LogUtil.v(tag,"存在此城市数据,更新成功");
        }
    }
    public void addWeatherDatas(List<WeatherDataEntity> entities){
        for (WeatherDataEntity entity : entities) {
            addWeatherData(entity);
        }
    }
    public WeatherDataEntity getWeatherData(String cityname){
        if (cityname.length()>2){
            cityname = cityname.substring(0,1);
        }
        Cursor cursor= mDatabase.query("weatherdatas",null,"cityname like ?",new String[]{"%"+cityname+"%"},null,null,null);
        if (cursor.moveToFirst()){
            LogUtil.v(tag,"有数据");
            String weatherdata = cursor.getString(cursor.getColumnIndex("weatherdata"));
            WeatherDataEntity entity = mGson.fromJson(weatherdata,WeatherDataEntity.class);
            cursor.close();
            return entity;
        }
        LogUtil.v(tag,"没有数据");
        return null;
    }
    public List<WeatherDataEntity> getWeatherDatas(List<String> citynames){
        List<WeatherDataEntity> weatherDataEntities = new ArrayList<>();
        for (String cityname:citynames) {
            WeatherDataEntity entity = getWeatherData(cityname);
            if (entity != null){
                weatherDataEntities.add(entity);
            }
        }
        return weatherDataEntities;
    }
    public void closeDatabase(){
        mDatabase.close();
    }
}
