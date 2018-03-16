package com.example.a1mymon.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.PasswordAuthentication;
import java.security.spec.ECField;
import java.util.Dictionary;
import java.util.UUID;

/**Thread for establishing a connection
 * Created by 1mymon on 03/02/2017.
 */

public class ConnectThread extends AsyncTask<BluetoothDevice,Void,BluetoothSocket>{

    Context prvContext;
    BluetoothSocket btSocket;

    UUID remoteUuid;
    public ConnectThread(){}
    public ConnectThread(UUID uuid, Context context)
    {
        this.remoteUuid = uuid;
        prvContext = context;
    }

    /**
     * Uses the device parameter and establishes secure connection
     * @param devices
     * @return
     */
    @Override
    protected BluetoothSocket doInBackground(BluetoothDevice... devices) {


        BluetoothDevice btDevice = devices[0];
        UUID uuid = new UUID(9191, 9191);
        remoteUuid = uuid;


        try {

        /*  if (remoteUuid == null) {
                for (ParcelUuid uid : btDevice.getUuids()) {
                    if (uid.getUuid() == uuid) {
                        remoteUuid = uid.getUuid();
                        break;
                    }
                }
           }
*/
            if (remoteUuid != null ) {

                if (true)
                btSocket = (BluetoothSocket) btDevice.createRfcommSocketToServiceRecord(remoteUuid);

                else if (btSocket == null)
                btSocket = (BluetoothSocket) btDevice.createInsecureRfcommSocketToServiceRecord(remoteUuid);

                else if (btSocket == null) {
                    try {
                        Method m = null;
                        m = btDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                        btSocket = (BluetoothSocket) m.invoke(btDevice, 1);

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
                // }
            }
                ConnectToSocket();
                //return btServerSocket;

            return btSocket;
                // }
        } catch (Exception e) {
            try {
                btSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return btSocket;
        }

    }

    /**
     * Makes the actual connection on the secure RFCOMM channel
     */
    private void ConnectToSocket() {

        if (!BluetoothAdapter.getDefaultAdapter().isDiscovering() && !btSocket.isConnected()) {
            try {
                       try{
                           btSocket.connect();
                    }
                    catch (Exception e)
                    {
                        Log.e("problem","with connection!!!");
                        btSocket.close();
                        e.printStackTrace();
                        // break;
                        }
                    //}
            }
            catch (Exception e) {
                Log.e("problem","with connection!!!");
                e.printStackTrace();

            }

        }
    }

    @Override
    protected void onPostExecute(BluetoothSocket socket) {

        try {
            if (socket.isConnected());
            else
            {

            }
                //Toast.makeText(prvContext, "Connected!!!", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
            super.onPostExecute(socket);
    }
}
