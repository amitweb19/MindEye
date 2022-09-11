package com.grickly.mindeye.mindseye;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    int ctr = 0, flag = -1;
    volatile boolean cPreview = false;
    boolean piece = false;
    public static String speak = "No Previous Command Found";
    public static TextToSpeech tts;
    public static Camera camera;
    public static FrameLayout frameLayout;
    public static ShowCamera showCamera;
    String tempMove = "";
    String move = "";
    boolean openChess = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);

        camera = Camera.open();
        showCamera = new ShowCamera(this, camera);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=TextToSpeech.ERROR){
                    tts.setLanguage(Locale.getDefault());
                }
            }
        });
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action, keycode;

        action = event.getAction();
        keycode = event.getKeyCode();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(ctr == 1 && flag == 0)
                {
                    if(openChess){

                    }
                    else
                    {
                        speakWords("move is "+speak);
                        Toast.makeText(MainActivity.this, "Move is : "+speak, Toast.LENGTH_SHORT).show();
                    }
                } else if(ctr == 1 && flag == 1)
                {
                    if(openChess){
                        promptSpeechInput();
                    }
                    else
                    {
                        if(!(cPreview))
                        {
                            promptSpeechInput();
                        }else
                        {
                            CaptureImage ci = new CaptureImage();
                            ci.captureImage(camera);
                        }
                    }

                } else if(ctr == 2 && flag == 0)
                {
                    if(openChess){

                    }
                    else
                    {
                        tts.speak("back", TextToSpeech.QUEUE_FLUSH, null);
                        Toast.makeText(MainActivity.this, "Back", Toast.LENGTH_SHORT).show();
                        camera.stopPreview();
                        camera.release();
                        cPreview = false;
                        frameLayout.removeView(showCamera);
                    }
                } else if(ctr == 2 && flag == 1)
                {
                    if(openChess){

                    }
                    else
                    {

                    }
                }
                ctr = 0;
            }
        },500);

        switch (keycode)
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
            {
                if(KeyEvent.ACTION_UP==action){
                    ctr++;
                    flag = 1;
                }
            }
            break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            {
                if(KeyEvent.ACTION_DOWN==action){
                    ctr++;
                    flag = 0;
                }
            }
            break;
        }
        return super.dispatchKeyShortcutEvent(event);
    }

    public void promptSpeechInput()
    {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");

        try{
            startActivityForResult(i, 100);
        }
        catch (ActivityNotFoundException a)
        {
            Toast.makeText(MainActivity.this, "Sorry! Your Device doesn't support Speech Language!", Toast.LENGTH_LONG).show();
        }
    }
    public void onActivityResult(int request_code, int result_code, Intent i)
    {
        super.onActivityResult(request_code, result_code, i);

        switch (request_code)
        {
            case 100: if(result_code == Activity.RESULT_OK && i != null) {
                ArrayList<String> result = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Toast.makeText(MainActivity.this, result.get(0), Toast.LENGTH_SHORT).show();

                speak = result.get(0);
            }
            break;
        }
        if(((speak.indexOf("face") !=- 1) || (speak.indexOf("object") !=- 1) || (speak.indexOf("book") !=- 1) || (speak.indexOf("reader") !=- 1)))
        {
            DataSender cmdSender = new DataSender();
            if(speak.indexOf("face") !=- 1){
                cmdSender.execute("face");
            }
            else if(speak.indexOf("object") !=- 1){
                cmdSender.execute("object");
            }
            else if((speak.indexOf("book") !=- 1) || (speak.indexOf("reader") !=- 1)){
                cmdSender.execute("book");
            }
            else{
                cmdSender.execute("unknown");
                return;
            }
            camera = Camera.open();
            showCamera = new ShowCamera(this, camera);
            frameLayout.addView(showCamera);
            camera.startPreview();
            tts.speak(speak, TextToSpeech.QUEUE_FLUSH, null);
            cPreview = true;
        }
        else if(((speak.indexOf("play") !=- 1) || (speak.indexOf("chess") !=- 1) || (speak.indexOf("Chess") !=- 1)) || openChess)
        {
            DataSender cmdSender = new DataSender();
            MoveSender moveSender = new MoveSender();
            openChess = true;
            if((speak.indexOf("quit") != -1) || (speak.indexOf("exit") != -1))
            {
                openChess = false;
            }
            else
            {
                if((speak.indexOf("black") != -1))
                {
                    printWords("You choosed black colour");
                    piece = true;
                    moveSender.execute("black");
                    receiveMove();
                }
                else if(speak.indexOf("white") != -1)
                {
                    speakWords("play your first move");
                    printWords("You choosed white colour");
                    piece = true;
                    moveSender.execute("white");
                }
                else if(piece)
                {
                    boolean valid = false;
                    String x = speak.toLowerCase();
                    if(x.equals("before"))
                        x = "b4";
                    else if(x.equals("bittu"))
                        x = "b2";
                    else if(x.equals("v6"))
                        x = "b6";
                    else if(x.equals("diwan"))
                        x = "d1";
                    else if(x.equals("even"))
                        x = "e1";
                    else if(x.equals("youtube"))
                        x = "e2";
                    else if((x.equals("88")) || (x.equals("v8")))
                        x = "e8";
                    else if(x.equals("ap7"))
                        x = "f7";
                    else if(x.equals("jeevan"))
                        x = "g1";
                    else if((x.equals("jitu")) || (x.equals("j2")))
                        x = "g2";
                    printWords(x);
                    if(!(valid))
                    {
                        if(Arrays.asList("a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8").contains(x)){
                            valid = true;
                        }
                        if(Arrays.asList("b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8").contains(x)){
                            valid = true;
                        }
                        if(Arrays.asList("c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8").contains(x)){
                            valid = true;
                        }
                        if(Arrays.asList("d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8").contains(x)){
                            valid = true;
                        }
                        if(Arrays.asList("e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8").contains(x)){
                            valid = true;
                        }
                        if(Arrays.asList("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8").contains(x)){
                            valid = true;
                        }
                        if(Arrays.asList("g1", "g2", "g3", "g4", "g5", "g6", "g7", "g8").contains(x)){
                            valid = true;
                        }
                        if(Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8").contains(x)){
                            valid = true;
                        }
                        if(valid)
                        {
                            if(tempMove != "")
                            {
                                tempMove = tempMove+x;
                                speakWords("move submitted to engine and your move is "+tempMove);
                                moveSender.execute(tempMove);
                                receiveMove();
                                move = tempMove;
                                x = "";
                            }
                            tempMove = x;
                        }
                        else
                        {
                            speakWords("move rejected");
                        }
                    }
                }
                else
                {
                    speakWords("welcome in chess and please choose your piece colour");
                    printWords("Welcome in chess");
                    boolean valid = false;
                    String x = speak;
                    cmdSender.execute("playChess");
                }
            }
        } else if((speak.indexOf("bulb") !=- 1) || (speak.indexOf("bulbs") !=- 1) || (speak.indexOf("door") !=- 1) || (speak.indexOf("fan") !=- 1) || (speak.indexOf("fans") !=- 1))
        {
            String cmd0 = "";
            String cmd1 = "";
            String cmd2 = "";
            String cmd = "";

            DataSender cmdSender = new DataSender();

            if(speak.indexOf("blue") !=- 1)
                cmd0 = "blue";
            else if(speak.indexOf("red") !=- 1)
                cmd0 = "red";
            else if(speak.indexOf("yellow") !=- 1)
                cmd0 = "yellow";
            else if(speak.indexOf("green") !=- 1)
                cmd0 = "green";
            else if(speak.indexOf("all") !=- 1)
                cmd0 = "all";
            else if(speak.indexOf("fan one") !=- 1)
                cmd0 = "fan1";
            else if(speak.indexOf("fan two") !=- 1)
                cmd0 = "fan2";
            else if(speak.indexOf("door") !=- 1)
                cmd0 = "door";


            if(speak.indexOf("bulb") !=- 1)
                cmd1 = "bulb";
            else if(speak.indexOf("bulbs") !=- 1)
                cmd1 = "bulbs";
            else if(speak.indexOf("door") !=- 1)
                cmd1 = "door";
            else if(speak.indexOf("fan") !=- 1)
                cmd1 = "fan";
            else if(speak.indexOf("fans") !=- 1)
                cmd1 = "fans";

            if(speak.indexOf("open") !=- 1)
                cmd2 = "open";
            else if(speak.indexOf("fan") !=- 1)
                cmd2 = "close";
            else if(speak.indexOf("fans") !=- 1)
                cmd2 = "on";
            else if(speak.indexOf("fan") !=- 1)
                cmd2 = "off";

            cmd = cmd0+","+cmd1+","+cmd2;

            if(!(cmd.equals("")))
            {
                cmdSender.execute();
                cmd = "";
                cmd0 = "";
                cmd1 = "";
                cmd2 = "";
            }

        }
    }

    public static void speakWords(String speech) {
        tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }
    public void printWords(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }
    public void receiveMove()
    {
        Thread myThread = new Thread(new Receiver());
        myThread.start();
    }
}
