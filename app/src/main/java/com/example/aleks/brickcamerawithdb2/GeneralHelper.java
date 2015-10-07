package com.example.aleks.brickcamerawithdb2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleks on 03-Oct-15.
 * Methods to use across the app
 */
public class GeneralHelper {

    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))
            return true;
        return false;
    }

    public boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
            return true;
        return false;
    }

    public boolean setPictureToSize(String filename, ImageView iv)
    {
        if(filename.equals("default"))
        {

            return false;
        }
        else
        {
            Log.d("setPictureToSize", "File: " + filename);

            ArrayList<String> exif = getExifInfo(filename);

//        Log.d("PICTURE", "FilePath: " + filename);
            String result = filename.substring(filename.lastIndexOf("/") + 1);
//        Log.d("PICTURE", "Filename: " + result);

//        myDB.addPicture(result, filename, orientation);

            adjustPicOrientation(exif.get(0), iv);

            Bitmap bitmap = resizePicture(filename, 200, 150);

            iv.setImageBitmap(bitmap);
            return true;
        }

    }

    public ArrayList<String> getExifInfo(String filename) {
        String orientation = null;
        String latValue;
        String latRef;
        String longValue;
        String longRef;

        ArrayList<String> exifInfo = new ArrayList<String>();

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(exif.getAttribute(ExifInterface.TAG_ORIENTATION) != null)
        {
            orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            latValue = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            longValue = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            longRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            exifInfo.add(orientation);
            exifInfo.add(latValue);
            exifInfo.add(latRef);
            exifInfo.add(longValue);
            exifInfo.add(longRef);
        }
        return exifInfo;
    }

    public Bitmap resizePicture(String filename, int width, int height) {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / width, photoH / height);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(filename, bmOptions);
    }

    public void adjustPicOrientation(String orientation, ImageView iv) {
        switch (orientation)
        {
            case "1":
                iv.setRotation(360);
                break;
            case "3":
                iv.setRotation(180);
                break;
            case "6":
                iv.setRotation(90);
                break;
            case "8":
                iv.setRotation(270);
                break;
        }
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".csv")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    public String filenameFromPath(String path)
    {
        return  path.substring(path.lastIndexOf("/") + 1);
    }
}
