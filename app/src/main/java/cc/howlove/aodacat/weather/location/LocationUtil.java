package cc.howlove.aodacat.weather.location;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import cc.howlove.aodacat.weather.logutil.LogUtil;

/**
 * Created by anymore on 17-4-21.
 */

public class LocationUtil {
    private static final String tag = "LocationUtil";
    private Context mContext;
    public static final String ACTION_GET_CURRENT_LOCATION = "cc.howlove.aodacat.weather.location.action_get_current_location";
    public static final String EXTRA_CURRENT_LOCATION = "cc.howlove.aodacat.weather.location.extra_current_location";
    public static final String EXTRA_CURRENT_LOCATION_RESULT_CODE = "cc.howlove.aodacat.weather.location.extra_current_location_result_code";
    public static final int EXTRA_CURRENT_LOCATION_SUCCESS = 1;
    public static final int EXTRA_CURRENT_LOCATION_FAILED = 0;
    private LocationClient mLocationClient;

    public LocationUtil(Context mContext) {
        this.mContext = mContext;
        initLocationClient();
    }
    private void initLocationClient(){
        mLocationClient = new LocationClient(mContext);
        mLocationClient.registerLocationListener(bdLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }
    public void getCurrentLocation(){
        LogUtil.v(tag,"getCurrentLocation");
        mLocationClient.start();
    }
    private BDLocationListener bdLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mLocationClient.stop();//定位一次就够了
            Intent intent = new Intent(ACTION_GET_CURRENT_LOCATION);
            intent.putExtra(EXTRA_CURRENT_LOCATION_RESULT_CODE,EXTRA_CURRENT_LOCATION_SUCCESS);
            intent.putExtra(EXTRA_CURRENT_LOCATION,bdLocation);
            mContext.sendBroadcast(intent);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            LogUtil.v(tag,"onConnectHotSpotMessage");
        }
    };
}
