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

    private GoogleMap gmap;
    private Button btnDoc;

    private static final int MENU = 1;
    private static final int GROUP = 1;

    MapFragment mapFragment;

    GeneralHelper utilities;
    DatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivLastPic = (ImageView) findViewById(R.id.ivLastPicture);
        tvOrientation = (TextView) findViewById(R.id.tvOrientationValue);
        tvLat = (TextView) findViewById(R.id.tvLatValue);
        tvLong = (TextView) findViewById(R.id.tvLongValue);

        myDB = new DatabaseHelper(this);
        utilities = new GeneralHelper();

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);

        mapFragment.getMapAsync(this);

//        if(utilities.isExternalStorageReadable() && utilities.isExternalStorageWritable())
//            Toast.makeText(this, "Can do stuff", Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(this, "Can't do stuff", Toast.LENGTH_LONG).show();

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

                utilities.setPictureToSize(filename, ivLastPic);

                showExifInfo(filename);

                String resultName = filename.substring(filename.lastIndexOf("/") + 1);

                ArrayList<String> exif = utilities.getExifInfo(filename);
                myDB.addPicture(resultName, filename, exif.get(0));

//                mapFragment.getMap().clear();
//                onMapReady(mapFragment.getMap());
                findImagesWithGeoTagAndAddToGmap(mapFragment.getMap());
            }
            if(resultCode == RESULT_CANCELED)
            {
                Log.d("onActivityResult", "RESULT_CANCELED");
//                if (data == null)
//                    setPictureToSize(picturePrevPath, ivLastPic);
            }
        }
    }

    private void showExifInfo(String filename)
    {
        ArrayList<String> exif = utilities.getExifInfo(filename);
        tvOrientation.setText(exif.get(0));
        tvLong.setText(exif.get(1) + " " + exif.get(2));
        tvLat.setText(exif.get(3) + " " + exif.get(4));
    }

    private String loadLastAttemptedImageCaptureFilename() {
        SharedPreferences prefs = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);
        String saved_path = prefs.getString(SAVED_PICTURE_PATH, pictureDirectory + File.separator + "IMG_20151003_162805.jpg");
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

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if(status == ConnectionResult.SUCCESS)
//        {
//            Toast.makeText(this, "Google Play is available", Toast.LENGTH_LONG).show();
//        }
//        else
//        {
//            Toast.makeText(this, "Google Play is not available", Toast.LENGTH_LONG).show();
//        }

        String filename = loadLastAttemptedImageCaptureFilename();

        utilities.setPictureToSize(filename, ivLastPic);
        showExifInfo(filename);
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

        Intent pictureIntent = new Intent(MainActivity.this, PictureActivity.class);
        pictureIntent.putExtra("pictureDir", pictureDir.getPath());
        startActivity(pictureIntent);
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
                googleMap.clear();
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
//                            googleMap.clear();
                            Log.d("GMAP", "Image with gtag: " + file.getName());
                            addGeoTag(pos, file.getName(), googleMap);
                        }
                    }
                }
            }
        }
    }
}
