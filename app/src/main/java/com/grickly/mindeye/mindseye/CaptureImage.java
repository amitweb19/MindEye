package com.grickly.mindeye.mindseye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CaptureImage {
    Camera camera;
    boolean cPreview = false;
    FrameLayout frameLayout;
    ImageView imageView;

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            File picture_file = getOutputMediaFile();
            if(picture_file == null)
            {
                return;
            }
            else
            {
                try {
                    FileOutputStream fos = new FileOutputStream(picture_file);
                    fos.write(data);
                    fos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
                Bitmap image = BitmapFactory.decodeFile(picture_file.getAbsolutePath());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bytes = bos.toByteArray();
                ImageSender sic = new ImageSender();
                sic.execute(bytes);
            }
        }
    };

    private File getOutputMediaFile() {
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED))
        {
            return null;
        }
        else
        {
            File folder_gui = new File(Environment.getExternalStorageDirectory() + File.separator + "GUI");
            if(!folder_gui.exists())
            {
                folder_gui.mkdirs();
            }
            File outputFile = new File(folder_gui,"temp.jpg");
            return outputFile;
        }
    }

    public void captureImage(Camera camera)
    {
        this.camera = camera;
        if(camera != null)
        {
            camera.takePicture(null,null, mPictureCallback);
        }else
        {
            MainActivity.speakWords("Camera is null");
        }
    }
}
