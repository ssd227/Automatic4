package com.ssd227.android.automatic;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity
    implements PeerListListener
{
    public static final String TAG = "Automatic" ;

    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private boolean isconnected = false;

    private final IntentFilter intentFilter = new IntentFilter();

    private WifiP2pManager manager;
    private Channel channel;
    private BroadcastReceiver receiver = null;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pDevice device;
    private String oldDeviceName = null;


    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled)
    {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume()
    {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList)
    {

        // Out with the old, in with the new.
        peers.clear();
        peers.addAll(peerList.getDeviceList());

        if (peers.size() == 0)
        {
            Log.d(TAG, "No devices found");
            return;
        }

        connect();

    }

    public void connect(){
        // Picking the first device found on the network.
        device = peers.get(0);

        if(oldDeviceName != null){
            if(oldDeviceName.equals(device.deviceName)){
                return;
            }
        }
        //do something to avoid conflict
        try {
            Thread.currentThread().sleep((int)(Math.random()*3000));//阻断2秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;

        manager.connect(channel, config, new ActionListener() {
            @Override
            public void onSuccess() {
                oldDeviceName = device.deviceName;
                Log.d(MainActivity.TAG, "connect successfully");
                updatePeerDeviceName(device);
                isconnected = true;
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.atn_direct_send:
                final FileListFragment fragment = (FileListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_fileList);
                fragment.send(10);

                return true;

            case R.id.atn_direct_discover:
                //wifi is closed
                if (!isWifiP2pEnabled) {
                    Toast.makeText(MainActivity.this,
                            R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

                //wifi is open,
                //then can initial the discover

                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this,
                                "Discovery Initiated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this,
                                "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;

            /*
            case R.id.atn_direct_disconnect:
                if(isconnected){

                    manager.cancelConnect(channel,new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            isconnected = false;
                            Toast.makeText(MainActivity.this,
                                    "disconnect successful", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(MainActivity.this,
                                    "disconnect Failed : " + reasonCode,
                                    Toast.LENGTH_SHORT).show();
                        }

                    });
                }
                */


            case R.id.atn_direct_enable:
                if (manager != null && channel != null)
                {
                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
                else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void updatePeerDeviceName(WifiP2pDevice device){
        String DeviceName = device.deviceName;
        TextView peer = (TextView)findViewById(R.id.peer_device);
        peer.setText(DeviceName);
    }

    public void updateMyDeviceName(WifiP2pDevice device){
        String DeviceName = device.deviceName;
        TextView me = (TextView)findViewById(R.id.my_device);
        me.setText(DeviceName);
    }
}
