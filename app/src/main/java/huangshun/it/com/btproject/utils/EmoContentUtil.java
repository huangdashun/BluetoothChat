package huangshun.it.com.btproject.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by hs on 2017/4/20.
 * 获取带有表情的Util
 */

public class EmoContentUtil {
    private static Context mContext;
    private static EmoContentUtil emoContentUtil;


    public static EmoContentUtil getInstance(Context context) {
        if (emoContentUtil == null) {
            emoContentUtil = new EmoContentUtil();
            mContext = context;
        }
        return emoContentUtil;

    }

    //<emo001>获取带有表情的文本
    public Spanned getEmoContent(String msg) {
        String htmlStr = msg;
        while (true) {
            int start = htmlStr.indexOf("<emo", 0);
            if (start != -1) {
                String resIdStr = htmlStr.substring(start + 1, start + 7);
                htmlStr = htmlStr.replaceFirst("<emo...>", "<img src='" + resIdStr + "'/>");//正则 .匹配除“\r\n”之外的任何单个字符
            } else {
                return Html.fromHtml(htmlStr, imgGetter, null);
            }
        }
    }

    private Html.ImageGetter imgGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager windowManger = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
            windowManger.getDefaultDisplay().getMetrics(dm);
            int resID = mContext.getResources().getIdentifier(source, "drawable", mContext.getPackageName());//获得应用包下的指定ID
            Drawable drawable = mContext.getResources().getDrawable(resID);
            int w = (int) (drawable.getIntrinsicWidth() * dm.density / 2);
            int h = (int) (drawable.getIntrinsicHeight() * dm.density / 2);
            drawable.setBounds(0, 0, w, h);
            return drawable;
        }
    };
}
