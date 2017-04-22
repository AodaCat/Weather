package cc.howlove.aodacat.weather.entity;

import java.util.Arrays;

/**
 * Created by anymore on 17-4-22.
 */

public class WeatherResult {
    //当前天气概述

    private CurrentWeather sk;
    private TodayWeather today;
    private FutureWeather[] future;

    public CurrentWeather getSk() {
        return sk;
    }

    public void setSk(CurrentWeather sk) {
        this.sk = sk;
    }

    public TodayWeather getToday() {
        return today;
    }

    public void setToday(TodayWeather today) {
        this.today = today;
    }

    public FutureWeather[] getFuture() {
        return future;
    }

    public void setFuture(FutureWeather[] future) {
        this.future = future;
    }

    @Override
    public String toString() {
        return "WeatherResult{" +
                "sk=" + sk +
                ", today=" + today +
                ", future=" + Arrays.toString(future) +
                '}';
    }
}
