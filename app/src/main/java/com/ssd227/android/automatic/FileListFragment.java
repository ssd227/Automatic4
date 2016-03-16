package com.ssd227.android.automatic;


import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.Stack;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileListFragment extends ListFragment {

    public FileListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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


    private String[] stackToNames(Stack<File> files){
        String[] filenames = new String[files.size()];
        int i=0;
        for (File fi : scaner()){
            filenames[i] = fi.getName();
            i++;
        }
        return  filenames;
    }

    private Stack<File> scaner(){
        File dirs = new File(
                Environment.getExternalStorageDirectory() + "/WIFIP2P");
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

    private void findAllFiles(File dirs, Stack<File> stack) {
        File files[] = dirs.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    findAllFiles(f, stack);
                } else {
                    stack.push(f);
                }
            }
        }
    }


}
