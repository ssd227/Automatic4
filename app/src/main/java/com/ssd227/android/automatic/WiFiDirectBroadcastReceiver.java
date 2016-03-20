package com.ssd227.android.automatic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;

    /**
     * @param manager
     *            WifiP2pManager system service
     * @param channel
     *            Wifi p2p channel
     * @param activity
     *            activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(
            WifiP2pManager manager, Channel channel, MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
            } else
            {
                activity.setIsWifiP2pEnabled(false);
            }
            Log.d(MainActivity.TAG, "P2P state changed - " + state);

        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null)
            {
                manager.requestPeers(channel, activity);
                Log.d(MainActivity.TAG, "request Peers");
            }



        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            Log.d(MainActivity.TAG, "P2P connection changed");
            // Respond to new connection or disconnections
            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                FileListFragment fragment = (FileListFragment) activity
                        .getFragmentManager().findFragmentById(R.id.frag_fileList);
                manager.requestConnectionInfo(channel, fragment);
            }
            else {
                // It's a disconnect
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
            // Respond to this device's wifi state changing
        }
    }
}