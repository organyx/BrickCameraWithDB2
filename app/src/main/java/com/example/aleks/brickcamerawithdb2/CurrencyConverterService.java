package com.example.aleks.brickcamerawithdb2;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.kobjects.util.Strings;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class CurrencyConverterService extends Service {
    public Intent i;
    public CurrencyConverterService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        i = intent;
        new ConvertCurrency().execute();
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ConvertCurrency extends AsyncTask<Integer, Void, String>
    {
//        String originalValue;
        String currencyFrom;
        String currencyTo;

        @Override
        protected void onPreExecute()
        {

            currencyFrom = i.getExtras().getString("FROM");

            currencyTo = i.getExtras().getString("TO");

//            originalValue = getApplicationContext().getString("FROM");
        }

        @Override
        protected String doInBackground(Integer... params)
        {
            String NAME_SPACE = "http://www.webserviceX.NET/";
            String URL = "http://www.webservicex.net/currencyconvertor.asmx";
            String SOAP_ACTION = "http://www.webserviceX.NET/ConversionRate";
            String METHOD_NAME = "ConversionRate";

            String webResult = null;

            SoapObject request = new SoapObject(NAME_SPACE, METHOD_NAME);
            Log.d("myService", "FROM: " + currencyFrom);
            Log.d("myService", "TO: " + currencyTo);
            request.addProperty("FromCurrency", currencyFrom);
            request.addProperty("ToCurrency", currencyTo);

            SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelop.dotNet = true;
            envelop.setOutputSoapObject(request);

            HttpTransportSE androidHttpRequest = new HttpTransportSE(URL);
            androidHttpRequest.debug = true;
            try {

                for (int i = 0; i < 3; i++) {
                    androidHttpRequest.call(SOAP_ACTION, envelop);
                    webResult = envelop.getResponse().toString();
                    Log.d("myService", "webResult: " + webResult);
                    if (webResult != null)
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("APP", "ERROR: " + e.getMessage());
            }

            return webResult;
        }

        @Override
        protected void onPostExecute(String result)
        {
            showResult(result);
        }
    }

    private void showResult(String result)
    {
        Intent intent = new Intent(CurrencyConverterActivity.CONVERT_CURRENCY);
        intent.putExtra(CurrencyConverterActivity.CONVERTED_CURRENCY, result);
        sendBroadcast(intent);
    }
}
