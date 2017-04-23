package cc.howlove.aodacat.weather.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cc.howlove.aodacat.weather.entity.CityEntity;
import cc.howlove.aodacat.weather.logutil.LogUtil;

/**
 * Created by anymore on 17-4-23.
 */

public class CitiesDataUtil {
    private static final String tag = "CitiesDataUtil";
    public static final String ACTION_SUBSCRIBED_CITY = "cc.howlove.aodacat.weather.ACTION_SUBSCRIBED_CITY";
    public static final String EXTRA_DISTRICT = "cc.howlove.aodacat.weather.EXTRA_DISTRICT";
    private CitiesSQLOpenHelper mOpenHelper;
    private SQLiteDatabase mSqLiteDatabase;
    private Context mContext;
    private static final int SUBSCRIBED_CITIE_SUCCESS = 1;
    private static final int SUBSCRIBED_CITIE_FAILED = 0;
    private static final int SUBSCRIBED_CITIE_REPETITION = 2;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SUBSCRIBED_CITIE_SUCCESS:
                    Intent intent = new Intent(ACTION_SUBSCRIBED_CITY);
                    intent.putExtra(EXTRA_DISTRICT, (String) msg.obj);
                    mContext.sendBroadcast(intent);
                    Toast.makeText(mContext,"添加成功..",Toast.LENGTH_SHORT).show();
                    break;
                case SUBSCRIBED_CITIE_FAILED:
                    Toast.makeText(mContext,"添加失败..请检查您输入的地区",Toast.LENGTH_SHORT).show();
                    break;
                case SUBSCRIBED_CITIE_REPETITION:
                    Toast.makeText(mContext,"添加失败..您已经订阅这个城市的天气，不要重复添加..",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    public CitiesDataUtil(Context context,String daName) {
        mOpenHelper = new CitiesSQLOpenHelper(context,daName,null,1);
        mSqLiteDatabase = mOpenHelper.getWritableDatabase();
        mContext = context;
    }

    public SQLiteDatabase getmSqLiteDatabase() {
        return mSqLiteDatabase;
    }
    private CityEntity isExist(String district){
        Cursor cursor = mSqLiteDatabase.query("ALL_CITIES",null,"district = ?",new String[]{district},null,null,null);
        if (cursor.moveToFirst()){
            CityEntity entity = new CityEntity();
            String city = cursor.getString(cursor.getColumnIndex("city"));
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String province = cursor.getString(cursor.getColumnIndex("province"));
            entity.setCity(city);
            entity.setProvince(province);
            entity.setId(id);
            entity.setDistrict(district);
            cursor.close();
            return entity;
        }
        cursor.close();
        return null;
    }
    public void subscribedCity(final String district){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = Message.obtain();
                String d = district;
                CityEntity entity = isExist(district);
                if (entity !=null){
                    if (isSubscribed(district)){
                        message.what = SUBSCRIBED_CITIE_REPETITION;
                        mHandler.sendMessage(message);
                    }else {
                        addSubscribedCity(entity);
                        message.what = SUBSCRIBED_CITIE_SUCCESS;
                        LogUtil.v(tag,d);
                        message.obj = d;
                        mHandler.sendMessage(message);
                    }
                }else {
                    message.what = SUBSCRIBED_CITIE_FAILED;
                    mHandler.sendMessage(message);
                }
            }
        }).start();
    }
    public void doTest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                test();
            }
        }).start();
    }
    public void test(){
        Cursor cursor = mSqLiteDatabase.query("ALL_CITIES",null,null,null,null,null,null);
        int i = 0;
        while (cursor.moveToFirst()){
            do {
                LogUtil.v(tag,cursor.getString(cursor.getColumnIndex("district")));
                i++;
            }while (cursor.moveToNext());
        }
        cursor.close();
        LogUtil.v(tag,"i = "+i);
    }
    private void addSubscribedCity(CityEntity entity){
        ContentValues values = new ContentValues();
        values.put("id",entity.getId());
        values.put("city",entity.getCity());
        values.put("district",entity.getDistrict());
        values.put("province",entity.getProvince());
        mSqLiteDatabase.insert("SUBSCRIBED_CITIES",null,values);
    }
    private boolean isSubscribed(String district){
        Cursor cursor = mSqLiteDatabase.query("SUBSCRIBED_CITIES",null,"district = ?",new String[]{district},null,null,null);
        return cursor.moveToFirst();
    }
    public List<CityEntity> getSubscribedCities(){
        List<CityEntity> cityEntities = new ArrayList<>();
        Cursor cursor = mSqLiteDatabase.query("SUBSCRIBED_CITIES",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do {
                CityEntity entity = new CityEntity();
                String city = cursor.getString(cursor.getColumnIndex("city"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String province = cursor.getString(cursor.getColumnIndex("province"));
                String district = cursor.getString(cursor.getColumnIndex("district"));
                entity.setCity(city);
                entity.setProvince(province);
                entity.setId(id);
                entity.setDistrict(district);
                cityEntities.add(entity);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return cityEntities;
    }
}
