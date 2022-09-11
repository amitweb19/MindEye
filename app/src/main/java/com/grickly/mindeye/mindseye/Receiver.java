package com.grickly.mindeye.mindseye;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;

public class Receiver implements Runnable{
    Socket s;
    ServerSocket ss;
    InputStreamReader isr;
    BufferedReader br;
    Handler h = new Handler();
    String message;
    @Override
    public void run() {
        try{
            ss = new ServerSocket(8002);
            while (true)
            {
                s = ss.accept();
                isr = new InputStreamReader(s.getInputStream());
                br = new BufferedReader(isr);
                message = br.readLine();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.speakWords(message);
                    }
                });
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
