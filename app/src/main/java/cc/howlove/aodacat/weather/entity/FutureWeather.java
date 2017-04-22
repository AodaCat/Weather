package cc.howlove.aodacat.weather.entity;

/**
 * Created by anymore on 17-4-22.
 */
public class FutureWeather {
//    "temperature": "26℃~33℃",
//            "weather": "多云",
//            "weather_id": {
//        "fa": "01",
//                "fb": "01"
//    },
//            "wind": "北风4-5级",
//            "week": "星期日",
//            "date": "20140810"
    private String temperature;
    private String weather;
    private WeatherID weather_id;
    private String wind;
    private String week;
    private String date;

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public WeatherID getWeather_id() {
        return weather_id;
    }

    public void setWeather_id(WeatherID weather_id) {
        this.weather_id = weather_id;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "FutureWeather{" +
                "temperature='" + temperature + '\'' +
                ", weather='" + weather + '\'' +
                ", weather_id=" + weather_id +
                ", wind='" + wind + '\'' +
                ", week='" + week + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
