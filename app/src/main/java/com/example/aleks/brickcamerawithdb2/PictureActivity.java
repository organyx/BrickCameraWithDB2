package com.example.aleks.brickcamerawithdb2;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PictureActivity extends AppCompatActivity {

    ImageView ivPicture;
    TextView tvComment;
    EditText etComment;

    DatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        myDB = new DatabaseHelper(this);

        ivPicture = (ImageView) findViewById(R.id.ivSelectedPicture);
        tvComment = (TextView) findViewById(R.id.tvComments);
        etComment = (EditText) findViewById(R.id.etPictureComments);

        Intent intent = getIntent();
        String  picturePath = intent.getStringExtra("pictureDir");
        String pictureName = picturePath.substring(picturePath.lastIndexOf("/") + 1);

        Log.d("onCreate", "picture name: " + pictureName);

        ContentValues values = myDB.getOrientation(pictureName);

//        values.size();
        Toast.makeText(this, "Returned: " + values.getAsString("Orientation"), Toast.LENGTH_LONG).show();
//        tvComment.setText(values.size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picture, menu);
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
}
