package cc.howlove.aodacat.weather.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cc.howlove.aodacat.weather.logutil.LogUtil;

/**
 * Created by anymore on 17-4-22.
 */

public class WeatherDataSQLOpenHelper extends SQLiteOpenHelper{
    private static final String tag = "WeatherDataSQLOpenHelper";
    private static final String CREATE_TABLE = "create table weatherdatas (" +
            "id integer primary key autoincrement," +
            "cityname text," +
            "weatherdata text)";
    public WeatherDataSQLOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
        LogUtil.v(tag,"create table success");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
