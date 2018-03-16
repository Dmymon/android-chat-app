package com.example.a1mymon.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

/**
 * Service for the server operations
 */
public class ServerService extends Service {

    private static ServerService activeService;
    BluetoothSocket btSocket;
    Context prvContext;

    ServerSide serverSide;
    ListenThread listenThread;

    public ServerService(){

    }

    /**
     * Constructor
     * @param prvContext the user activity context for sending back messages and results
     * @param socket the socket to listen to (if null opens new socket on-"ListenThread")
     */
    public ServerService(Context prvContext,BluetoothSocket socket) {
        this.prvContext = prvContext;
        listenThread = new ListenThread(this.prvContext);
        this.btSocket = socket;
        activeService = this;


    }

    public static ServerService GetServiceInstance()
    {
        return activeService;
    }

    /**
     * Process socket status and create one if needed
     */
    public void StartListen() {

        if (btSocket == null) {
          final Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    final AsyncTask<Void, Void, BluetoothSocket> listenTask = listenThread.execute();

                    int i = 0;
                    while(i<4) {
                        try {
                            //if (listenTask.getStatus().equals(AsyncTask.Status.FINISHED))
                            btSocket = listenTask.get(1000, TimeUnit.MILLISECONDS);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        i++;
                        if (btSocket != null) {
                            if (btSocket.isConnected())
                            {
                                Toast.makeText(prvContext,"Server Connected",Toast.LENGTH_SHORT).show();
                                StartReceive();
                                break;
                            }


                        }
                    }
                }
            });
            t.run();

        }
        else {
            try {
                if (btSocket.isConnected())
                    StartReceive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Starts the data receiving on the socket
     */
    private void StartReceive() {
        Toast.makeText(prvContext, "Start Receiving", Toast.LENGTH_SHORT).show();

        serverSide = new ServerSide(btSocket);
        serverSide.SetContext(prvContext);
        final AsyncTask<String, Void, String> async = serverSide.execute("RECEIVE");
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void SendMessage(String message)
    {
        serverSide = new ServerSide(btSocket);
        serverSide.execute("SEND",message);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

/*
    public void SendIntent(String message)
    {
        Intent intent = new Intent("NEW_MESSAGE");
        intent.putExtra("MESSAGE",message);
        startActivity(intent);
    }
*/
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    public void onDestroy() {
        try {
            if (serverSide != null)
                serverSide.cancel(true);
               // btServerSocket.close();
            if (listenThread !=null)
                listenThread.cancel(true);

           // if (btSocket != null)
            //btSocket.close();
        }
        catch (Exception e){e.printStackTrace();}
        super.onDestroy();
    }
}
