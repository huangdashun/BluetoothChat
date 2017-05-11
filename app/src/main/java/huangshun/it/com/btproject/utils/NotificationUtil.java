package huangshun.it.com.btproject.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import huangshun.it.com.btproject.R;

/**
 * Created by hs on 2017/3/27.
 */
public class NotificationUtil {
    public static final int NOTIFY_ID1 = 1001;
    private static NotificationManager nm;

    public static void notifyMessage(Context context, String msg, Activity activity) {
        //Notification builder;
        PendingIntent contentIntent = null;

        // 发送通知需要用到NotificationManager对象
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 消息对象
        Intent notificationIntent = new Intent(context, activity.getClass());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        //定义通知栏展现的内容信息
        int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        Notification.Builder builder = new Notification.Builder(context);
        Notification notification = builder.setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .setTicker(msg)
                .setWhen(when)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setVibrate(new long[]{300, 500})
                .setContentTitle("BluetoothChat")
                .setContentText(msg)
                .setAutoCancel(true).build();
        // 参数1通知的ID，参数2发送哪个通知
        nm.notify(NOTIFY_ID1, notification);
    }

    public static void clearNotify() {
        nm.cancelAll();
    }
}
