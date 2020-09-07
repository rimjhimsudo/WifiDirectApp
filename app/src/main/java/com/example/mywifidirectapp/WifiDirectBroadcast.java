package com.example.mywifidirectapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
//wifi direct works on radiowaves

public class WifiDirectBroadcast extends BroadcastReceiver {
    private static final String TAG = WifiDirectBroadcast.class.getSimpleName();
    //private static final int PERMSSION = 2;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private MainActivity mainActivity;

    public WifiDirectBroadcast(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, MainActivity mainActivity) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d("TAG_State" ,"state : "+state);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "WIFI direct is on", Toast.LENGTH_LONG).show();
                mainActivity.wifiDirectstatus.setText("wifi direct is ON");
            } else {
                Toast.makeText(context, "WIFI direct is off", Toast.LENGTH_LONG).show();
                mainActivity.wifiDirectstatus.setText("wifi direct is OFF");

            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (wifiP2pManager != null) {
                if(ActivityCompat.checkSelfPermission(this.mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    wifiP2pManager.requestPeers(channel, mainActivity.peerListListener);
                    Log.d(TAG,"discovering peer and wifipeers :"+wifiP2pManager);
                }

            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            if(wifiP2pManager==null){
                return;
            }
            NetworkInfo networkInfo=intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()){
                wifiP2pManager.requestConnectionInfo(channel,mainActivity.connectionInfoListener);
            }
            else{

                mainActivity.deviceConnectionStatus.setText("device connected");
            }

        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //
        }

    }

}



 /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMSSION,);
                    //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

                    return;
                }
                else{
                    wifiP2pManager.requestPeers(channel, mainActivity.peerListListener);
                }
              /*  if (ActivityCompat.checkSelfPermission(this.mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                        }
                        */

