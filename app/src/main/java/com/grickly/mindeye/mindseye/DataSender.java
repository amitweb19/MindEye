package com.grickly.mindeye.mindseye;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class DataSender extends AsyncTask<String, Void, Void> {

    Socket s;
    DataOutputStream dos;
    PrintWriter pw;

    @Override
    protected Void doInBackground(String... voids) {
        String message = voids[0];
        try{
            s = new Socket("192.168.43.254",8000);
            pw = new PrintWriter(s.getOutputStream());
            pw.write(message);
            pw.flush();
            pw.close();
            s.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
