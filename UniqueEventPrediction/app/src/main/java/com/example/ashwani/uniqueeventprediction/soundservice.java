package com.example.ashwani.uniqueeventprediction;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import android.os.Vibrator;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class soundservice extends android.app.Service {
    String user="",filename="",ip="",portid="",soundactivity="",sensoractivity="";
    int mode=-1,samplingrate;
    boolean stopper=true;
    String storagepath=Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Prediction";
    /////////////////////////////////////////////
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    int RECORDER_SAMPLERATE = 8000;
    int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    AudioRecord recorder = null;
    Thread recordingThread = null;
    boolean isRecording = false;
    int RECORDER_BPP = 16;
    int bufferSize;
    //////////////////////////////////////////////
    public soundservice() {
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
        try{ samplingrate = Integer.parseInt(intent.getStringExtra("sampler").toString());}
        catch(Exception e){samplingrate=3000;}
        mode = Integer.parseInt(intent.getStringExtra("mode"));
        soundservicethread t = new soundservicethread(this);
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
    class soundservicethread extends Thread {
        MediaRecorder audiorecorder;
        soundservice ss;
        soundservicethread(soundservice ss) {
            this.ss=ss;
        }
        public void run() {
            while(stopper)
            {
                String date = datename();
                try {

                if (mode == 0)
                    filename = storagepath + File.separator + "Audio Test samples" + File.separator + date + ".3gp";
                if (mode == 1)
                    filename = storagepath + File.separator + "Audio Training samples" + File.separator +soundactivity+":"+ date + ".3gp";
                audiorecorder = new MediaRecorder();
                audiorecorder.reset();
                audiorecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                audiorecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                audiorecorder.setOutputFile(filename);
                audiorecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                try {
                    audiorecorder.prepare();
                } catch (IOException e) {
                }
                audiorecorder.start();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                audiorecorder.stop();
                audiorecorder.release();
                audiorecorder = null;
                }
                catch(Exception e){}
                if (mode==0) {
                    ///send data
                    //showNotification("sound data captured");
                    try{//String temp=senddata(filename);
                       // httppost(filename);
                       // httpget();
                      //  showNotification(filetostring(filename));
                       // httpget(filename);
                     uploadFile(filename);
                        }
                    catch (Exception e){showNotification("ERROR:Sound Captured But Not Sended");}
                }
                else if(mode==1){
                    stopper = false;
                    showNotification("sound data captured for Training");

                }
            }
        }
    }
    public void showNotification(String message)
    {
        int mNotificationId = Integer.parseInt(notid().toString());
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_ic_googleplayservices)
                        .setContentTitle(notid())
                        .setContentText(message)
                        .setAutoCancel(true);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
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
            // close streams
            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
            showNotification(serverResponseMessage+serverResponseCode+urlString);
            // fileInputStream.close();
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
        String str="",str1="hi ";

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
        return str1;
    }
    void httppost(String file1)
    {
        String urlString="http://"+ip+":"+portid;
File file = new File(file1);
try {
    HttpClient httpclient = new DefaultHttpClient();

    HttpPost httppost = new HttpPost(urlString);

    InputStreamEntity reqEntity = new InputStreamEntity(
            new FileInputStream(file), -1);
    reqEntity.setContentType("binary/octet-stream");
    reqEntity.setChunked(true); // Send in multiple parts if needed
    httppost.setEntity(reqEntity);
    HttpResponse response = httpclient.execute(httppost);
    showNotification(response.getEntity().toString());
} catch (Exception e) {
    // show error
}
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
            v.vibrate(200);
        }
        // End else block
    }
}
