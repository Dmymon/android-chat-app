package com.example.a1mymon.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by 1mymon on 05/02/2017.
 */

/**
 * Thread for executing the server side operations
 */
public class ServerSide extends AsyncTask<String,Void,String> {


    DataInputStream din;
    DataOutputStream dout;
    BufferedInputStream buff;

    String message;

    private boolean messageChnged;
    private Context context;
    private BluetoothSocket btSocket;

    private EncryptionComponent encryptor;

    public ServerSide(BluetoothSocket socket) {

        btSocket = socket;
        messageChnged = false;

    }

    /**
     * Starts and maintains the listening for data and messages
     */
    public void StartListen() {
        try {

            try {
                din = new DataInputStream(btSocket.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (btSocket.isConnected()) {
                try {
                    if (din.available()>0) {
                    String Command = din.readUTF();

                        if (Command.compareTo("GET") == 0) {
                            //   System.out.println("\tGET Command Received ...");
                            //   SendFile();
                            continue;
                        }
                        else if (Command.compareTo("KEY") == 0) {
                            //   System.out.println("\tSEND Command Receiced ...");
                            Command = din.readUTF();
                            InitializeSecretKey(Command);
                            continue;
                        }else if (Command.compareTo("SEND") == 0) {
                            //   System.out.println("\tSEND Command Receiced ...");
                            Command = din.readUTF();
                            ReceiveMessage(Command);
                            continue;
                        } else if (Command.compareTo("SEND M") == 0) {
                            //   System.out.println("\tSEND Command Receiced ...");

                            ReceiveMessage(Command);
                            continue;
                        } else if (Command.compareTo("DISCONNECT") == 0) {
                            //   System.out.println("\tDisconnect Command Received ...");
                            // System.exit(1);
                        }
                    }
                } catch (Exception ex) {
                    Log.e("Problem in :"," ServerSide Class");
                    ex.printStackTrace();
                }
            }
            //serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(BluetoothAdapter.getDefaultAdapter().getName(), new UUID(9, 9));

        } catch (Exception ioe) {
            Log.e("Problem in :"," ServerSide Class");
            ioe.printStackTrace();
        }

    }

    /**
     * Generates a secretKey for data decryption from received password
     * @param password the received remote device(client) password
     */
    private void InitializeSecretKey(String password) {

        encryptor = new EncryptionComponent(password);
    }


    /**
     * Gets the new incoming message and sends a broadcast for the activities broadcast receivers
     * @param message the new incoming message
     * @throws Exception
     */
    void ReceiveMessage(String message) throws Exception {

        if (message.trim().compareTo("") == 0) {
            messageChnged = false;
        }

        else {
            this.message = encryptor.Decrypt(message);
            messageChnged = true;
            SendBroadcast();
        }

    }
    /*
    public void SendMessage(String message) throws Exception
    {

        try {
            dout = new DataOutputStream(btSocket.getOutputStream());
            dout.writeUTF("SEND");
            dout.flush();

            do
            {
                dout.writeUTF(message);
                dout.flush();
            }
            while(false);
        }
        catch (Exception e)
        {e.printStackTrace();}

    }
*/

    /**
     * Sends the broadcast containing the new message
     */
    private void SendBroadcast() {
        Intent intent = new Intent("NEW_MESSAGE");
        intent.putExtra("REMOTE_DEVICE_ADDRESS",btSocket.getRemoteDevice().getAddress());
        //  intent.setAction("NEW_MESSAGE");
        intent.putExtra("MESSAGE", message);
        context.sendBroadcast(intent);
        doInBackground("RECEIVE");
    }


    @Override
    protected String doInBackground(String...strings) {

        switch (strings[0])
        {
            case "SEND":
            {
                try {
                   // SendMessage(strings[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case "RECEIVE":
            {
                StartListen();
                break;
            }
        }
       // btSocket = sockets[0];
        //StartListen();

        if (strings[0] == "RECEIVE") {
            if (messageChnged)
                return message;
            else
                return null;
        }
        else
            return "sent";
    }

    public void SetContext(Context context){
        this.context=context;
    }
    @Override
    protected void onPostExecute(String message) {

        if (message == "sent")
            SendBroadcast();
            super.onPostExecute(message);

    }
}
