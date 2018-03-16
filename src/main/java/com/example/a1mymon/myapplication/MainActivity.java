package com.example.a1mymon.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**The startup activity contains the contacts list and options
 */
public class MainActivity extends AppCompatActivity {

    private static final String SP_NAME = "MySP" ;

    private ArrayList<BluetoothDevice> btDevices;
    private ArrayAdapter<String> btDevices_Adp;
    private BluetoothAdapter btAdapter;
    private BroadcastReceiver btReceiver;

    private ListView chatsList;
    private LinearLayout chatArea;

    private ListView deviceOnPopUp;

    private TextView title;
    PopupWindow popUp;

    Toolbar myTitle;

    private DiscoveryService btService;
    private int REQUEST_ENABLE;
    private int DISCOVERY_REQUEST;
    private int STOP_DISCOVERY_REQUEST;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private LinearLayout popUpContainer;
    private boolean isConnected = false;

    ServerService serverService;

    ArrayList<String> devicesOnListView;
    SharedPreferences mySP;
    private Intent serverIntent;

    //Settings frame Elements
    //
    private Button settingsBtn;
    private Button chatsBtn;

    private boolean onChats;
    private EditText adpNameEditText;
    private Button setAdpName;
    private LinearLayout settingsLayout;
    private Button deleteContacts;
    private ImageButton listenBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //construct/get Shared Preferences
        //
        mySP = this.getSharedPreferences(SP_NAME,MODE_PRIVATE);

