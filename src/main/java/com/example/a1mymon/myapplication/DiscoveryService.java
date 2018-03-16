package com.example.a1mymon.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

/**Service for handling the blue-tooth device discovery Thread
 */
public class DiscoveryService extends Service {

    IBinder myBinder;
    public DiscoveryService() {

        myBinder = new LocalBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  myBinder;
    }


    /**
     * Initialize the discovery thread and calls execute on it
     */
    public void Discover() {
        DiscoverDevice dd = new DiscoverDevice();
        dd.execute();
    }

    public class LocalBinder extends Binder {

        public DiscoveryService getService()
        {
            return DiscoveryService.this;
        }
    }
}
