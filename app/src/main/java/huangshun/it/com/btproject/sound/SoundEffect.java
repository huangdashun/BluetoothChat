package huangshun.it.com.btproject.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Build;

import java.util.HashMap;

import huangshun.it.com.btproject.R;

import static android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION;

/**
 * Created by hs on 2017/3/25.
 * 音频播放工具类
 */
public class SoundEffect implements OnLoadCompleteListener {
    private static final String TAG = "SoundEffect";
    public static final int SOUND_SEND = 0;//发送
    public static final int SOUND_RECV = 1;//接收
    public static final int SOUND_ERR = 2;//错误
    public static final int SOUND_PLAY = 3;//开启服务
    private static SoundEffect mSound;
    private SoundPool mSoundPool;
    private int mLoadNum = 0;
    private HashMap<Integer, Integer> mSoundMap;//存放音频的集合

    public static SoundEffect getInstance(Context context) {
        if (mSound == null) {
            mSound = new SoundEffect(context);
        }
        return mSound;
    }

    private SoundEffect(Context context) {
        mSoundMap = new HashMap<Integer, Integer>();
        // SoundPool(int maxStreams, int streamType, int srcQuality)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//如果编译版本大于21,即安卓5.0
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(new AudioAttributes.Builder().setContentType(CONTENT_TYPE_SONIFICATION).build())
                    .build();
        } else {
            mSoundPool = new SoundPool(2, AudioManager.STREAM_SYSTEM, 0);
        }
        // load(Context context, int resId, int priority)
        mSoundMap.put(SOUND_SEND, mSoundPool.load(context, R.raw.send, 1));
        mSoundMap.put(SOUND_RECV, mSoundPool.load(context, R.raw.recv, 1));
        mSoundMap.put(SOUND_ERR, mSoundPool.load(context, R.raw.error, 1));
        mSoundMap.put(SOUND_PLAY, mSoundPool.load(context, R.raw.play_completed, 1));
        mSoundPool.setOnLoadCompleteListener(this);
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        mLoadNum++;
    }

    /**
     * 0：send sound
     * 1: recv sound
     * 2: error sound
     * 3: play sound
     *
     * @param idx
     */
    public void play(int idx) {
        if (idx > mSoundMap.size() || idx < 0)
            return;
        if (mLoadNum < 4)
            return;
        // play(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate)
//        soundID：Load()返回的声音ID号
//        leftVolume：左声道音量设置
//        rightVolume：右声道音量设置
//        priority：指定播放声音的优先级，数值越高，优先级越大。
//        loop：指定是否循环：-1表示无限循环，0表示不循环，其他值表示要重复播放的次数
//        rate：指定播放速率：1.0的播放率可以使声音按照其原始频率，而2.0的播放速率，可以使声音按照其 原始频率的两倍播放。如果为0.5的播放率，则播放速率是原始频率的一半。播放速率的取值范围是0.5至2.0。
        mSoundPool.play(mSoundMap.get(idx), 1, 1, 0, 0, 1);
    }
}
