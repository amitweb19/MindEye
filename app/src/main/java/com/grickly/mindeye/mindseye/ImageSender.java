package com.grickly.mindeye.mindseye;

import android.os.AsyncTask;
import android.os.Handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ImageSender extends AsyncTask<byte[], Void, Void> {

    Socket s;
    DataOutputStream dos;
    Handler h = new Handler();

    @Override
    protected void onPostExecute(Void aVoid) {
        Thread myThread = new Thread(new Receiver());
        myThread.start();
    }

    @Override
    protected Void doInBackground(final byte[]... bytes) {

        try {
            s = new Socket("192.168.43.254",8001);

            OutputStream out = s.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            dos.write(bytes[0], 0, bytes[0].length);
            h.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.speakWords("Picture sent to server");
                }
            });
            dos.close();
            out.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
            h.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.speakWords("I/O Exception occured");
                }
            });
        }
        return null;
    }
}
