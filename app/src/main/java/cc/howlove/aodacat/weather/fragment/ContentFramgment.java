package cc.howlove.aodacat.weather.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cc.howlove.aodacat.weather.R;
import cc.howlove.aodacat.weather.activity.MainActivity;
import cc.howlove.aodacat.weather.location.LocationUtil;
import okhttp3.OkHttpClient;

/**
 * Created by anymore on 17-4-22.
 */

public class ContentFramgment extends Fragment{
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
                    Toast.makeText(getContext(),"拉取信息失败...",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationUtil = new LocationUtil(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_layout,container,false);
        btnSetting = (Button) view.findViewById(R.id.btn_setting);
        btnAdd = (Button) view.findViewById(R.id.btn_add);
        tvLocation = (TextView) view.findViewById(R.id.tv_location);
        ivCurrentLocation = (ImageView) view.findViewById(R.id.iv_current_location);
        tvWeather = (TextView) view.findViewById(R.id.tv_weather);
        ivWeather = (ImageView) view.findViewById(R.id.iv_weather);
        if (getCurrentLocationFromRecord()){
            ivCurrentLocation.setVisibility(View.VISIBLE);
        }
        tvLocation.setText(currentLocation);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public boolean getCurrentLocationFromRecord() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("locations", Context.MODE_PRIVATE);
        currentLocation = sharedPreferences.getString("current_location","这是我瞎掰的一句话");

        if (currentLocation.equals("这是我瞎掰的一句话")){
            currentLocation = "信阳";
            return false;
        }
        return true;
    }
}
