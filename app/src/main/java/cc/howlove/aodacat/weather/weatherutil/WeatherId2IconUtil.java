package cc.howlove.aodacat.weather.weatherutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import cc.howlove.aodacat.weather.R;

/**
 * Created by anymore on 17-4-21.
 */

public class WeatherId2IconUtil {
    private static int[] weatherIconId = {R.mipmap.w00,R.mipmap.w01,R.mipmap.w02, R.mipmap.w03,
            R.mipmap.w04, R.mipmap.w05, R.mipmap.w06, R.mipmap.w07, R.mipmap.w08, R.mipmap.w09,
            R.mipmap.w10, R.mipmap.w11, R.mipmap.w12, R.mipmap.w13, R.mipmap.w14, R.mipmap.w15,
            R.mipmap.w16, R.mipmap.w17, R.mipmap.w18, R.mipmap.w19, R.mipmap.w20, R.mipmap.w21,
            R.mipmap.w22, R.mipmap.w23, R.mipmap.w24, R.mipmap.w25, R.mipmap.w26, R.mipmap.w27,
            R.mipmap.w28,R.mipmap.w29,R.mipmap.w30,R.mipmap.w31};
    private static int w_53 = R.mipmap.w53;
    private static int w_unknow = R.mipmap.wunknow;

    public static Bitmap getIcon(Context context,int id){
        Bitmap bitmap = null;
        if (id >= 0 && id <= 31){
            bitmap = BitmapFactory.decodeResource(context.getResources(),weatherIconId[id]);
        }else if (id == 53){
            bitmap = BitmapFactory.decodeResource(context.getResources(),w_53);
        }else {
            bitmap = BitmapFactory.decodeResource(context.getResources(),w_unknow);
        }
        return bitmap;
    }
}
