package com.example.a1mymon.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.UUID;

/**
 * Activity for handling the display and behavior of the contacts add/remove pop-up window
 */
public class PopUpWindow extends AppCompatActivity {

    private ListView deviceOnPopUp;
    private ArrayAdapter<String> ada;
    private TextView popUpTitle;
    private Intent sendBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_window);

        //To make bg dimmer
        //*******************************************
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int height = dm.heightPixels;
        int width = dm.widthPixels;
        getWindow().setLayout((int)(width*.8),(int)(height*.5));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.5f;
        //*********************************************

        popUpTitle = (TextView)findViewById(R.id.popUpTitle);
        sendBack = new Intent();
        switch (getIntent().getAction())
        {
            case "DELETE":
            {
                popUpTitle.setText(R.string.popUp_Delete);
                setResult(RESULT_CANCELED,sendBack);
                break;
            }
            case "ADD":
            {
                popUpTitle.setText(R.string.popUp_Add);
                setResult(RESULT_OK,sendBack);
                break;
            }
        }

        //The device list on the window
        deviceOnPopUp = (ListView)findViewById(R.id.deviceList2);

        //Gets the devices from the intent for further actions
        final ArrayList<BluetoothDevice> toIterate = getIntent().getParcelableArrayListExtra("DEVICE_LIST");

        deviceOnPopUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view;
                for (BluetoothDevice bd : toIterate)
                {
                    if (bd.getName().equals(tv.getText())) {
                        sendBack.putExtra("CHOSEN_DEVICE", bd);
                        break;
                    }
                }

            }
        });



        ShowDiscoveredDevices(toIterate);

    }

    /**
     * process the data to display (device name) and displays it
     * @param toIterate devices found in the discovery process
     */
    private void ShowDiscoveredDevices(ArrayList<BluetoothDevice> toIterate) {
        ArrayList<String> newDeviceToConnect = new ArrayList<String>();


        ada = new ArrayAdapter<String>(deviceOnPopUp.getContext(),R.layout.pop_up_item,newDeviceToConnect);
        if (getIntent() != null) {
            for (BluetoothDevice bd : toIterate) {
                newDeviceToConnect.add(bd.getName());
            }
            deviceOnPopUp.setAdapter(ada);

        }
    }
}
