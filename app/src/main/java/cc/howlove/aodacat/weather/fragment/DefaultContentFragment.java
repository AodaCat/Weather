package cc.howlove.aodacat.weather.fragment;

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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cc.howlove.aodacat.weather.R;
import cc.howlove.aodacat.weather.activity.MainActivity;
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

/**
 * 与其他ContentFragment布局相同，但是这个Fragment是程序的默认视图，所以它需要进行运行时权限的处理
 * Created by anymore on 17-4-22.
 */

public class DefaultContentFragment extends Fragment{
    private static final String tag = "DefaultContentFragment";
    private SwipeRefreshLayout srlRefresh;
    private RecyclerView rvWeatherDatas;
    private LocationUtil mLocationUtil;
    private OkHttpClient mOkHttpClient;
    private Gson mGson;
    private WeatherDataUtil mWeatherDataUtil;
    private WeatherAdapter mWeatherAdapter;
    private List<FutureWeather> futureWeathers;
    private BDLocation mBdLocation;
    public String currentCityName;
    private MainActivity mMainActivity;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (srlRefresh.isRefreshing()){
                srlRefresh.setRefreshing(false);
            }
            switch (msg.what){
                case MainActivity.CODE_SUCCESS:
                    mWeatherAdapter.notifyDataSetChanged();
                    break;
                case MainActivity.CODE_FAILED:
                    Toast.makeText(getContext(),"更新失败"+msg.obj,Toast.LENGTH_SHORT).show();
                    mWeatherAdapter.notifyDataSetChanged();
                    break;
                case MainActivity.CODE_NO_DATA:
                    Toast.makeText(getContext(),"拉取信息失败...",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
        mLocationUtil = new LocationUtil(getContext());
        mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .connectTimeout(10,TimeUnit.SECONDS)
                .build();
        mGson = new Gson();
        mWeatherDataUtil = new WeatherDataUtil(getContext(),"weather.db");
        futureWeathers = new ArrayList<>();
        getCurrentLocationFromRecord();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View defaultContentView = inflater.inflate(R.layout.content_layout,container,false);
        srlRefresh = (SwipeRefreshLayout) defaultContentView.findViewById(R.id.srl_refresh);
        rvWeatherDatas = (RecyclerView) defaultContentView.findViewById(R.id.rv_weather_datas);
        return defaultContentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setListeners();
        getCurrentLocation();
        register();
        refreshWeather();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
        mWeatherDataUtil.closeDatabase();
    }

    private void setListeners(){
        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWeather();
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mWeatherAdapter = new WeatherAdapter(futureWeathers,getContext());
        rvWeatherDatas.setLayoutManager(layoutManager);
        rvWeatherDatas.setAdapter(mWeatherAdapter);
    }
    //注册广播
    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationUtil.ACTION_GET_CURRENT_LOCATION);
        getActivity().registerReceiver(receiver,filter);
    }
    private void getCurrentLocation(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            requestPermissions(permissions,1);//碎片的运行时权限请求
        }else {
            mLocationUtil.getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(getContext(),"您必须同意所有权限，程序才能正常使用..",Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                            return;
                        }
                    }
                    mLocationUtil.getCurrentLocation();
                }else {
                    Toast.makeText(getContext(),"发生未知错位，程序异常退出..",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            default:
                break;
        }
    }
    private void refreshWeather(){
        String url = null;
        if (mBdLocation != null){
            url = MainActivity.baseURL2+"?format=2&dtype=json&key="
                    +MainActivity.key+"&lon="+mBdLocation.getLongitude()+"&lat="+mBdLocation.getLatitude();
        }else {
            url = MainActivity.baseURL1+"?format=2&dtype=json&cityname="+currentCityName+"&key="+MainActivity.key;
        }
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                WeatherDataEntity entity = mWeatherDataUtil.getWeatherData("default");
                if (entity == null){
                    message.what = MainActivity.CODE_NO_DATA;
                    mHandler.sendMessage(message);
                    return;
                }
                message.what = MainActivity.CODE_FAILED;
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
                Message message = Message.obtain();
                if (entity.getResultcode() != 200){
                    message.what = MainActivity.CODE_FAILED;
                    message.obj = entity.getReason();
                    mHandler.sendMessage(message);
                    return;
                }
                mWeatherAdapter.setTodayWeather(entity.getResult().getToday());
                futureWeathers.clear();
                futureWeathers.addAll(Arrays.asList(entity.getResult().getFuture()));
                LogUtil.v(tag,entity.toString());
                message.what = MainActivity.CODE_SUCCESS;
                message.obj = entity;
                mHandler.sendMessage(message);
                mWeatherDataUtil.addWeatherData(entity,"default");
            }
        });
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(LocationUtil.ACTION_GET_CURRENT_LOCATION)){
                int code = intent.getIntExtra(LocationUtil.EXTRA_CURRENT_LOCATION_RESULT_CODE,LocationUtil.EXTRA_CURRENT_LOCATION_FAILED);
                if (code == LocationUtil.EXTRA_CURRENT_LOCATION_SUCCESS){
                    mBdLocation = intent.getParcelableExtra(LocationUtil.EXTRA_CURRENT_LOCATION);
                    currentCityName = mBdLocation.getDistrict();
                    mMainActivity.tvLocation.setText(currentCityName);
                    SharedPreferences.Editor editor = getContext().getSharedPreferences("locations",Context.MODE_PRIVATE).edit();
                    editor.putString("current_location",currentCityName);
                    editor.apply();
                    refreshWeather();
                }
            }
        }
    };
    public void getCurrentLocationFromRecord() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("locations",Context.MODE_PRIVATE);
        currentCityName = sharedPreferences.getString("current_location","这是我瞎掰的一句话");
        if (currentCityName.equals("这是我瞎掰的一句话")){
            currentCityName = "信阳";
        }
    }
}
