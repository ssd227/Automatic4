package com.ssd227.android.automatic;

// Copyright 2011 Google Inc. All Rights Reserved.

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;




/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService
{

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name)
    {
        super(name);
    }

    public FileTransferService()
    {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent)
    {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE))
        {
            String filepath = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);

            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try
            {
                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                socket.getReuseAddress();
                Log.d(MainActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)),
                        SOCKET_TIMEOUT);

                /**
                 *
                 */
                Log.d(MainActivity.TAG,
                        "Client socket - " + socket.isConnected());

                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = null;

                File file = new File(filepath);
                String filename = file.getName();


                new DataOutputStream(outputStream).writeUTF(filename);

                //send file name
                // outputStream.write(filename.getBytes());
                Log.d(MainActivity.TAG, "send file name :" + filename);
                outputStream.flush();

                //send file data
                FileInputStream fileInputStream = new FileInputStream(file);



                FileListFragment.copyFile(fileInputStream, outputStream);
                outputStream.flush();
                Log.d(MainActivity.TAG, "Client: Data written");

            }
            catch (IOException e)
            {
                Log.e(MainActivity.TAG, e.getMessage());
            }
            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            finally
            {
                if (socket !=  null)
                {
                    if (socket.isConnected())
                    {
                        try
                        {
                            socket.close();
                        } catch (IOException e)
                        {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
