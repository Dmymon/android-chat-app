package com.example.a1mymon.myapplication;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**Handles the messaging and connecting to remote device
 */
public class ChatActivity extends AppCompatActivity {


    //chat list
    private ArrayAdapter<String> adp;
    private ArrayList<String> messages;
    //

    private Toolbar myTitle;
    private ListView messageList;
    private EditText typingBox;
    private Button sendBtn;
    private ImageButton delete;
    private TextView titleText;
    private ImageButton syncBtn;

    private static boolean startConnection = false;
    BluetoothDevice remoteDevice;
    ConnectThread connectThread;
    BluetoothSocket socket;

    ServerSide server;
    ClientService clientService;
    BroadcastReceiver messageReceiver;
    ServerService serverService;

    String newMessage;
    Intent clientServiceIntent;

    private BluetoothSocket serverSocket;
    private ServerService serverInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

          messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("NEW_MESSAGE"))
                {
                    newMessage = intent.getStringExtra("MESSAGE");
                    //Toast.makeText(getBaseContext(),newMessage,Toast.LENGTH_SHORT).show();
                    AddMessageToChat(newMessage);
                }
                if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction()))
                {
                    int connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,-1);
                    if (connectionState== BluetoothProfile.STATE_CONNECTED) {

                        //isConnected = !isConnected;
                        //   if (isConnected)
                        Toast.makeText(getBaseContext(), "Connected!", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(getBaseContext(),"Disconnected!",Toast.LENGTH_SHORT).show();
                }
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction()))
                {
                    //else
                       // clientService

                }
            }
        };
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_UUID);
        IntentFilter filter2 = new IntentFilter("NEW_MESSAGE");
        IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);

        registerReceiver(messageReceiver,filter1);
        registerReceiver(messageReceiver,filter2);
        registerReceiver(messageReceiver, filter3);
        registerReceiver(messageReceiver, filter4);
        //TODO:
   /*     messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String newMessage = intent.getStringExtra("NEW_MESSAGE");


            }
        };

        IntentFilter filter1 = new IntentFilter(NEW_MESSAGE_RECEIVED);
        registerReceiver(messageReceiver,filter1);
     */

        clientServiceIntent = new Intent(this,ClientService.class);
        startService(clientServiceIntent);

        //customed varia+ble initialize method
        //
        LoadContent();
        //

        Intent init = getIntent();
        if(init.hasExtra("REMOTE_DEVICE_ADDRESS")) {
            String remoteAddress = init.getStringExtra("REMOTE_DEVICE_ADDRESS");
            remoteDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(remoteAddress);
        }
      /*  if (init.getAction() == "NEW_MESSAGE")
        {
            String message = init.getStringExtra("MESSAGE");
            Intent intent = new Intent(init.getAction());
            intent.putExtra("MESSAGE",message);
            sendBroadcast(intent);
        }
*/

        //if User Enter Chat After Connecting As Server
        //
         serverInstance = ServerService.GetServiceInstance();
        if (serverInstance != null)
          serverSocket = serverInstance.btSocket;
        //

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(typingBox.getText().toString().trim().length()>0) {
                    String message2send = typingBox.getText().toString();
                    AddMessageToChat(message2send);
                    try {
                        if (clientService != null)
                        clientService.SendMessage(message2send);
                       else if (serverInstance != null)
                           serverInstance.SendMessage(message2send);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                typingBox.setText("");
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                typingBox.setText("");
            }
        });

        titleText = (TextView)findViewById(R.id.titleTextView);
        titleText.setText(remoteDevice.getName());
        //int left= 10;
        // titleText.layout(left,titleText.getTop(),titleText.getRight(),titleText.getBottom());
        myTitle = (Toolbar)findViewById(R.id.chatActivityTitle);
        setSupportActionBar(myTitle);

    }

    /**Puts message in the chat window
     * @param s, message to show
     */
    private void AddMessageToChat(String s) {
        messages.add(s);
        adp.notifyDataSetChanged();
    }


    /**Initialize activity objects
     */
    private void LoadContent() {

        messages = new ArrayList<String>();
        adp = new ArrayAdapter<String>(this, R.layout.text_message, R.id.message, messages);

        messageList = (ListView) findViewById(R.id.chatList);
        messageList.setAdapter(adp);

        typingBox = (EditText) findViewById(R.id.typingBox);

        sendBtn = (Button) findViewById(R.id.sendBtn);

        delete = (ImageButton) findViewById(R.id.deleteBtn);
        syncBtn = (ImageButton)findViewById(R.id.syncBtn);
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (remoteDevice.fetchUuidsWithSdp()) {
                    startConnection = true;
                    Connect2RemoteDevice();
                }
            }
        });


    }

    /**Makes the connection with the remote device
     */
    private void Connect2RemoteDevice() {


        connectThread = new ConnectThread(null,this);
        if (!(connectThread.getStatus() == AsyncTask.Status.RUNNING)) {
                //async = connectThread.execute(remoteDevice);
                //blocking task(hence the timeout)
                Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();


              final  Thread t = new Thread(new Runnable() {
                   @Override
                   public void run() {
                       AsyncTask<BluetoothDevice, Void, BluetoothSocket> async;
                       async = connectThread.execute(remoteDevice);
                       //socket = null;
                       int i = 1;
                       while (true) {
                           if (i>3) {
                               Toast.makeText(getBaseContext(), "Connection Failed :(", Toast.LENGTH_LONG).show();
                               break;
                           }
                           try {
                               try {
                                  // if (async.getStatus().equals(AsyncTask.Status.FINISHED))
                                    socket = async.get(1000,TimeUnit.MILLISECONDS);
                               }
                               catch (Exception e) {
                                   e.printStackTrace();
                               }
                               Toast.makeText(getBaseContext(), "Connection trial " + i, Toast.LENGTH_LONG).show();
                               i+=1;
                               if (socket != null) {
                                   if (socket.isConnected()) {
                                       Toast.makeText(getBaseContext(), "Connected!!", Toast.LENGTH_LONG).show();
                                       clientService = new ClientService(socket,getBaseContext());
                                       clientService.EstablishSecretKeyExchange();
                                      // StartServer();
                                       break;
                                   }

                               }
                           } catch (Exception e) {
                               Toast.makeText(getBaseContext(), "Connection Failed :(", Toast.LENGTH_LONG).show();
                               e.printStackTrace();
                           }

                       }
                   }

                   });t.run();

        }


    }


    private void StartServer() {
            startService(new Intent(this, ServerService.class));
            serverService = new ServerService(this, socket);
            serverService.StartListen();

    }


    @Override
    protected void onDestroy() {
        try {
            if (socket!=null) {
                socket.close();
            }
            stopService(new Intent(getBaseContext(),ServerService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterReceiver(messageReceiver);


        Log.e("Out","Chat Activity");

        super.onDestroy();

    }
}



