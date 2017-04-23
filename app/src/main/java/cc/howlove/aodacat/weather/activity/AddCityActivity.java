package cc.howlove.aodacat.weather.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import cc.howlove.aodacat.weather.R;
import cc.howlove.aodacat.weather.adapter.CityCursorAdapter;
import cc.howlove.aodacat.weather.sqlite.CitiesDataUtil;

public class AddCityActivity extends AppCompatActivity {
    private static final String tag = "AddCityActivity";
    private Button btnBack,btnSubmit;
    private AutoCompleteTextView actvCity;
    private RecyclerView rvHotCities;
    private CityCursorAdapter mCityCursorAdapter;
    private CitiesDataUtil mCitiesDataUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        initViews();
        setListener();
    }

    private void initViews() {
        btnBack = (Button) findViewById(R.id.btn_back);
        btnSubmit = (Button) findViewById(R.id.btn_submit);
        actvCity = (AutoCompleteTextView) findViewById(R.id.actv_city);
        rvHotCities = (RecyclerView) findViewById(R.id.rv_hot_cities);
    }
    private void setListener(){
        mCitiesDataUtil = new CitiesDataUtil(AddCityActivity.this,"cities.db");
        mCityCursorAdapter = new CityCursorAdapter(AddCityActivity.this,null,0,mCitiesDataUtil.getmSqLiteDatabase());
        actvCity.setAdapter(mCityCursorAdapter);
        actvCity.setThreshold(1);
        actvCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView item = (TextView) view;
                actvCity.setText(item.getText());
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCity();
            }
        });
    }

    private void addCity() {
        String text = actvCity.getText().toString();
        String district;
        if (text.contains("-")){
            district = text.substring(text.lastIndexOf('-')+1);
        }else {
            district = text;
        }
        mCitiesDataUtil.subscribedCity(district);
    }
}
