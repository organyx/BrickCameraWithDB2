package com.example.aleks.brickcamerawithdb2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class CurrencyConverterActivity extends AppCompatActivity {

    public static final String CONVERT_CURRENCY = "CONVERT_CURRENCY";
    public static final String CONVERTED_CURRENCY = "CONVERTED_CURRENCY";

    TextView tvResult;
    EditText etValue;
    EditText etFrom;
    EditText etTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        tvResult = (TextView) findViewById(R.id.tvServiceResult);
        etValue = (EditText) findViewById(R.id.etServiceAmount);
        etFrom = (EditText) findViewById(R.id.etServiceFrom);
        etTo = (EditText) findViewById(R.id.etServiceTo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_currency_converter, menu);
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

    public void onBtnConvertClick(View view) {
        Intent convertCurrencyIntent = new Intent(CurrencyConverterActivity.this, CurrencyConverterService.class);
        convertCurrencyIntent.putExtra("FROM", etFrom.getText().toString());
        convertCurrencyIntent.putExtra("TO", etTo.getText().toString());
        CurrencyConverterActivity.this.startService(convertCurrencyIntent);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null)
            {
                String result = bundle.getString(CONVERTED_CURRENCY);
                Log.d("myService", "Result: " + result);
                if(result != null && result.compareTo("") != 0)
                {
                    int original = Integer.parseInt(etValue.getText().toString());
                    double rate = Double.parseDouble(result);
                    double finalResult = original * rate;
                    tvResult.setText(finalResult + "");
                }
            }
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(CONVERT_CURRENCY));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
