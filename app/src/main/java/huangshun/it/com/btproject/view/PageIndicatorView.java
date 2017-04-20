package huangshun.it.com.btproject.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import huangshun.it.com.btproject.R;

/**
 * 页面小圆点
 */
public class PageIndicatorView extends android.support.v7.widget.AppCompatImageView {

    public PageIndicatorView(Context context) {
        super(context);
        setSelectedView(false);
    }

    public void setSelectedView(boolean selected) {
        Bitmap bitmap;
        if (selected) {//当前页
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.page_select);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.page_item);
        }
        this.setImageBitmap(bitmap);
    }
}
