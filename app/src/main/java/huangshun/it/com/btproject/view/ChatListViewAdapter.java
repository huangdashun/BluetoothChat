package huangshun.it.com.btproject.view;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import huangshun.it.com.btproject.DB.DBAdapterImage;
import huangshun.it.com.btproject.R;
import huangshun.it.com.btproject.utils.EmoContentUtil;

/**
 * Created by hs on 2017/3/27.
 * 聊天记录的适配器
 */
public class ChatListViewAdapter extends BaseAdapter {
    public static final int ROLE_OWN = 0;
    public static final int ROLE_TARGET = 1;
    public static final int ROLE_OTHER = 2;
    public static final String KEY_ROLE = "role";//角色
    public static final String KEY_TEXT = "text";//内容
    public static final String KEY_DATE = "date";//日期
    public static final String KEY_NAME = "name";//蓝牙名称
    public static final String KEY_SHOW_MSG = "show_msg";

    private Context mContext;

    private ArrayList<HashMap<String, Object>> mDatalist;


    private DisplayMetrics dm;


    public ChatListViewAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
        super();
        mContext = context;
        mDatalist = data;
        dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    @Override
    public int getCount() {
        return mDatalist.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HashMap<String, Object> roleMap = mDatalist.get(position);
        int role = (Integer) roleMap.get(KEY_ROLE);//获取角色信息
        ViewHolder holder;
        if (convertView == null) {//角色是自己
            convertView = View.inflate(mContext, R.layout.chatting_item_msg_text, null);
            holder = new ViewHolder();
            holder.mOwnerName = (TextView) convertView.findViewById(R.id.tv_owner_username);//我的名字
            holder.mOtherName = (TextView) convertView.findViewById(R.id.tv_other_username);
            holder.mDate = (TextView) convertView.findViewById(R.id.tv_send_time);//时间
            holder.mOwnerHeadImg = (ImageView) convertView.findViewById(R.id.iv_owner_head);//头像
            holder.mOtherHeadImg = (ImageView) convertView.findViewById(R.id.iv_other_head);//头像
            holder.mOwnerText = (TextView) convertView.findViewById(R.id.tv_owner_content);//我的聊天内容
            holder.mOtherText = (TextView) convertView.findViewById(R.id.tv_other_content);//名字
            holder.mOwnerLayout = (LinearLayout) convertView.findViewById(R.id.ll_owner);
            holder.mOtherLayout = (LinearLayout) convertView.findViewById(R.id.ll_other);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (role == ROLE_OWN) {//我
            holder.mOwnerLayout.setVisibility(View.VISIBLE);
            holder.mOtherLayout.setVisibility(View.GONE);
            holder.mOwnerName.setText((String) roleMap.get(KEY_NAME));//设置名字
            holder.mOwnerText.setText(EmoContentUtil.getInstance(mContext).getEmoContent((String) roleMap.get(KEY_TEXT)));
            DBAdapterImage db = new DBAdapterImage(mContext);
            //打开数据库
            db.open();
            //第一步，从数据库中读取出相应数据，并保存在字节数组中
            Cursor cursor = db.queryAllData();
            int index = cursor.getColumnIndex("image");
            if (index != -1) {
                byte[] blob = cursor.getBlob(cursor.getColumnIndex("image"));
                //第二步，调用BitmapFactory的解码方法decodeByteArray把字节数组转换为Bitmap对象
                Bitmap bmp2 = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                //第三步，调用BitmapDrawable构造函数生成一个BitmapDrawable对象，该对象继承Drawable对象，所以在需要处直接使用该对象即可
                @SuppressWarnings("deprecation")
                BitmapDrawable bd = new BitmapDrawable(bmp2);
                holder.mOwnerHeadImg.setImageDrawable(bd);
            }
            db.close();
        } else {
            holder.mOtherLayout.setVisibility(View.VISIBLE);
            holder.mOwnerLayout.setVisibility(View.GONE);
            holder.mOtherName.setText((String) roleMap.get(KEY_NAME));//设置名字
            holder.mOtherText.setText(EmoContentUtil.getInstance(mContext).getEmoContent((String) roleMap.get(KEY_TEXT)));
        }

        holder.position = position;
        holder.mDate.setText((String) roleMap.get(KEY_DATE));//设置日期
        ClickListener listenerOwner = new ClickListener(holder.mOwnerText, position);
        holder.mOwnerText.setOnClickListener(listenerOwner);

        ClickListener listenerOther = new ClickListener(holder.mOtherText, position);
        holder.mOtherText.setOnClickListener(listenerOther);

        if (listenerOwner != null) {
            if ((Boolean) roleMap.get(KEY_SHOW_MSG)) {
                listenerOwner.hideMessage();
            } else {
                listenerOwner.showMessage();
            }
        }
        if (listenerOther != null) {
            if ((Boolean) roleMap.get(KEY_SHOW_MSG)) {
                listenerOther.hideMessage();
            } else {
                listenerOther.showMessage();
            }
        }

        return convertView;
    }


    private class ClickListener implements OnClickListener {
        private TextView mView;
        private int mPosition;

        public ClickListener(TextView v, int position) {
            mView = v;
            mPosition = position;
        }

        public void showMessage() {
            mView.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        public void hideMessage() {
            mView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }

        @Override
        public void onClick(View v) {

            boolean isShow = (Boolean) mDatalist.get(mPosition).get(KEY_SHOW_MSG);
            if (isShow) {
                mView.setTransformationMethod(PasswordTransformationMethod.getInstance());//设置隐藏内容
                mDatalist.get(mPosition).put(KEY_SHOW_MSG, false);
            } else {
                mView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());//显示内容
                mDatalist.get(mPosition).put(KEY_SHOW_MSG, true);
            }
        }

    }

//    //<emo001]
//    private Spanned getEmoContent(String msg) {
//        String htmlStr = msg;
//        while (true) {
//            int start = htmlStr.indexOf("<emo", 0);
//            if (start != -1) {
//                String resIdStr = htmlStr.substring(start + 1, start + 7);
//                htmlStr = htmlStr.replaceFirst("<emo...>", "<img src='" + resIdStr + "'/>");
//            } else {
//                return Html.fromHtml(htmlStr, imgGetter, null);
//            }
//        }
//    }
//
//    private ImageGetter imgGetter = new ImageGetter() {
//        @Override
//        public Drawable getDrawable(String source) {
//            int resID = mContext.getResources().getIdentifier(source, "drawable", mContext.getPackageName());
//            Drawable drawable = mContext.getResources().getDrawable(resID);
//            int w = (int) (drawable.getIntrinsicWidth() * dm.density / 2);
//            int h = (int) (drawable.getIntrinsicHeight() * dm.density / 2);
//            drawable.setBounds(0, 0, w, h);
//            return drawable;
//        }
//    };

    class ViewHolder {
        public TextView mOwnerName, mOwnerText, mDate, mOtherName, mOtherText;
        public ImageView mOwnerHeadImg, mOtherHeadImg;
        public LinearLayout mOwnerLayout, mOtherLayout;
        public int position;

        public ViewHolder() {
        }
    }

}
