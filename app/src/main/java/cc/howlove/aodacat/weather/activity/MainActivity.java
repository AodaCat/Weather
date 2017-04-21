package cc.howlove.aodacat.weather.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cc.howlove.aodacat.weather.R;
import cc.howlove.aodacat.weather.location.LocationUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String baseURL = "http://v.juhe.cn/weather/index";
    private static final String key = "3cc8677905eb4140df95344b6ca96762";
    private static final int CODE_SUCCESS = 1;
    private static final int CODE_FAILED = 0;
    private String currentLocation;
    private Button btnSetting,btnAdd;
    private TextView tvLocation,tvWeather;
    private ImageView ivCurrentLocation,ivWeather;
    private LocationUtil mLocationUtil;
    private OkHttpClient mOkHttpClient;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CODE_SUCCESS:
                    String result = (String) msg.obj;
                    tvWeather.setText(result);
                    break;
                case CODE_FAILED:
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
        mOkHttpClient = new OkHttpClient();
        mLocationUtil.getCurrentLocation();
        initViews();
        register();
        refreshWeather();
        //获取权限获取当前位置
        getCurrentLocation();

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

    private void initViews(){
        btnSetting = (Button) findViewById(R.id.btn_setting);
        btnAdd = (Button) findViewById(R.id.btn_add);
        tvLocation = (TextView) findViewById(R.id.tv_location);
        ivCurrentLocation = (ImageView) findViewById(R.id.iv_current_location);
        tvWeather = (TextView) findViewById(R.id.tv_weather);
        ivWeather = (ImageView) findViewById(R.id.iv_weather);
        if (getCurrentLocationFromRecord()){
            ivCurrentLocation.setVisibility(View.VISIBLE);
        }
        tvLocation.setText(currentLocation);
    }

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
                Message message = Message.obtain();
                message.what = CODE_FAILED;
                mHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Message message = Message.obtain();
                message.what = CODE_SUCCESS;
                message.obj = result;
                mHandler.sendMessage(message);
            }
        });
    }
}
