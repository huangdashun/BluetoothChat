package huangshun.it.com.btproject.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import huangshun.it.com.btproject.R;
import huangshun.it.com.btproject.view.DrawerHScrollView;

/**
 * Created by hs on 2017/3/17.
 * 表情初始化工具类
 */

public class Emoji {
    private static final String TAG = "Emoji";
    private static GridView mGridView;
    private static DrawerHScrollView mScrollView;

    private static int mScrollHeight;
    private static View mEmoView;

    private static Context mContext;
    private ArrayList<HashMap<String, Object>> mEmoList = new ArrayList<HashMap<String, Object>>();

    public Emoji(Context context) {
        mContext = context;
    }

    public static int getScrollHeight() {
        return mScrollHeight;
    }

    /**
     * 初始化表情View
     *
     * @return
     */
    public View initEmoView(final EditText mInput) {

        if (mEmoView == null) {
            mEmoView = View.inflate(mContext, R.layout.emo_layout, null);

            mScrollView = (DrawerHScrollView) mEmoView.findViewById(R.id.scrollView);
            mGridView = (GridView) mEmoView.findViewById(R.id.gridView);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // 在android中要显示图片信息，必须使用Bitmap位图的对象来装载
                    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), (Integer) mEmoList.get(position).get("img"));
                    ImageSpan imageSpan = new ImageSpan(mContext, bitmap);//字符串中添加图片
//                    setSpan(Object what, int start, int end, int flags)方法需要用户输入四个参数，
//                          what表示设置的格式是什么，可以是前景色、背景色也可以是可点击的文本等等，start表示需要设置格式的子字符串的起始下标，同理end表示终了下标，
//                          flags属性就有意思了，共有四种属性：
//                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE 从起始下标到终了下标，包括起始下标
//                    Spanned.SPAN_INCLUSIVE_INCLUSIVE 从起始下标到终了下标，同时包括起始下标和终了下标
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE 从起始下标到终了下标，但都不包括起始下标和终了下标
//                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE 从起始下标到终了下标，包括终了下标
                    SpannableString spannableString = new SpannableString((String) mEmoList.get(position).get("text"));//face就是图片的前缀名
                    spannableString.setSpan(imageSpan, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mInput.append(spannableString);
                }
            });

            mScrollHeight = setScrollGridView(mScrollView, mGridView, 3);
            System.out.println("mScrollHeight:" + mScrollHeight);
        }
        return mEmoView;
    }

    // 设置表情的多页滚动显示控件
    private int setScrollGridView(DrawerHScrollView scrollView, GridView gridView,
                                  int lines) {

        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        Display display = windowManager.getDefaultDisplay();
        System.out.println("Width:" + display.getWidth());
        System.out.println("Height:" + display.getHeight());


        int scrollWid = display.getWidth();
        int scrollHei;
        System.out.println("scrollWid:" + scrollWid);
        if (scrollWid <= 0) {
            Log.d(TAG, "scrollWid or scrollHei is less than 0");
            return 0;
        }


        float density = dm.density;      // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）

        int readlViewWidth = 56;
        // 图片都放在了Hdpi中，所以计算出图片的像素独立宽度
        int viewWidth = (int) (readlViewWidth * density / 1.5);
        int viewHeight = viewWidth;
        System.out.println("viewWidth:" + viewWidth + " viewHeight:" + viewHeight);

        int numColsPage = scrollWid / viewWidth;
        int spaceing = (scrollWid - viewWidth * numColsPage) / (numColsPage);
        System.out.println("Space:" + spaceing);


        SimpleAdapter adapter = getEmoAdapter();
        int pages = adapter.getCount() / (numColsPage * lines);

        if (pages * numColsPage * lines < adapter.getCount()) {
            pages++;
        }

        System.out.println("pages:" + pages);

        scrollHei = lines * viewHeight + spaceing * (lines + 1);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pages * scrollWid, LinearLayout.LayoutParams.WRAP_CONTENT);
        gridView.setLayoutParams(params);
        gridView.setColumnWidth(viewWidth);
        gridView.setHorizontalSpacing(spaceing);
        gridView.setVerticalSpacing(spaceing);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setNumColumns(numColsPage * pages);

        //adapter = new DrawerListAdapter(this, colWid, colHei);
        //listener = new DrawerItemClickListener();
        gridView.setAdapter(adapter);
        //mGridView.setOnItemClickListener(listener);

        scrollView.setParameters(pages, 0, scrollWid, spaceing);
        //updateDrawerPageLayout(pageNum, 0);
        // 表情区域还要加上分布显示区
        int pageNumHei = (int) (18 * density);
        return scrollHei + pageNumHei;
    }

    /**
     * 返回一个装着表情包的适配器
     *
     * @return
     */
    private SimpleAdapter getEmoAdapter() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("img", R.drawable.emo001);
        map.put("text", "<emo001>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo002);
        map.put("text", "<emo002>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo003);
        map.put("text", "<emo003>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo004);
        map.put("text", "<emo004>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo005);
        map.put("text", "<emo005>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo006);
        map.put("text", "<emo006>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo007);
        map.put("text", "<emo007>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo008);
        map.put("text", "<emo008>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo009);
        map.put("text", "<emo009>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo010);
        map.put("text", "<emo010>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo011);
        map.put("text", "<emo011>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo012);
        map.put("text", "<emo012>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo013);
        map.put("text", "<emo013>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo014);
        map.put("text", "<emo014>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo015);
        map.put("text", "<emo015>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo016);
        map.put("text", "<emo016>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo017);
        map.put("text", "<emo017>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo018);
        map.put("text", "<emo018>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo019);
        map.put("text", "<emo019>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo020);
        map.put("text", "<emo020>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo021);
        map.put("text", "<emo021>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo022);
        map.put("text", "<emo022>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo023);
        map.put("text", "<emo023>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo024);
        map.put("text", "<emo024>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo025);
        map.put("text", "<emo025>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo026);
        map.put("text", "<emo026>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo027);
        map.put("text", "<emo027>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo028);
        map.put("text", "<emo028>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo029);
        map.put("text", "<emo029>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo030);
        map.put("text", "<emo030>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo031);
        map.put("text", "<emo031>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo032);
        map.put("text", "<emo032>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo033);
        map.put("text", "<emo033>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo034);
        map.put("text", "<emo034>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo035);
        map.put("text", "<emo035>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo036);
        map.put("text", "<emo036>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo037);
        map.put("text", "<emo037>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo038);
        map.put("text", "<emo038>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo039);
        map.put("text", "<emo039>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo040);
        map.put("text", "<emo040>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo041);
        map.put("text", "<emo041>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo042);
        map.put("text", "<emo042>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo043);
        map.put("text", "<emo043>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo044);
        map.put("text", "<emo044>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo045);
        map.put("text", "<emo045>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo046);
        map.put("text", "<emo046>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo047);
        map.put("text", "<emo047>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo048);
        map.put("text", "<emo048>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo049);
        map.put("text", "<emo049>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo050);
        map.put("text", "<emo050>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo051);
        map.put("text", "<emo051>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo052);
        map.put("text", "<emo052>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo053);
        map.put("text", "<emo053>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo054);
        map.put("text", "<emo054>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo055);
        map.put("text", "<emo055>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo056);
        map.put("text", "<emo056>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo057);
        map.put("text", "<emo057>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo058);
        map.put("text", "<emo058>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo059);
        map.put("text", "<emo059>");
        mEmoList.add(map);
        map = new HashMap<>();
        map.put("img", R.drawable.emo060);
        map.put("text", "<emo060>");
        mEmoList.add(map);

        /**
         * 上述添加表情效率高，但是代码太冗余，下面的方式代码简单，但是效率较低
         */
           /*
           HashMap<String, Integer> map;
		   for(int i = 0; i < 100; i++){
			   map = new HashMap<String, Integer>();
			   Field field=R.drawable.class.getDeclaredField("image"+i);
			   int resourceId=Integer.parseInt(field.get(null).toString());
			   map.put("img", resourceId);
			   mEmoList.add(map);
		   }
		   */
//        第一个参数 表示访问整个android应用程序接口，基本上所有的组件都需要
//        第二个参数表示生成一个Map(String ,Object)列表选项
//        第三个参数表示界面布局的id  表示该文件作为列表项的组件
//        第四个参数表示该Map对象的哪些key对应value来生成列表项
//        第五个参数表示来填充的组件 Map对象key对应的资源一依次填充组件 顺序有对应关系
        return new SimpleAdapter(mContext, mEmoList, R.layout.grid_view_item,
                new String[]{"img"}, new int[]{R.id.imageView});
    }

}
