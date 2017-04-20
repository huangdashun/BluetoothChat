package huangshun.it.com.btproject.view;

import android.app.Activity;
import android.content.Context;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

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
        if (convertView == null) {
            if (role == ROLE_OWN) {//角色是自己
                convertView = View.inflate(mContext, R.layout.chatting_item_msg_text_right, null);
            } else {//接收到别人发来的信息
                convertView = View.inflate(mContext, R.layout.chatting_item_msg_text_left, null);
            }
            holder = new ViewHolder();
            holder.mText = (TextView) convertView.findViewById(R.id.tv_chat_content);//聊天内容
            holder.mDate = (TextView) convertView.findViewById(R.id.tv_send_time);//时间
            holder.mHearImg = (ImageView) convertView.findViewById(R.id.iv_user_head);//头像
            holder.mName = (TextView) convertView.findViewById(R.id.tv_username);//名字
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;
        holder.mName.setText((String) roleMap.get(KEY_NAME));//设置名字
        holder.mText.setText(EmoContentUtil.getInstance(mContext).getEmoContent((String) roleMap.get(KEY_TEXT)));
        holder.mDate.setText((String) roleMap.get(KEY_DATE));//设置日期
        ClickListener listener = new ClickListener(holder.mText, position);
        holder.mText.setOnClickListener(listener);
        if (listener != null) {
            if ((Boolean) roleMap.get(KEY_SHOW_MSG)) {
                listener.hideMessage();
            } else {
                listener.showMessage();
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
        public TextView mName, mText, mDate;
        public ImageView mHearImg;
        public int position;

        public ViewHolder() {
        }
    }

}
