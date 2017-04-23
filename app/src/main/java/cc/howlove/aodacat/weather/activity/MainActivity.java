package cc.howlove.aodacat.weather.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cc.howlove.aodacat.weather.R;
import cc.howlove.aodacat.weather.adapter.ContentFragmentAdapter;
import cc.howlove.aodacat.weather.entity.CityEntity;
import cc.howlove.aodacat.weather.fragment.ContentFragment;
import cc.howlove.aodacat.weather.fragment.DefaultContentFragment;
import cc.howlove.aodacat.weather.logutil.LogUtil;
import cc.howlove.aodacat.weather.sqlite.CitiesDataUtil;
import cc.howlove.aodacat.weather.sqlite.CitiesSQLOpenHelper;

public class MainActivity extends AppCompatActivity {
    private static final String tag = "MainActivity";
    public static final String baseURL1 = "http://v.juhe.cn/weather/index";
    public static final String baseURL2 = "http://v.juhe.cn/weather/geo";
    public static final String key = "3cc8677905eb4140df95344b6ca96762";
    public static final int CODE_SUCCESS = 1;
    public static final int CODE_FAILED = 0;
    public static final int CODE_NO_DATA = 2;
    private ViewPager vpContents;
    private Button btnAdd,btnSetting;
    public ImageView ivCurrentLocation;
    public TextView tvLocation;
    private ContentFragmentAdapter mContentFragmentAdapter;
    private List<Fragment> fragments;
    private CitiesDataUtil mCitiesDataUtil;
    private List<CityEntity> cityEntities;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCityList();
        initViews();
        initViewPager();
        setListeners();
        register();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void initCityList() {
        mCitiesDataUtil = new CitiesDataUtil(MainActivity.this,"cities.db");
    }

    private void initViews() {
        ivCurrentLocation = (ImageView) findViewById(R.id.iv_current_location);
        tvLocation = (TextView) findViewById(R.id.tv_location);
        btnAdd = (Button) findViewById(R.id.btn_add);
        btnSetting = (Button) findViewById(R.id.btn_setting);
        vpContents = (ViewPager) findViewById(R.id.vp_contents);
    }
    private void setListeners(){
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AddCityActivity.class);
                startActivity(intent);
            }
        });
        vpContents.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0){
                    DefaultContentFragment d = (DefaultContentFragment) fragments.get(0);
                    tvLocation.setText(d.currentCityName);
                }else {
                    ContentFragment c = (ContentFragment) fragments.get(position);
                    tvLocation.setText(c.cityname);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    private void initViewPager(){
        cityEntities = mCitiesDataUtil.getSubscribedCities();
        fragments = new ArrayList<>();
        fragments.add(new DefaultContentFragment());
        for (CityEntity c:cityEntities) {
            fragments.add(ContentFragment.newInstance(c.getDistrict()));
        }
        mContentFragmentAdapter = new ContentFragmentAdapter(getSupportFragmentManager(),fragments);
        vpContents.setAdapter(mContentFragmentAdapter);
    }
    private void register(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(CitiesDataUtil.ACTION_SUBSCRIBED_CITY);
        registerReceiver(receiver,filter);
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CitiesDataUtil.ACTION_SUBSCRIBED_CITY)){
                String district = intent.getStringExtra(CitiesDataUtil.EXTRA_DISTRICT);
                //添加新的碎片
                fragments.add(ContentFragment.newInstance(district));
                mContentFragmentAdapter.notifyDataSetChanged();
            }
        }
    };
}
