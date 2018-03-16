package com.example.a1mymon.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

/**Thread for blue-tooth device discovery
 * Created by 1mymon on 30/01/2017.
 */

 public class DiscoverDevice extends AsyncTask<BluetoothAdapter,Void,String> {


    BluetoothAdapter deviceAdapter;
    @Override
    protected String doInBackground(BluetoothAdapter... bluetoothAdapters) {
        deviceAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceAdapter.startDiscovery();
        return null;
    }

    @Override
    protected void onPostExecute(String s) {

        super.onPostExecute(s);
    }
}
