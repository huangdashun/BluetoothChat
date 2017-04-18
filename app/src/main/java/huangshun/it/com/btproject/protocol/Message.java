package huangshun.it.com.btproject.protocol;

/**
 * Created by hs on 2017/3/17.
 * 消息实体封装类
 */
public class Message {
    public byte type;
    public int total;
    public int length;
    public String msg;
    public String fileName;
    public String remoteDevName;
}
