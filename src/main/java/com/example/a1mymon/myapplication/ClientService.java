package com.example.a1mymon.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;

/**Handles the client side requests
 */
public class ClientService extends Service {

    IBinder binder;
   ClientSide client;
    Context prvContext;
    BluetoothSocket btSocket;

    EncryptionComponent encryptor;


    public ClientService(){}

    /**Constructor
     *
     * @param socket active socket for sending data
     * @param context context to route back results
     */
    public ClientService(BluetoothSocket socket, Context context) {

        btSocket = socket;
        prvContext = context;
        binder = new LocalBinder();

        encryptor = new EncryptionComponent();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
         //   SendMessage(intent.getStringExtra("MESSAGE_2_SEND"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Initialize the Thread for sending a message
     * @param message
     * @throws Exception
     */
    void SendMessage(String message) throws Exception
    {
        client = new ClientSide(btSocket,prvContext);
        String s = encryptor.Encrypt(message);
        client.execute(s);

    }

    /**
     * Shares the secret dtat encryption key with server
     * @throws Exception
     */
    void EstablishSecretKeyExchange() throws Exception
    {
        client = new ClientSide(btSocket,prvContext);
        client.execute("KEY",encryptor.GetSecret());

    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {

        public ClientService getService()
        {
            return ClientService.this;
        }
    }
}
