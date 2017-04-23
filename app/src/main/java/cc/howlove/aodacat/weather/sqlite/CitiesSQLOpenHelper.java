package cc.howlove.aodacat.weather.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import cc.howlove.aodacat.weather.entity.CityEntity;
import cc.howlove.aodacat.weather.logutil.LogUtil;

/**
 * Created by anymore on 17-4-23.
 */

public class CitiesSQLOpenHelper extends SQLiteOpenHelper{
    private static final String tag = "CitiesSQLOpenHelper";
    private Context mContext;
    private static final String CREATE_ALL_CITIES_TABLE = "create table ALL_CITIES(" +
            "id integer," +
            "province text," +
            "district text," +
            "city text)";
    private static final String CREATE_SUB_CITIES_TABLE = "create table SUBSCRIBED_CITIES(" +
            "id integer," +
            "province text," +
            "district text," +
            "city text)";
    public CitiesSQLOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_ALL_CITIES_TABLE);
        sqLiteDatabase.execSQL(CREATE_SUB_CITIES_TABLE);
        LogUtil.v(tag,"数据表建立完毕");
        initAllCities(sqLiteDatabase);
    }

    private void initAllCities(final SQLiteDatabase db) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                LogUtil.v(tag,"in run");
                try {
                    Reader reader = new InputStreamReader(
                            mContext.getAssets().open("cities.json"));
                    List<CityEntity> cityEntities = gson.fromJson(reader,new TypeToken<List<CityEntity>>()
                    {}.getType());
                    ContentValues values = new ContentValues();
                    for (CityEntity entity : cityEntities) {
                        values.put("id",entity.getId());
                        values.put("city",entity.getCity());
                        values.put("province",entity.getProvince());
                        values.put("district",entity.getDistrict());
                        db.insert("ALL_CITIES",null,values);
                        values.clear();
                    }
                    db.close();
                    LogUtil.v(tag,"所有城市列表初始化完毕..共"+cityEntities.size()+"条数据");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
