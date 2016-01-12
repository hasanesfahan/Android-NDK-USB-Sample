package ir.bigandsmall.hiddevice;

import android.support.v7.app.AppCompatActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import java.util.HashMap;

public class MainActivity extends AppCompatActivity {



    private static final String ACTION_USB_PERMISSION = "ir.bigandsmall.hiddevice.USB";
    private static final String Tag = "ir.bigandsmall";

    public static Intent intent = null;
    public static PendingIntent permissionIntent;
    public static  UsbDevice deviceSepronik = null;
    private UsbDeviceConnection connectionSepronik = null;
    private CSepronik cSepronik;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = getIntent();
        cSepronik = new CSepronik();
    }

    public void WriteMemClick(View v)
    {
//        EditText buffers =   (EditText) findViewById(R.id.editTextPlainWriteMem) ;
//        String bufWrite =buffers.getText().toString();
//        int nameRet =  cSepronik.WriteMemory(  bufWrite,bufWrite.length());
//
//        if(nameRet == 0)
//            Toast.makeText(getApplicationContext(),"WriteMem Success" ,Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(getApplicationContext(),"WriteMem Error="+nameRet ,Toast.LENGTH_SHORT).show();
    }

    public void ReadMemClick(View v)
    {
//        int len =  Integer.parseInt(((EditText) findViewById(R.id.editTextReadLen)).getText().toString());
//        EditText buffers =   (EditText) findViewById(R.id.editTextPlainReadMem) ;
//
//        StringBuffer evValue=new StringBuffer();
//        int nameRet =  cSepronik.ReadMemory( evValue,len);
//
//        if(nameRet == 0)
//            buffers.setText(evValue.toString());
//        else
//            Toast.makeText(getApplicationContext(),"Read Mem Error="+nameRet ,Toast.LENGTH_SHORT).show();
    }

    public void CloseDeviceClick(View v)
    {
//        int nameRet =  cSepronik.CloseDevice();
//
//        if(nameRet == 0)
//            Toast.makeText(getApplicationContext(),"Close Success" ,Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(getApplicationContext(),"Close Error="+nameRet ,Toast.LENGTH_SHORT).show();
    }

    public void OpenDeviceClick(View v)
    {
        deviceSepronik = null;
        try {
            permissionIntent = PendingIntent.getBroadcast (this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        } catch (Throwable e) {
        }

        final UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if(device == null) {
            final HashMap<String, UsbDevice> usb_device_list = manager.getDeviceList();
            for(final String desc : usb_device_list.keySet()) {
                final UsbDevice candidate = usb_device_list.get(desc);
                if(String.valueOf(candidate.getVendorId()).equals("1234")&&String.valueOf(candidate.getProductId()).equals("45789")) {
                    deviceSepronik = candidate;
                }
            }
            if(deviceSepronik != null)
                manager.requestPermission(deviceSepronik, permissionIntent);
        }

        if(manager.getDeviceList().size()==0)
            Toast.makeText(getApplicationContext(),"Please Insert module Sepronik",Toast.LENGTH_SHORT).show();;
    }


    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ACTION_USB_PERMISSION.equals(action)) {
                synchronized(this) {


                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(deviceSepronik != null) {
                            final UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                            if (deviceSepronik != null && !manager.hasPermission(deviceSepronik)) {
                                manager.requestPermission(deviceSepronik, permissionIntent);
                                return;
                            }


                            UsbInterface intf = null;
                            for (int i = 0; i < deviceSepronik.getInterfaceCount(); i++) {
                                if (deviceSepronik.getInterface(i).getEndpoint(0).getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                                    intf = deviceSepronik.getInterface(i);
                                }
                            }

                            if (intf == null) {
                                Log.e(Tag, "interface is not existed!");
                                return;
                            }


                            int inEndPoint = 0;
                            int outEndPoint = 0;
                            //TODO get endpoint if not static

                            if (deviceSepronik != null) {
                                if(connectionSepronik != null)
                                    connectionSepronik.close();
                                connectionSepronik = manager.openDevice(deviceSepronik);
                                if (connectionSepronik != null && connectionSepronik.claimInterface(intf, true)) {
                                    cSepronik.OpenDevice(connectionSepronik.getFileDescriptor(),inEndPoint,outEndPoint);
                                }

                            }
                        }
                        else {
                            Log.d(Tag, "Android granted USB device permissions but device was lost.");
                        }
                    }
                    else {
                        Log.d(Tag, "Android did not give USB device permissions.");
                    }
                }
            }
        }
    };
}
