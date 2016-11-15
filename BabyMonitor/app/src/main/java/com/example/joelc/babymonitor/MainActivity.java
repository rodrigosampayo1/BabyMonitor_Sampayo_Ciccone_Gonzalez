package com.example.joelc.babymonitor;


import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private EditText entrada;
    private TextView salidaTemperatura;
    private TextView salidaHumedad;
    private SensorManager mSensorManager;
    private long last_update = 0, last_movement = 0;
    private float prevX = 0;
    private float curX = 0;
    float estado=0;
    String ip="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        entrada = (EditText) findViewById(R.id.EditText01);
        salidaTemperatura = (TextView) findViewById(R.id.TextViewTemperatura);
        salidaHumedad = (TextView) findViewById(R.id.textViewHumedad);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.
                Builder().permitNetwork().build());
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    public void buscar(View view) {



        salidaTemperatura.setText("");
        salidaHumedad.setText("");
        ip = entrada.getText().toString();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                new HttpAsyncTask().execute("http://" + ip + "/");
            }

        };


        new Timer().scheduleAtFixedRate(task, 0, 2800);



    }


    @Override
    public void onStart() {
        super.onStart();



    }

    @Override
    public void onStop() {
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);
        super.onStop();

    }
    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);


    }

    @Override
    public void onSensorChanged(SensorEvent event){
        int bandera = 0;
        synchronized (this) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    long current_time = event.timestamp;
                    curX = event.values[0];
                    if (prevX == 0) {
                        last_update = current_time;
                        last_movement = current_time;
                        prevX = curX;
                    }
                    long time_difference = current_time - last_update;
                    if (time_difference > 0) {
                        float movement = Math.abs((curX) - (prevX)) / time_difference;
                        int limit = 1500;
                        float min_movement = 1E-6f;
                        if (movement > min_movement) {
                            if (current_time - last_movement >= limit) {
                                Toast.makeText(getApplicationContext(), "Chau, chau", Toast.LENGTH_SHORT).show();
                                bandera = 1;
                            }
                            last_movement = current_time;
                        }
                        prevX = curX;

                        last_update = current_time;
                    }
                    if (bandera == 1) {
                        finish();
                    }
                    break;

                case Sensor.TYPE_PROXIMITY:

                    // Si detecta 0 lo represento
                    try {if(event.values[0] == estado) {
                        estado = event.values[0];
                        RelativeLayout rl = (RelativeLayout)findViewById(R.id.fondo);
                        rl.setBackgroundColor(Color.DKGRAY);
                        URL url = new URL("http://"+ip+"/APAGAR/");
                        HttpURLConnection conexion = (HttpURLConnection)
                                url.openConnection();
                        conexion.setRequestProperty("User-Agent",
                                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");

                        if (conexion.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        }
                        else {
                            salidaTemperatura.append("ERROR: "
                                    + conexion.getResponseMessage() + "\n");
                            salidaHumedad.append("ERROR: "
                                    + conexion.getResponseMessage() + "\n");
                        }
                        conexion.disconnect();

                    }else{estado=0;
                    }
                    } catch (java.io.IOException e) {
                        Toast.makeText(getApplicationContext(), "ERROR EN LA ALARMA", Toast.LENGTH_SHORT).show();
                    }


                    break;
            }
        }
    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String pagina) {
            String palabra1="TEMPERATURA: ";
            String palabra2="HUMEDAD: ";
            String palabra3="Lectura: ";
            String devuelve="";
            int ini = pagina.indexOf(palabra1);
            if (ini != -1) {
                int fin = pagina.indexOf(" ", ini + palabra1.length()+1);
                devuelve = pagina.substring(ini + palabra1.length() , fin);
                salidaTemperatura.setText(palabra1 +" "+ devuelve);
                Float f= Float.parseFloat(devuelve);
                if(f>=25.00){
                    salidaTemperatura.setTextColor(Color.RED);
                    salidaHumedad.setTextColor(Color.RED);
                }
                if(f>= 14.00&&f<25.00){
                    salidaTemperatura.setTextColor(Color.GREEN);
                    salidaHumedad.setTextColor(Color.GREEN);
                }
                if(f<14.00){
                    salidaTemperatura.setTextColor(Color.BLUE);
                    salidaHumedad.setTextColor(Color.BLUE);
                }
            } else {
                devuelve = "no encontrado";
            }
            //BUSCAR PALABRA HUMEDAD
            ini = pagina.indexOf(palabra2);
            if (ini != -1) {
                int fin = pagina.indexOf(" ", ini + palabra2.length()+1);
                devuelve = pagina.substring(ini + palabra2.length(), fin-1);
                salidaHumedad.setText(palabra2 +" "+ devuelve);
            } else {
                devuelve = "no encontrado";
            }
            ini = pagina.indexOf(palabra3);
            if (ini != -1) {
                int fin = pagina.indexOf("<", ini + palabra2.length()+1);
                devuelve = pagina.substring(ini + palabra2.length(), fin);
                devuelve=devuelve.toUpperCase();
                if(!devuelve.contentEquals("SIN MOVIMIENTO")){
                    RelativeLayout rl = (RelativeLayout)findViewById(R.id.fondo);
                    rl.setBackgroundColor(Color.WHITE);
                }
            } else {
                devuelve = "no encontrado";
            }
        }
    }
    public String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
            Toast.makeText(getApplicationContext(), "ERROR:"+e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return result;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}