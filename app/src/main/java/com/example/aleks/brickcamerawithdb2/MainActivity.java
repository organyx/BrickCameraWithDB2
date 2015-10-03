package com.example.aleks.brickcamerawithdb2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, View.OnClickListener{

    private ImageView ivLastPic;
    private TextView tvOrientation;
    private TextView tvLong;
    private TextView tvLat;

    private static final int PICTURE_REQUEST_CODE = 123;
    public static final String SAVED_PREFERENCES = "SAVED_PREFERENCES";
    public static final String SAVED_PICTURE_PATH = "SAVED_PICTURE_PATH";

    private File pictureDirectory;

    private LinearLayout pop_up;
    private ImageView pop_up_image;

    private GoogleMap gmap;
    private Button btnDoc;

    private static final int MENU = 1;
    private static final int GROUP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivLastPic = (ImageView) findViewById(R.id.ivLastPicture);
        tvOrientation = (TextView) findViewById(R.id.tvOrientationValue);
        tvLat = (TextView) findViewById(R.id.tvLatValue);
        tvLong = (TextView) findViewById(R.id.tvLongValue);

        pop_up = (LinearLayout) findViewById(R.id.pop_up_layout);
        pop_up_image = (ImageView) findViewById(R.id.pop_up_image);
        if(pop_up.getVisibility() == View.VISIBLE)
        {
            pop_up.setVisibility(View.INVISIBLE);
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);

        mapFragment.getMapAsync(this);

        if(isExternalStorageReadable() && isExternalStorageWritable())
            Toast.makeText(this, "Can do stuff", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Can't do stuff", Toast.LENGTH_LONG).show();

        pictureDirectory = getMyPicDirectory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

//        List<File> files = getListFiles(pictureDirectory);
//
//        for (int i = 0; i < files.size(); i++)
//        {
//            menu.add(GROUP, MENU, i, i);
//            Log.d("OptionsMenu", "Item: " + i);
//        }

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICTURE_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                Log.d("onActivityResult", "RESULT_OK");
                String filename = loadLastAttemptedImageCaptureFilename();

                setPictureToSize(filename, ivLastPic);
            }
            if(resultCode == RESULT_CANCELED)
            {
                Log.d("onActivityResult", "RESULT_CANCELED");
//                if (data == null)
//                    setPictureToSize(picturePrevPath, ivLastPic);
            }
        }
    }

    private String loadLastAttemptedImageCaptureFilename() {
        SharedPreferences prefs = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);
        String saved_path = prefs.getString(SAVED_PICTURE_PATH, "DEFAULT PATH");
        Log.d("FILE_PATH", "Loaded value: " + saved_path);
        return saved_path;
    }

    private void saveLastAttemptedImageCaptureFilename(String filename) {
        SharedPreferences prefs = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SAVED_PICTURE_PATH, filename);
        Log.d("FILE_PATH", "Saved value: " + filename);
        editor.apply();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(pop_up.getVisibility() == View.VISIBLE)
        {
            pop_up.setVisibility(View.INVISIBLE);
        }

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(status == ConnectionResult.SUCCESS)
        {
            Toast.makeText(this, "Google Play is available", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Google Play is not available", Toast.LENGTH_LONG).show();
        }

        String filename = loadLastAttemptedImageCaptureFilename();

        setPictureToSize(filename, ivLastPic);
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        pop_up.setVisibility(View.VISIBLE);
    }

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

    private void setPictureToSize(String filename, ImageView iv)
    {
        Log.d("setPictureToSize", "File: " + filename);

        String orientation = getExifInfo(filename);

        adjustPicOrientation(orientation, iv);

        Bitmap bitmap = resizePicture(filename, 200, 150);

        iv.setImageBitmap(bitmap);
    }

    private String getExifInfo(String filename) {
        String orientation = null;
        String latValue;
        String latRef;
        String longValue;
        String longRef;

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
            tvOrientation.setText(orientation);
            tvLat.setText(latValue + " " + latRef);
            tvLong.setText(longValue + " " + longRef);
        }
        return orientation;
    }

    private void setPictureToPopUpSize(String filename, ImageView iv) {
        String orientation = getExifInfo(filename);

        adjustPicOrientation(orientation, iv);

        Bitmap bitmap = resizePicture(filename, 200, 150);
        iv.setImageBitmap(bitmap);
    }

    private Bitmap resizePicture(String filename, int width, int height) {

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

    private void adjustPicOrientation(String orientation, ImageView iv) {
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

    private File getMyPicDirectory()
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "BrickCamera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (checkMyPicDirectory(mediaStorageDir))
            return mediaStorageDir;
        else
            return null;
    }

    private boolean checkMyPicDirectory(File filename)
    {
        if (! filename.exists()){
            if (! filename.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return false;
            }
        }
        return true;
    }

    public void onBtnTakePicClick(View view) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String filename = pictureDirectory.getPath() + File.separator+"IMG_"+timeStamp+".jpg";
        File imageFile = new File(filename);
        Uri imageUri = Uri.fromFile(imageFile);

        saveLastAttemptedImageCaptureFilename(filename);

        takePicture(imageUri);
    }

    public void takePicture(Uri filepath)
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, filepath);
        if(cameraIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(cameraIntent, PICTURE_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(this);

        findImagesWithGeoTagAndAddToGmap(googleMap);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        pictureDir = new File(pictureDir, "BrickCamera" + File.separator + marker.getTitle());

//        if(pop_up.getVisibility() == View.INVISIBLE)
//        {
//            TextView title = (TextView) pop_up.getChildAt(0);
//            ImageView image = (ImageView) pop_up.getChildAt(1);
//
//            title.setText(marker.getTitle());
//            setPictureToPopUpSize(pictureDir.getPath(), image);
//
//            pop_up.setVisibility(View.VISIBLE);
//        }
//        else
//        {
//            pop_up.setVisibility(View.INVISIBLE);
//        }

//        Intent pictureIntent = new Intent(PicActivity.this, PictureActivity.class);
//        pictureIntent.putExtra("pictureDir", pictureDir.toString());
//        startActivity(pictureIntent);
        return true;
    }

    private void addGeoTag(LatLng pos, String name, GoogleMap gmap) {
        gmap.setMyLocationEnabled(true);
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
        gmap.addMarker(new MarkerOptions().position(pos)).setTitle(name);
    }

    private LatLng getLatLongFromExif(String absolutePath) {
        float latLong[] = new float[2];
        LatLng pos = null;
        try{
            ExifInterface exif = new ExifInterface(absolutePath);
            if(exif.getLatLong(latLong))
            {
                pos = new LatLng(latLong[0], latLong[1]);
            }
            else
            {
                return null;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return pos;
    }

    private void findImagesWithGeoTagAndAddToGmap(GoogleMap googleMap) {
        Log.d("GMAP", "findImagesWithGeoTagAndAddToGmap");
        String storageState = Environment.getExternalStorageState();
        if(storageState.equals(Environment.MEDIA_MOUNTED))
        {
            Log.d("GMAP", "Media Mounted");
            File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            pictureDir = new File(pictureDir, "BrickCamera");

            if(pictureDir.exists())
            {
                Log.d("GMAP", "Folder Found");
                File[] files = pictureDir.listFiles();
                for(File file : files)
                {
                    if(file.getName().endsWith(".jpg"))
                    {
                        Log.d("GMAP", "Found .jpgs");
                        LatLng pos = getLatLongFromExif(file.getAbsolutePath());

                        if(pos != null)
                        {
                            Log.d("GMAP", "Image with gtag: " + file.getName());
                            addGeoTag(pos, file.getName(), googleMap);
                        }
                    }
                }
            }
        }
    }

    public void onPopUpClick(View view) {
        if(pop_up.getVisibility() == View.VISIBLE)
        {
            pop_up.setVisibility(View.INVISIBLE);
        }
    }
}
