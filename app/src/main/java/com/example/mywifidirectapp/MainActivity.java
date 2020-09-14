package com.example.mywifidirectapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.InetAddresses;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SearchEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    Button  btnDiscover, btnsend;
    ListView devicesList;
    TextView wifiStatus, wifiDirectstatus, discoveringPeersStatus,readMsg, deviceConnectionStatus;
    EditText writemsg;
    //request code
    public  static final int REQUEST_CODE=102; //random
    //
    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    //
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    //arraylist of devices found nearby
    List<WifiP2pDevice> peers=new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] devicesArray;

    //
    static  final int message_read=1;
    //objects of classes of data transfer
    Serversideclass serversideclass;
    Clientsideclass clientsideclass;
    SendRecieve sendRecieve;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] perms = {"android.permission.ACCESS_FINE_LOCATION"};
        requestPermissions( perms,REQUEST_CODE);
        bindingandIntents();
        btnDiscover.setOnClickListener(this);
        btnsend.setOnClickListener(this);
    }

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case message_read:
                    byte[] readBuffer= (byte[]) message.obj;
                    String tempmsg=new String(readBuffer,0,message.arg1);
                    writemsg.setText(tempmsg);
                    break;
            }
            return true;
        }
    });

    private void bindingandIntents() {
        //find view by id for all
        wifiStatus=findViewById(R.id.wifistatus);
        wifiDirectstatus=findViewById(R.id.wifidirect_status);
        btnDiscover = findViewById(R.id.btn_discover);
        btnsend=findViewById(R.id.btn_send_msg);
        writemsg=findViewById(R.id.et_msg);
        discoveringPeersStatus=findViewById(R.id.discovering_peers);
        devicesList=findViewById(R.id.listview);
        deviceConnectionStatus=findViewById(R.id.device_connection);
        //
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        broadcastReceiver = new WifiDirectBroadcast(wifiP2pManager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice itemDevice=devicesArray[i];
                WifiP2pConfig config=new WifiP2pConfig();
                config.deviceAddress=itemDevice.deviceAddress;
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"device connnected to"+itemDevice.deviceName,Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int i) {
                            Toast.makeText(getApplicationContext(),"device disconnected",Toast.LENGTH_LONG).show();
                        }
                    });


                }
            }
        });

    }
     WifiP2pManager.ConnectionInfoListener connectionInfoListener=new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress=wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                deviceConnectionStatus.setText("host");
                //thread class instantiated and started
                serversideclass=new Serversideclass();
                serversideclass.start();
            }
            else if(wifiP2pInfo.groupFormed){
                deviceConnectionStatus.setText("client");
                //thread class instantiated and started
                clientsideclass=new Clientsideclass(groupOwnerAddress);
                clientsideclass.start();
            }
        }
    };
    WifiP2pManager.PeerListListener peerListListener =new WifiP2pManager.PeerListListener(){
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Log.d(TAG,"peer listener working");
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                //((WifiP2p) getListAdapter()).notifyDataSetChanged();
                deviceNameArray=new String[peerList.getDeviceList().size()];
                devicesArray=new WifiP2pDevice[peerList.getDeviceList().size()];
                int index=0;
                for(WifiP2pDevice device : peerList.getDeviceList()){
                    deviceNameArray[index]=device.deviceName;
                    devicesArray[index]=device;
                    index++;
                }
                ArrayAdapter<String> peeradapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                devicesList.setAdapter(peeradapter);
            }
            if(peers.size()==0){
                Toast.makeText(getApplicationContext(),"No devices found nearby in range",Toast.LENGTH_LONG).show();
                return;
            }

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M) //requestPermissions require this annotation
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_discover:
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                    wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                        @Override

                        public void onSuccess() {
                            discoveringPeersStatus.setText("devices discovering");
                        }

                        @Override
                        public void onFailure(int i) {
                            discoveringPeersStatus.setText("discovering failed");
                        }
                    });
                }
                else{
                    requestPermissions( new String[] { Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
                }
                break;
            case R.id.btn_send_msg:
                String msg=writemsg.getText().toString();
                sendRecieve.write(msg.getBytes());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "your wifi is on", Toast.LENGTH_LONG).show();
            wifiStatus.setText("wifi is on");
        } else {
            wifiStatus.setText("wifi is off");
            AlertDialog.Builder alertDialogbuilder = new AlertDialog.Builder(this);
            alertDialogbuilder.setMessage("Please Turn on your wifi");
            alertDialogbuilder.setCancelable(true);
            AlertDialog alertDialog = alertDialogbuilder.create();
            alertDialog.show();
        }
        registerReceiver(broadcastReceiver,intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    public  class Serversideclass extends  Thread{
        Socket socket;
        ServerSocket serverSocket;
        @Override
        public void run(){
            try {
                serverSocket=new ServerSocket(8888);
                socket=serverSocket.accept();
                sendRecieve=new SendRecieve(socket);
                sendRecieve.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public  class Clientsideclass extends  Thread{
        Socket socket;
        String hostAdd;
        public  Clientsideclass(InetAddress hostAddress){
            hostAdd=hostAddress.getHostAddress();
            socket=new Socket();
            Log.d(TAG, " hostadd    : "+hostAdd);
        }
        @Override
        public void run(){
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),555);
                sendRecieve=new SendRecieve(socket);
                sendRecieve.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private  class SendRecieve extends  Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        public  SendRecieve(Socket socket){
            this.socket=socket;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;
            while(socket!=null){
                try {
                    bytes=inputStream.read(buffer);
                    if(bytes>0){
                        handler.obtainMessage(message_read,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public  void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            int[] grantResults) {
        switch (requestCode) {
            case 102:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }
                else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

}
/*
if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    // public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.



 */