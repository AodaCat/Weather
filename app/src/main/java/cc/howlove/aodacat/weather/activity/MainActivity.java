package cc.howlove.aodacat.weather.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cc.howlove.aodacat.weather.R;
import cc.howlove.aodacat.weather.adapter.WeatherAdapter;
import cc.howlove.aodacat.weather.entity.FutureWeather;
import cc.howlove.aodacat.weather.entity.WeatherDataEntity;
import cc.howlove.aodacat.weather.location.LocationUtil;
import cc.howlove.aodacat.weather.logutil.LogUtil;
import cc.howlove.aodacat.weather.sqlite.WeatherDataUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String tag = "MainActivity";
    private static final String baseURL = "http://v.juhe.cn/weather/index";
    private static final String key = "3cc8677905eb4140df95344b6ca96762";
    private static final int CODE_SUCCESS = 1;
    private static final int CODE_FAILED = 0;
    private static final int CODE_NO_DATA = 2;
    private String currentLocation;
    private Button btnSetting,btnAdd;
    private DrawerLayout mDrawerLayout;     // 左滑动
    private NavigationView navView;         // 设置菜单项的点击事件
    private TextView tvLocation;
    private ImageView ivCurrentLocation;
    private LocationUtil mLocationUtil;
    private OkHttpClient mOkHttpClient;
    private SwipeRefreshLayout srfRefresh;
    private Gson mGson;
    private RecyclerView rvFutureWeather;
    private WeatherAdapter mWeatherAdapter;
    private List<FutureWeather> futureWeathers;
    private WeatherDataUtil mWeatherDataUtil;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (srfRefresh.isRefreshing()){
                srfRefresh.setRefreshing(false);
            }
            switch (msg.what){
                case CODE_SUCCESS:
                    mWeatherAdapter.notifyDataSetChanged();
                    break;
                case CODE_FAILED:
                    Toast.makeText(MainActivity.this,"更新失败",Toast.LENGTH_SHORT).show();
                    mWeatherAdapter.notifyDataSetChanged();
                    break;
                case CODE_NO_DATA:
                    Toast.makeText(MainActivity.this,"拉取信息失败...",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationUtil = new LocationUtil(MainActivity.this);
        setContentView(R.layout.activity_main);
        navView = (NavigationView) findViewById(R.id.setting_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                if (item.getItemId() == R.id.setting_location) {
                    intent = new Intent(MainActivity.this, LocationActivity.class);
                } else {
                    return false;
                }
                startActivity(intent);
                return true;
            }
        });
        mOkHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30,TimeUnit.SECONDS)
                            .connectTimeout(10,TimeUnit.SECONDS)
                            .build();
        mLocationUtil.getCurrentLocation();
        mGson = new Gson();
        mWeatherDataUtil = new WeatherDataUtil(MainActivity.this,"weather.db");
        futureWeathers = new ArrayList<>();
        mWeatherAdapter = new WeatherAdapter(futureWeathers,MainActivity.this);
        initViews();
        setListeners();
        register();
//        refreshWeather();
        //获取权限获取当前位置
        getWeatherDataFromRecord();
        getCurrentLocation();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        btnSetting = (Button) findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }
    private void getCurrentLocation(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            mLocationUtil.getCurrentLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        mWeatherDataUtil.closeDatabase();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this,"您必须同意所有权限，程序才能正常使用..",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    mLocationUtil.getCurrentLocation();
                }else {
                    Toast.makeText(MainActivity.this,"发生未知错位，程序异常退出..",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }
    //初始化控件，为控件设定句柄
    private void initViews(){
        btnSetting = (Button) findViewById(R.id.btn_setting);
        btnAdd = (Button) findViewById(R.id.btn_add);
        tvLocation = (TextView) findViewById(R.id.tv_location);
        ivCurrentLocation = (ImageView) findViewById(R.id.iv_current_location);
        srfRefresh = (SwipeRefreshLayout) findViewById(R.id.srf_refresh);
        rvFutureWeather = (RecyclerView) findViewById(R.id.rv_future_weather);
        if (getCurrentLocationFromRecord()){
            ivCurrentLocation.setVisibility(View.VISIBLE);
        }
        tvLocation.setText(currentLocation);
    }
    //绑定控件事件
    private void setListeners(){
        srfRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLocationUtil.getCurrentLocation();
                refreshWeather();
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        rvFutureWeather.setLayoutManager(layoutManager);
        rvFutureWeather.setAdapter(mWeatherAdapter);
    }
    //注册广播
    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationUtil.ACTION_GET_CURRENT_LOCATION);
        registerReceiver(receiver,filter);
    }
    public boolean getCurrentLocationFromRecord() {
        SharedPreferences sharedPreferences = getSharedPreferences("locations",MODE_PRIVATE);
        currentLocation = sharedPreferences.getString("current_location","这是我瞎掰的一句话");
        if (currentLocation.equals("这是我瞎掰的一句话")){
            currentLocation = "信阳";
            return false;
        }
        return true;
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(LocationUtil.ACTION_GET_CURRENT_LOCATION)){
                int code = intent.getIntExtra(LocationUtil.EXTRA_CURRENT_LOCATION_RESULT_CODE,LocationUtil.EXTRA_CURRENT_LOCATION_FAILED);
                if (code == LocationUtil.EXTRA_CURRENT_LOCATION_SUCCESS){
                    currentLocation = intent.getStringExtra(LocationUtil.EXTRA_CURRENT_LOCATION);
                    tvLocation.setText(currentLocation);
                    refreshWeather();
                }else if (code == LocationUtil.EXTRA_CURRENT_LOCATION_FAILED){
                    if (getCurrentLocationFromRecord()){
                        ivCurrentLocation.setVisibility(View.VISIBLE);
                    }
                    refreshWeather();
                }
            }
        }
    };
    private void refreshWeather(){
        String url = baseURL+"?format=2&cityname="+currentLocation+"&key="+key;
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                WeatherDataEntity entity = mWeatherDataUtil.getWeatherData(currentLocation);
                if (entity == null){
                    message.what = CODE_NO_DATA;
                    mHandler.sendMessage(message);
                    return;
                }
                message.what = CODE_FAILED;
                mWeatherAdapter.setTodayWeather(entity.getResult().getToday());
                futureWeathers.clear();
                futureWeathers.addAll(Arrays.asList(entity.getResult().getFuture()));
                message.obj = entity;
                mHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                WeatherDataEntity entity = mGson.fromJson(result,WeatherDataEntity.class);
                LogUtil.v(tag,entity.getReason());
                mWeatherAdapter.setTodayWeather(entity.getResult().getToday());
                futureWeathers.clear();
                futureWeathers.addAll(Arrays.asList(entity.getResult().getFuture()));
                LogUtil.v(tag,entity.toString());
                Message message = Message.obtain();
                message.what = CODE_SUCCESS;
                message.obj = entity;
                mHandler.sendMessage(message);
                mWeatherDataUtil.addWeatherData(entity);

            }
        });
    }
    private void getWeatherDataFromRecord(){
        WeatherDataEntity entity = mWeatherDataUtil.getWeatherData(currentLocation);
        if (entity == null){
            Toast.makeText(MainActivity.this,"拉取数据失败..",Toast.LENGTH_SHORT).show();
            return;
        }
        mWeatherAdapter.setTodayWeather(entity.getResult().getToday());
        futureWeathers.clear();
        futureWeathers.addAll(Arrays.asList(entity.getResult().getFuture()));
        mWeatherAdapter.notifyDataSetChanged();
    }

}
