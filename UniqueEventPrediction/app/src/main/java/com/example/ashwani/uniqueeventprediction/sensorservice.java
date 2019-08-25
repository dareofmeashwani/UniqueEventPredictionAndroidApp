package com.example.ashwani.uniqueeventprediction;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import android.os.Vibrator;
import com.android.internal.http.multipart.MultipartEntity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class sensorservice extends Service {

    String user="",filename="",ip="",portid="",soundactivity="",sensoractivity="";
    int mode=-1,samplingrate;
    boolean stopper=true;
    String storagepath= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Prediction";
    public sensorservice() {
    }
    @Override
    public void onCreate()
    {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        user = intent.getStringExtra("user");
        portid = intent.getStringExtra("port");
        ip = intent.getStringExtra("ip");
        filename = intent.getStringExtra("filename");
        sensoractivity = intent.getStringExtra("sensor");
        soundactivity = intent.getStringExtra("sound");
        mode = Integer.parseInt(intent.getStringExtra("mode"));
        try{ samplingrate = Integer.parseInt(intent.getStringExtra("sampler").toString());}
        catch(Exception e){samplingrate=50;}
        sensorservicethread t = new sensorservicethread();
        t.start();
        return START_STICKY;
    }
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        stopper=false;
    }
    String datename()
    {
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmmss");
        dateFormatter.setLenient(false);
        Date today = new Date();
        String date = dateFormatter.format(today);
        return date;
    }
    String notid()
    {
        DateFormat dateFormatter = new SimpleDateFormat("hhmmss");
        dateFormatter.setLenient(false);
        Date today = new Date();
        String date = dateFormatter.format(today);
        return date;
    }
    public void showNotification(String message)
    {
        int mNotificationId = Integer.parseInt(notid().toString())+1;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.s)
                        .setContentTitle(notid())
                        .setContentText(message);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    class sensorservicethread extends Thread implements SensorEventListener
    {
        SensorManager msensormanager;
        Sensor mlight = null, mgyro = null, macc = null, mmag = null;
        sensordata sensor_data = new sensordata();
        boolean a = false, g = false, m = false, p = false;
        float proxythreshold = -1;
        float d = 0, b = 0, c = 0;
        String file ="";
        long oldtime=0;
        public sensorservicethread()
        {
            activatesensor();
        }
        public void run()
        {
            oldtime= System.currentTimeMillis();
            if(mode==0) filename=storagepath + File.separator + "Text Test samples" + File.separator + "testdata"+":"+datename()+ ".csv";
            if (mode==1) filename = storagepath + File.separator + "Text Training Samples" + File.separator + user + ".csv";
            String sep=",";
            while(stopper)
            {
                try
                {// Toast.makeText(mainclass.getApplicationContext(), ""+sensor_data.acc_x+" "+sensor_data.acc_y+" "+sensor_data.acc_z, Toast.LENGTH_LONG).show();
                    if ((a == true && macc != null || a == false && macc == null) && (g == true && mgyro != null || g == false && mgyro == null) && (m == true && mmag != null || m == false && mmag == null))
                    {
                        if ((sensor_data.proximity < proxythreshold && mlight != null || mlight == null))
                        {
                            if (mode == 0)//////test layout
                            {
                                String temp = sensor_data.acc_x + sep + sensor_data.acc_y + sep + sensor_data.acc_z + sep + sensor_data.gyro_x + sep + sensor_data.gyro_y + sep + sensor_data.gyro_z+sep + sensor_data.mag_x + sep + sensor_data.mag_y + sep + sensor_data.mag_z;
                                writedata(filename, temp,0);
                            }
                            else if (mode == 1)/////training layout
                            {
                                String temp = sensor_data.acc_x + sep + sensor_data.acc_y + sep + sensor_data.acc_z+sep +sensor_data.gyro_x + sep + sensor_data.gyro_y + sep + sensor_data.gyro_z + sep + sensor_data.mag_x + sep + sensor_data.mag_y + sep + sensor_data.mag_z + sep +sensoractivity;
                                writedata(filename, temp,1);
                            }
                            a = g = m = false;

                        }
                    }
                    long time=System.currentTimeMillis();
                    if (time-oldtime>4000&mode==0)
                    {
                        try{
                            uploadFile(filename);
                            filename=storagepath + File.separator + "Text Test samples" + File.separator + "testdata"+":"+datename()+ ".csv";
                        }
                        catch (Exception e){}
                        oldtime=time;
                    }
                }
                catch (Exception e)
                {
                   // Toast.makeText(getApplicationContext(), "EXCEPTION:sensorthreadclass:doinbackground:sleep", Toast.LENGTH_LONG).show();
                }
            }
            deactivatesensor();
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                sensor_data.acc_x = values[0];
                sensor_data.acc_y = values[1];
                sensor_data.acc_z = values[2];
                a = true;
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                sensor_data.gyro_x = values[0];
                sensor_data.gyro_y = values[1];
                sensor_data.gyro_z = values[2];
                g = true;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                sensor_data.mag_x = values[0];
                sensor_data.mag_y = values[1];
                sensor_data.mag_z = values[2];
                m = true;
            }
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                sensor_data.proximity = values[0];
                c = b;
                b = d;
                d = values[0];
                Float max, min;
                if (d > b && d > c)
                    max = d;
                else if (b > d && b > c)
                    max = b;
                else
                    max = c;
                if (d < b && d < c)
                    min = d;
                else if (b < d && b < c)
                    min = b;
                else
                    min = c;
                proxythreshold = (max + min) / 2;
            }

            //Toast.makeText(mainclass.getApplicationContext(), ""+sensor_data.acc_x+" "+sensor_data.acc_y+" "+sensor_data.acc_z, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        void activatesensor() {
            msensormanager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            if (msensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                macc = msensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
            if (msensormanager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                mgyro = msensormanager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            }
            if (msensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                mmag = msensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            }
            if (msensormanager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
                mlight = msensormanager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            }

            if (mlight != null) {
                msensormanager.registerListener(this, mlight,samplingrate);
            } else {
                Toast.makeText(getApplicationContext(), "proximity sensor NOT found", Toast.LENGTH_LONG).show();
            }
            if (macc != null) {
                msensormanager.registerListener(this, macc,samplingrate);
            } else {
                Toast.makeText(getApplicationContext(), "acceleromter sensor NOT found", Toast.LENGTH_LONG).show();
            }
            if (mgyro != null) {
                msensormanager.registerListener(this, mgyro,samplingrate);
            } else {
                //Toast.makeText(getApplicationContext(), "gyrometer sensor NOT found", Toast.LENGTH_LONG).show();
            }
            if (mmag != null) {
                msensormanager.registerListener(this, mmag,samplingrate);
            } else {
                Toast.makeText(getApplicationContext(), "magnetometer sensor NOT found", Toast.LENGTH_LONG).show();
            }
        }

        void deactivatesensor() {
            macc = null;
            mgyro = null;
            mmag = null;
            mlight = null;
            msensormanager.unregisterListener(this);
        }
    }



    void writedata(String filename,String str,int i)
    {
        String label="";
        File file = new File(filename);
        FileWriter fw;
        BufferedWriter writer;
        try{
            if(!file.exists())
            {
            fw = new FileWriter(file,true);
            writer = new BufferedWriter(fw);

                if(i==0) label=label+"Acc_x,Acc_y,Acc_z,Gyro_x,Gyro_y,Gyro_z,Mag_x,Mag_y,Mag_z";
                else label=label+"Acc_x,Acc_y,Acc_z,Gyro_x,Gyro_y,Gyro_z,Mag_x,Mga_y,Mag_z,Event";
                writer.append(label);
                writer.newLine();
                writer.append(str);
                writer.newLine();
                writer.flush();
                writer.close();
            }
            else
            {
                fw = new FileWriter(file,true);
                writer = new BufferedWriter(fw);
                writer.append(str);
                writer.newLine();
                writer.flush();
                writer.close();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    void writearraylist(String filename,ArrayList<String> list)
    {
        File file = new File(filename);
        try{
            FileWriter fw = new FileWriter(file,true);
            BufferedWriter writer = new BufferedWriter(fw);
            for (int i=0;i<list.size();i++)
            {
                writer.append(list.get(i));
                writer.newLine();
                writer.flush();
                writer.close();
            }
            list.clear();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    void httppost(String filename)
    {

    }
    String  senddata(String existingFileName)
    {
        String urlString="http://"+ip+":"+portid;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String responseFromServer = "";
        try{

            //------------------ CLIENT REQUEST
            FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
            // open a URL connection to the Servlet
            URL url=null;
            try{url = new URL(urlString);}
            catch (Exception e){showNotification("Invalid Address"+ip+":"+portid);}
            // Open a HTTP connection to the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            // Allow Outputs
            conn.setDoOutput(true);
            // Don't use a cached copy.
            conn.setUseCaches(false);

            // Use a post method.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", existingFileName);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            //read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // close streams
            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
            showNotification(serverResponseMessage+serverResponseCode);
            fileInputStream.close();
            dos.flush();
            dos.close();
        }
        catch (MalformedURLException ex)
        {
            Log.e("Debug", "error: " + ex.getMessage(), ex);
        }
        catch (IOException ioe)
        {
            Log.e("Debug", "error: " + ioe.getMessage(), ioe);
        }
        String str="",str1="";

        try {

            inStream = new DataInputStream(conn.getInputStream());
            while ((str = inStream.readLine()) != null) {
                Log.e("Debug", "Server Response " + str);
                // str1=str1+str;
            }
            inStream.close();
        } catch (IOException ioex)
        {
            Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }
        return str1;
    }
    void httpget(String file)
    {
        HttpResponse response = null;
        try {
            // Create http client object to send request to server
            HttpClient client = new DefaultHttpClient();
            // Create URL string
            String URL = "http://"+ip+":"+portid+"/mytest/?name="+filetostring(file);
            // Create Request to server and get response
            HttpGet httpget= new HttpGet();
            httpget.setURI(new URI(URL));
            response = client.execute(httpget);
            showNotification(EntityUtils.toString(response.getEntity())+"result");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }
    String filetostring(String file1)
    {
        File file = new File(file1);
        String line,temp="";

//Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));


            while ((line = br.readLine()) != null) {
                temp=temp+"#"+line;
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return temp;
    }
    public void uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;
        String upLoadServerUri = "http://"+ip;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        int serverResponseCode=0;
        String serverResponseMessage;
        DataInputStream inStream = null;

            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=uploaded_file ;filename="
                        + fileName + "" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
            }
        String str="",str1="";

        try {

            inStream = new DataInputStream(conn.getInputStream());
            while ((str = inStream.readLine()) != null) {
                Log.e("Debug", "Server Response " + str);
                 str1=str1+str;
            }
            inStream.close();
        } catch (IOException ioex)
        {
            Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }
        if(serverResponseCode == 200){
            ///uploaded successfully
            showNotification(str1);
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
        // End else block
    }
}
