package com.ssd227.android.automatic;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Stack;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileListFragment extends ListFragment
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


}
