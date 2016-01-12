package ir.bigandsmall.hiddevice;

/**
 * Created by karimi on 11/23/2015.
 */
public class CSepronik {

    static {
        System.loadLibrary("sepronik");
    }

    public native void OpenDevice(int fd, int endPointIn,int endPointOut);
    public native int ReadMemory(StringBuffer buffer ,int len);
    public native int WriteMemory(String buffer,int len  );
}
