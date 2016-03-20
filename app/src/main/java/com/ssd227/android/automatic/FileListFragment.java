package com.ssd227.android.automatic;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Stack;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileListFragment extends ListFragment
        implements  ConnectionInfoListener
{
    private HashMap<String,Integer> fileHash = null;
    private final String systemHashPath =Environment.getExternalStorageDirectory()
            + "/WIFIP2P"+"/hashMap.data";

    public FileListFragment()
    {
        // Required empty public constructor
        File f = new File(systemHashPath);
        if(f.exists()){
            fileHash = (HashMap<String,Integer>)readObjectFromFile(systemHashPath);
        }
        else {
            fileHash = new HashMap<String,Integer>();
            send(1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        String[] filenames = stackToNames(scaner());
        //set list adapter
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(
                inflater.getContext(),
                android.R.layout.simple_list_item_1,
                filenames);
        setListAdapter(listAdapter);
        // Inflate the layout for this fragment
        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    public void onDestroyView() {

        writeObjecttoFile(systemHashPath, fileHash);
        super.onDestroyView();
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info){
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
            new FileServerAsyncTask(getActivity() ).execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.


            //find the directory and list out all file name
            File dirs = new File(
                    Environment.getExternalStorageDirectory() + "/WIFIP2P/"+"/Date");
            if (!dirs.exists())
                dirs.mkdirs();

            Stack<File> fileStack = new Stack<>();
            findAllFiles(dirs, fileStack);

            //for test
            for (File file : fileStack) {
                Log.d(MainActivity.TAG, file.getAbsolutePath());
            }

            if (!fileStack.empty()) {
                Log.d(MainActivity.TAG, "stack is not empty");

                File file = fileStack.pop();

                Log.d(MainActivity.TAG, "the filepath choosed is "
                        + file.getAbsolutePath());

                if (file != null) {
                    Log.d(MainActivity.TAG,
                            "file choosed is not null \nfilename is" + file.getName());

                    String filepath = file.getAbsolutePath();

                    Intent serviceIntent = new Intent(getActivity(),
                            FileTransferService.class);
                    serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                    // add some extra info
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
                            filepath);
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            info.groupOwnerAddress.getHostAddress());
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                            8988);
                    getActivity().startService(serviceIntent);

                }
            }
        }


    }

    /**
     * make all value in hashMap  10 copies
     */
    public void send(int i)
    {
        for(File file : scaner())
        {
            fileHash.put(file.getName(),i);
        }
    }


    /**
     *
     * @param files
     * @return String array with some info needed
     */
    private String[] stackToNames(Stack<File> files)
    {
        String[] filenames = new String[files.size()];
        int i=0;
        for (File fi : scaner())
        {
            String str = fi.getName();
            if(fileHash.get(str)!= null){
                str += ("    (" + fileHash.get(str))+" ";
            }
            else {
                str += "    (0 ";
            }
            str += "copies) ";
            filenames[i] = str;
            i++;
        }
        return  filenames;
    }

    /**
     *
     * @return all files found in the given directory path
     */
    private Stack<File> scaner()
    {
        File dirs = new File(
                Environment.getExternalStorageDirectory() + "/WIFIP2P"+"/Date");
        if (!dirs.exists())
            dirs.mkdirs();

        Stack<File> fileStack = new Stack<>();
        findAllFiles(dirs, fileStack);
        //for test
        for (File file : fileStack) {
            Log.d(MainActivity.TAG, file.getAbsolutePath());
        }

        return fileStack;
    }

    /**
     * find all files in the directory dies
     * @param dirs
     * @param stack keep all files found in stack and return
     */
    private void findAllFiles(File dirs, Stack<File> stack)
    {
        File files[] = dirs.listFiles();
        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory()) {
                    findAllFiles(f, stack);
                }
                else {
                    stack.push(f);
                }
            }
        }
    }

    /**
     *
     * @param filepath
     * @return
     */
    private Object readObjectFromFile(String filepath)
    {
        Object temp=null;
        File file =new File(filepath);
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            ObjectInputStream objIn=new ObjectInputStream(in);
            temp=objIn.readObject();
            objIn.close();
            //System.out.println("read object success!");
        } catch (IOException e) {
           // System.out.println("read object failed");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return temp;
    }

    /**
     *
     * @param filepath
     * @param fileHash
     */
    private void writeObjecttoFile(String filepath, Object fileHash){
        File file = new File(filepath);

        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            ObjectOutputStream objOut=new ObjectOutputStream(out);

            objOut.writeObject(fileHash);
            objOut.flush();
            objOut.close();
            //System.out.println("write object success!");

        } catch (IOException e) {
            //System.out.println("write object failed");
            e.printStackTrace();
        }

    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String>
    {

        private Context context;

        /**
         * @param context
         */
        public FileServerAsyncTask(Context context)
        {
            this.context = context;

        }

        @Override
        protected String doInBackground(Void... params)
        {
            ServerSocket serverSocket =null;
            Socket client =null;

            try
            {
                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                serverSocket = new ServerSocket(8988);
                Log.d(MainActivity.TAG, "Server: Socket opened");

                client = serverSocket.accept();
                Log.d(MainActivity.TAG, "Server: connection done");

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */

                InputStream inputstream = client.getInputStream();


                String filename = new DataInputStream(inputstream).readUTF();

                final File f = new File(
                        Environment.getExternalStorageDirectory() + "/WIFIP2P/"
                                +"/Date/" +filename);

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(MainActivity.TAG,
                        "server: copying files " + f.toString());


                copyFile(inputstream, new FileOutputStream(f));

                serverSocket.close();

                return f.getAbsolutePath();
            }
            catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
                return null;
            }
            finally {
                if(serverSocket != null){
                    try{
                        serverSocket.close();
                    }
                    catch (IOException e){
                        // Give up
                        e.printStackTrace();
                    }
                }
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        /**
         * Start activity that can handle the JPEG image
         */
        @Override
        protected void onPostExecute(String result)
        {
            if (result != null)
            {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "*/*");
                context.startActivity(intent);
            }
        }
        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute()
        {

        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out)
    {
        byte buf[] = new byte[1024];
        int len;
        try
        {
            while ((len = inputStream.read(buf)) != -1)
            {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        }
        catch (IOException e)
        {
            Log.d(MainActivity.TAG, e.toString());
            return false;
        }
        return true;
    }


}
