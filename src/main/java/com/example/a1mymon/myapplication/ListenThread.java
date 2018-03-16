package com.example.a1mymon.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**Thread for listening to incoming connection requests
 * Created by 1mymon on 22/03/2017.
 */

public class ListenThread extends AsyncTask<Void,Void,BluetoothSocket> {

    BluetoothServerSocket btServerSocket;
    BluetoothSocket btSocket;
    Context prvContext;
    UUID myUuid;


    public ListenThread(Context prvContext)
    {
        this.prvContext = prvContext;
        btSocket = null;
        myUuid = new UUID(9191,9191);

    }

    /**
     * Builds a secure RFCOMM channel for to listen to
     * @return the new active blue-tooth socket
     */
    @Override
    protected BluetoothSocket doInBackground(Void... voids) {

        try {
           // btSocket = sockets[0];
            btServerSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("BTME_SERVER",myUuid) ;

            if (btServerSocket == null)
            btServerSocket = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord("BTME_SERVER",myUuid) ;

            btSocket = btServerSocket.accept();

            //return btSocket;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                btServerSocket.close();
                btSocket.close();
            } catch (IOException e1) {
                Log.e("Closing Sockets :", " Failed");
            }
            e.printStackTrace();
        }
        return btSocket;
    }

    @Override
    protected void onPostExecute(BluetoothSocket socket) {

        try {
            if (socket.isConnected()) {
                //Intent intent = new Intent(prvContext,ChatActivity.class);
                //prvContext.startActivity(intent);
                //Toast.makeText(prvContext, "Connected!", Toast.LENGTH_SHORT).show();
                btServerSocket.close();
            }
                else {
                Toast.makeText(prvContext, "Cant Connect..", Toast.LENGTH_SHORT).show();
                btServerSocket.close();
                btSocket.close();
            }

        }
        catch (Exception e)
        {e.printStackTrace();}


        super.onPostExecute(socket);
    }

    @Override
    protected void onCancelled() {

        if (btServerSocket != null){
            try {
                btServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
        super.onCancelled();
    }
}
