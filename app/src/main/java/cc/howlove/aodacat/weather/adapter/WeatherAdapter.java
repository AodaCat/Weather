package cc.howlove.aodacat.weather.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cc.howlove.aodacat.weather.R;
import cc.howlove.aodacat.weather.entity.FutureWeather;
import cc.howlove.aodacat.weather.entity.TodayWeather;
import cc.howlove.aodacat.weather.entity.WeatherID;
import cc.howlove.aodacat.weather.weatherutil.WeatherId2IconUtil;

/**
 * Created by anymore on 17-4-22.
 */

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder>{
    private List<FutureWeather> futureWeathers;
    private Context mContext;
    private TodayWeather todayWeather;
    private View todayWeatherItem;
    private static final int TYPE_TODAY = 0;
    private static final int TYPE_FUTURE = 1;
    public WeatherAdapter(List<FutureWeather> futureWeathers, Context mContext) {
        this.futureWeathers = futureWeathers;
        this.mContext = mContext;
        //最开始可能todayweather没有更新
        //所以随便设置一个代替一下
        todayWeather = new TodayWeather();
        todayWeather.setCity("信阳");
        WeatherID weatherID = new WeatherID();
        weatherID.setFa(-1);
        weatherID.setFb(-1);
        todayWeather.setWeather_id(weatherID);
        todayWeather.setWeather("未知");
        todayWeather.setTemperature("未知");
    }

    public TodayWeather getTodayWeather() {
        return todayWeather;
    }

    public void setTodayWeather(TodayWeather todayWeather) {
        this.todayWeather = todayWeather;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (todayWeatherItem == null){
            todayWeatherItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.today_weather_item,parent,false);
            return new ViewHolder(todayWeatherItem);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.future_weather_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_TODAY){
            Bitmap bitmap = WeatherId2IconUtil.getIcon(mContext,todayWeather.getWeather_id().getFa());
            holder.ivTodayWeather.setImageBitmap(bitmap);
            String text = todayWeather.getWeather()+"\n"+todayWeather.getWind()+"\n"+todayWeather.getTemperature();
            holder.tvTodayWeather.setText(text);
            return;
        }
        FutureWeather weather = futureWeathers.get(position-1);
        int id = weather.getWeather_id().getFa();
        Bitmap bitmap = WeatherId2IconUtil.getIcon(mContext,id);
        String s = weather.getWeek()+"\n"+weather.getWeather();
        String temp = weather.getTemperature();
        holder.ivFutureWeather.setImageBitmap(bitmap);
        holder.tvFutureWeather.setText(s);
        holder.tvFutureTemp.setText(temp);
    }

    @Override
    public int getItemCount() {
        return futureWeathers.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return TYPE_TODAY;
        }
        return TYPE_FUTURE;
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivFutureWeather;
        TextView tvFutureWeather;
        TextView tvFutureTemp;
        ImageView ivTodayWeather;
        TextView tvTodayWeather;
        public ViewHolder(View itemView) {
            super(itemView);
            if (itemView == todayWeatherItem){
                ivTodayWeather = (ImageView) itemView.findViewById(R.id.iv_today_weather);
                tvTodayWeather = (TextView) itemView.findViewById(R.id.tv_today_weather);
                return;
            }
            ivFutureWeather = (ImageView) itemView.findViewById(R.id.iv_future_weather);
            tvFutureWeather = (TextView) itemView.findViewById(R.id.tv_future_weather);
            tvFutureTemp = (TextView) itemView.findViewById(R.id.tv_future_temp);
        }
    }
}