        /**
         * Gets blue-tooth related events
         */
        btReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Toast.makeText(getBaseContext(), "found", Toast.LENGTH_SHORT).show();

                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    AddDevicesToDeviceList(newDevice);

                }
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (btDevices.size() <= 0)
                        Toast.makeText(getBaseContext(), "Couldnt find any devices", Toast.LENGTH_SHORT).show();

                    else {
                        btAdapter.cancelDiscovery();
                        showConnection();
                    }
                }
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Toast.makeText(getBaseContext(), "Looking For new Devices", Toast.LENGTH_SHORT).show();
                }
                if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action))
                {
                    int connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,-1);
                    if (connectionState==BluetoothProfile.STATE_CONNECTED) {

                        //isConnected = !isConnected;
                        //   if (isConnected)
                        Toast.makeText(getBaseContext(), "Connected!", Toast.LENGTH_SHORT).show();
                        StartChat((BluetoothDevice)(intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_DEVICE)[0]));
                    }
                        else
                        Toast.makeText(getBaseContext(),"Disconnected!",Toast.LENGTH_SHORT).show();
                }
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
                {
                    // String bondState = intent.getStringExtra(BluetoothDevice.EXTRA_BOND_STATE);
                   // if (bondState == "BOND_BONDED")

                }
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction()))
                {
                    BluetoothDevice bd = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Toast.makeText(getBaseContext(),"Connecting To: " + bd.getName(),Toast.LENGTH_SHORT).show();
                    StartServer();

                }
                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction()))
                    Toast.makeText(getBaseContext(),"Disconnectd",Toast.LENGTH_SHORT).show();


                if (action == "NEW_MESSAGE")
                {
                    //Toast.makeText(getBaseContext(),"got new message!",Toast.LENGTH_SHORT).show();
                    serverService.StartListen();
                }
            }
        };

        //register bluetooth receiver to receive following
        //
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter filter4 = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        IntentFilter filter5 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter filter6 = new IntentFilter(BluetoothDevice.ACTION_UUID);
        IntentFilter filter7 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter8 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        IntentFilter filter9 = new IntentFilter("NEW_MESSAGE");

        registerReceiver(btReceiver, filter);
        registerReceiver(btReceiver, filter2);
        registerReceiver(btReceiver, filter3);
        registerReceiver(btReceiver,filter4);
        registerReceiver(btReceiver,filter5);
        registerReceiver(btReceiver,filter6);
        registerReceiver(btReceiver, filter7);
        registerReceiver(btReceiver, filter8);
        registerReceiver(btReceiver, filter9);
        //


        chatsBtn = (Button)findViewById(R.id.chatsBtn);
        chatsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!onChats) {
                    chatArea.removeView(settingsLayout);
                    chatArea.addView(chatsList, 1);

                }
                    onChats = true;
            }
        });
        settingsBtn = (Button)findViewById(R.id.settingsButton);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onChats) {
                    chatArea.removeView(chatsList);
                    chatArea.addView(settingsLayout,1);

                }
                onChats = false;

            }
        });


        //customed variable initialize method
        //
        //chatsBtn.callOnClick();

        LoadContent();
        //


       // StartServer();
      //  if (btAdapter != null && !(btAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)) {
       //     Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
       //     startActivity(discoverable);
     //   }
        //
        myTitle = (Toolbar)findViewById(R.id.myTitle);
       // myTitle.showOverflowMenu();
        int left=(getWindow().getAttributes().width-title.getWidth())/2;
        setSupportActionBar(myTitle);



        //this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.layout_title);//TO CHECK!!


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private final ServiceConnection btServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DiscoveryService.LocalBinder binder = (DiscoveryService.LocalBinder)iBinder;
            btService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            Log.e("","service disconnected");
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Initialize activity layout/behavioral objects
     */
    private void LoadContent() {

        //The Settings Fields
        //
        settingsLayout = new LinearLayout(this);

        onChats = true;

        chatArea = (LinearLayout) findViewById(R.id.chatArea);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btDevices = new ArrayList<BluetoothDevice>();

        //fill the devices list- btDevices
        //
        if (mySP.contains("DEVICE_ADDRESS"))
        {
            Set<String> devicesSet =  mySP.getStringSet("DEVICE_ADDRESS",new HashSet<String>());
            ArrayList<String> devicesList = new ArrayList<String>(devicesSet);
            for (String deviceAdd : devicesList)
            {
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAdd);
                btDevices.add(device);

            }
        }
        //

        popUp = new PopupWindow(this);
        popUpContainer = (LinearLayout)findViewById(R.id.toPopUp);
        popUp.setContentView(popUpContainer);


        deviceOnPopUp = (ListView)findViewById(R.id.deviceList);
        btService = new DiscoveryService();


        //restore known contacts
        //
        if (mySP.contains("DEVICE_LIST")) {
            Set<String> set = mySP.getStringSet("DEVICE_LIST", new HashSet<String>());
            devicesOnListView = new ArrayList<String>(set);
        }
        else
            devicesOnListView = new ArrayList<String>();

        chatsList = (ListView) findViewById(R.id.chatList);
        btDevices_Adp = new ArrayAdapter<String>(chatsList.getContext(),R.layout.chat_list_item, devicesOnListView);
        chatsList.setAdapter(btDevices_Adp);

        chatsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView)view;

                ArrayList<String> deviceFromSP ;
                if (mySP.contains("DEVICE_ADDRESS")) {
                    for (BluetoothDevice bd : btDevices) {
;                            if (bd.getName().equals(tv.getText())) {
                            Toast.makeText(getBaseContext(),bd.getAddress(),Toast.LENGTH_SHORT).show();
                            StartChat(bd);
                            break;
                        }

                    }

                }
            }
        });

        title = (TextView)findViewById(R.id.main_title_text);
        int left=(getWindow().getAttributes().width-title.getWidth())/2;


        adpNameEditText = (EditText)findViewById(R.id.adpName_edit);
        setAdpName = (Button)findViewById(R.id.setAdpName_btn);
        setAdpName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (BluetoothAdapter.getDefaultAdapter().setName(adpNameEditText.getText().toString()))
                        Toast.makeText(getBaseContext(),"Name Updated!",Toast.LENGTH_SHORT).show();
                else
                        Toast.makeText(getBaseContext(),"Cant Edit",Toast.LENGTH_SHORT).show();

            }
            });

        deleteContacts = (Button)findViewById(R.id.deleteContacts_btn);
        deleteContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<BluetoothDevice> temp = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice bd:btDevices)
                {
                    if (devicesOnListView.contains(bd.getName()))
                        temp.add(bd);

                }
                ShowDevicesPopUp(temp,"DELETE");
            }
        });

        listenBtn = (ImageButton)findViewById(R.id.listenBtn);
        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // StartServer();
            }
        });
       // title.layout(left,title.getTop(),title.getRight(),title.getBottom());
        //chatsList.setAdapter(btDevices_Adp);
        //
        checkForConnections();
        //



    }

    /**
     * Initiate ChatActivity instance for chatting with remote device
     * @param remoteDevice the device to start the chat with
     */
    private void StartChat(BluetoothDevice remoteDevice) {
        Intent startChat = new Intent(getBaseContext(),ChatActivity.class);
        startChat.putExtra("REMOTE_DEVICE_ADDRESS",remoteDevice.getAddress());
        startActivity(startChat);
    }

    private void checkForConnections() {

        if (btAdapter == null)
            Toast.makeText(this, "Theres No Bluetooth Adapter On This Device!", Toast.LENGTH_SHORT).show();

        else if (!btAdapter.isEnabled()) {

            Intent rqstBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(rqstBT, REQUEST_ENABLE);
        }
        //
        //?????????????????????????????????????
         if (btAdapter.getBondedDevices().size() <= 0) {
           //  Intent lookForDevice = new Intent(this,DiscoveryService.class);
           //  bindService(lookForDevice,btServiceConnection,Context.BIND_AUTO_CREATE);
            //startService(lookForDevice);
             // btService.Discover();

          //  Intent pairBT = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
          //  startActivityForResult(pairBT, REQUEST_ENABLE);
        }
       // else
             //FillChatList();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE)
        {
            if (resultCode == RESULT_OK) {
                if (btAdapter.getBondedDevices().size() <= 0) {

                    // btService.Discover();
                    // Toast.makeText(this, "Looking for devices", Toast.LENGTH_SHORT).show();
                } else  {
                    for (BluetoothDevice bd : btAdapter.getBondedDevices()) {
                        if (!btDevices.contains(bd))
                            AddDevicesToDeviceList(bd);

                    }
                    showConnection();
                }
            }

        }
        if (data!=null && data.hasExtra("CHOSEN_DEVICE")) {
            BluetoothDevice chosenDevice = (BluetoothDevice) data.getParcelableExtra("CHOSEN_DEVICE");

            if (resultCode == RESULT_OK) {
                SaveDeviceListView();
                FillChatList(chosenDevice);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                DeleteFromChatList(chosenDevice);
                RemoveDeviceFromDeviceList(chosenDevice);
                SaveDeviceListView();
            }
        }
    }

    /**
     * Deletes contacts from the contacts list
     * @param bd the contacts device to remove from the list
     */
    private void DeleteFromChatList(BluetoothDevice bd) {
        if (devicesOnListView.contains(bd.getName())) {
            devicesOnListView.remove(bd.getName());
            btDevices_Adp.notifyDataSetChanged();
        }

    }

    /**
     * Display the new contacts (if exist) after device discovery
     */
    private void showConnection()
    {
        if (btDevices.size() > 0) {
            ArrayList<BluetoothDevice> temp = new ArrayList<BluetoothDevice>();
            for (BluetoothDevice bd : btDevices)
            {
                if (devicesOnListView.contains(bd.getName()))
                    continue;
                else
                    temp.add(bd);
            }
            if (temp.size()>0) {
                ShowDevicesPopUp(temp,"ADD");
            }
            else {
                Toast.makeText(this, "Theres No New Devices", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * Activity for the new contacts window from which the user can choose new contacts to add
     * @param array The devices found in the device discovery process
     * @param action The intended action for the contact choose (add/delete)
     */
    private void ShowDevicesPopUp(ArrayList<BluetoothDevice> array,String action) {
        Intent popUp = new Intent(this, PopUpWindow.class);
        popUp.setAction(action);
        popUp.putExtra("DEVICE_LIST", array);
        startActivityForResult(popUp, 1);
    }

    /**
     * Removes a specific device from the device list (contact list sync with the device list when required)
     * @param device2Remove
     */
    private void RemoveDeviceFromDeviceList(BluetoothDevice device2Remove)
    {
        if (btDevices.contains(device2Remove))
            btDevices.remove(device2Remove);
    }

    /**
     * Adds a device to the device list (contact list sync with the device list when required)
     * @param device2Add
     */
    private void AddDevicesToDeviceList(BluetoothDevice device2Add)
    {
        if (!btDevices.contains(device2Add))
            btDevices.add(device2Add);
    }



    public void AddNewContact(MenuItem item) {

            Intent lookForDevice = new Intent(this, DiscoveryService.class);
            startService(lookForDevice);
            btService.Discover();
    }


    /**
     * Adds contacts to the contacts list
     * @param bd the device to add (by name)
     */
    private void FillChatList(BluetoothDevice bd) {
        if (!devicesOnListView.contains(bd)) {
            devicesOnListView.add(bd.getName());
            btDevices_Adp.notifyDataSetChanged();
        }
    }

    /**
     * For every activity onStop/onDestroy save the current contacts list
     * uses SharedPreferences
     */
     private void SaveDeviceListView()
     {
         SharedPreferences.Editor editor = mySP.edit();
         Set<String> set = new HashSet<String>();
         set.addAll(devicesOnListView);
         editor.putStringSet("DEVICE_LIST",set);

         //editor.commit();
         Set<String> addrSet = new HashSet<String >();
         if (btDevices.size()>0) {
             for (BluetoothDevice bd : btDevices) {
                 addrSet.add(bd.getAddress());
             }
             editor.putStringSet("DEVICE_ADDRESS", addrSet);
         }
         editor.apply();

     }

    /**
     * Method for starting the blue-tooth server
     */
    private void StartServer() {
        if (serverService==null) {
            Toast.makeText(this, "Connecting to Server..", Toast.LENGTH_LONG).show();
            serverIntent = new Intent(getBaseContext(), ServerService.class);
            startService(serverIntent);
            serverService = new ServerService(this, null);
            serverService.StartListen();
        }
         else if (serverService.btSocket!=null&&serverService.btSocket.isConnected())
                Toast.makeText(this, "Allready Connected..", Toast.LENGTH_LONG).show();
        else {
            serverService.stopSelf(); //stopService(serverIntent);
            Toast.makeText(this, "Trying Again..", Toast.LENGTH_LONG).show();
            serverService = new ServerService(this, null);
            startService(serverIntent);
            serverService.StartListen();

        }



    }
    @Override
    protected void onStop() {
        SaveDeviceListView();
        try {
            unregisterReceiver(btReceiver);
        }
        catch (Exception e)
        {e.printStackTrace();}
            try {
           // if (serverIntent != null)
           // stopService(serverIntent);
           // Log.e("Server","Disconnected");
        }
        catch (Exception e)
        {e.printStackTrace();}
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        SaveDeviceListView();
        try {
            stopService(serverIntent);
            unregisterReceiver(btReceiver);
        }
        catch (Exception e)
        {e.printStackTrace();}
        Log.e("Out","Main Activity");
            super.onDestroy();

    }
}

