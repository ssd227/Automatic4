package com.ssd227.android.automatic;


import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class FileReceiveService  extends IntentService
{


    public static final String ACTION_RECEIVE_FILE = "RECEIVE_FILE";
    public static final String EXTRAS_FILE_CLIENT = "client";

    public FileReceiveService(String name)
    {
        super(name);
    }

    public FileReceiveService()
    {
        super("FileReceiveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Integer i = intent.getExtras().getInt(EXTRAS_FILE_CLIENT);
        Socket client = FileListFragment.clientHash.get(i);

        try{
            InputStream inputstream = client.getInputStream();


            //get file name
            String filename = new DataInputStream(inputstream).readUTF();

            //get copy num
            int copy_num = new DataInputStream(inputstream).readInt();
            FileListFragment.fileHash.put(filename, copy_num);

            //get file data
            final File f = new File(
                    Environment.getExternalStorageDirectory() + "/WIFIP2P/"
                            +"/Date/" +filename);

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            Log.d(MainActivity.TAG, "server: copying files " + f.toString());

            FileListFragment.copyFile(inputstream, new FileOutputStream(f));

            FileListFragment.clientHash.remove(i);
            client.close();
        }
        catch (IOException e) {
            Log.e(MainActivity.TAG, e.getMessage());
        }



    }
}
