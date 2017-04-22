package cc.howlove.aodacat.weather.entity;

/**
 * Created by anymore on 17-4-22.
 */
public class WeatherID {
//    "fa": "00",	/*天气标识00：晴*/
//                "fb": "53"	/*天气标识53：霾 如果fa不等于fb，说明是组合天气*/
    private int fa;
    private int fb;

    public int getFa() {
        return fa;
    }

    public void setFa(int fa) {
        this.fa = fa;
    }

    public int getFb() {
        return fb;
    }

    public void setFb(int fb) {
        this.fb = fb;
    }

    @Override
    public String toString() {
        return "WeatherID{" +
                "fa=" + fa +
                ", fb=" + fb +
                '}';
    }
}
