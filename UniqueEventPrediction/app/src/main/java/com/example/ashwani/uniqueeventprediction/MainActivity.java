package com.example.ashwani.uniqueeventprediction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.app.*;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.view.menu.ExpandedMenuView;
import android.text.InputFilter;
import android.text.method.HideReturnsTransformationMethod;
import android.widget.*;
import android.graphics.*;
import android.content.res.Resources;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.media.*;
import android.content.*;
import android.app.*;
import java.io.IOException;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.logging.*;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import static android.R.attr.animation;
import static android.R.attr.port;

public class MainActivity extends AppCompatActivity implements OnClickListener,GestureDetector.OnGestureListener,AdapterView.OnItemSelectedListener {
    RelativeLayout testlayout,traininglayout,animationlayout;
    Button startbutton,connect;
    RelativeLayout.LayoutParams startbutton_detail,status_detail,user_detail,spin1_detail,spin2_detail,addresset_detail,portet_detail,toggle_detail,connect_detail,soundtext_detail,sensortext_detail;
    TextView status,soundtext,sensortext;
    Switch toggle;
    Spinner spin1,spin2;
    permissionclass permissionasking;
    String storagepath = Environment.getExternalStorageDirectory().getAbsolutePath();
    GestureDetectorCompat gesturedetector;
    EditText user,addresset,portet;
    ArrayAdapter adapter1,adapter2;
    String[] sensorpredictionlist=new String[]{"Cycling","Walking","Running","StairUp","StairDown","Jumping"};
    String[] soundpredictionlist=new String[]{"Glass Breaking","Door open Close","Tab Water Sound","Chair Shifting","Flush Water","Clapping","CoinDrop"};
    String userstring="Username...",addressstring="10.0.0.0",portstring="",spinner1string="",spinner2string="";

    Intent soundintent,sensorintent;

    int mode=-1, background = 0;
    int switchstate=0;
    int samplingrate=50000;
    private GoogleApiClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        storagepath=storagepath + File.separator + "Prediction";
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //wakeLock.acquire();
        directorymanager();
        startbutton = new Button(this);
        startbutton.setText("Start");
        startbutton.setBackgroundResource(R.layout.roundcorner);
        startbutton.setHeight(dptopx(120));
        startbutton.setWidth(dptopx(120));
        startbutton.setId(3);
        startbutton_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        startbutton_detail.addRule(RelativeLayout.CENTER_HORIZONTAL);
        startbutton_detail.addRule(RelativeLayout.CENTER_VERTICAL);

        connect = new Button(this);
        connect.setText("Connect");
        connect.setBackgroundResource(R.layout.connect);
        connect.setHeight(dptopx(50));
        connect.setWidth(dptopx(150));
        connect.setId(8);
        connect_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        connect_detail.addRule(RelativeLayout.CENTER_HORIZONTAL);
        connect_detail.addRule(RelativeLayout.BELOW,startbutton.getId());
        connect_detail.setMargins(0,dptopx(110),0,0);

        status = new TextView(this);
        status.setHeight(dptopx(100));
        status.setWidth(dptopx(160));
        status.setId(100);
        status.setText("");
        status.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        status_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        status_detail.addRule(RelativeLayout.CENTER_HORIZONTAL);
        status_detail.addRule(RelativeLayout.ABOVE, startbutton.getId());

        soundtext = new TextView(this);
        soundtext.setHeight(dptopx(100));
        soundtext.setWidth(dptopx(160));
        soundtext.setId(51);
        soundtext.setText("Sound");
        soundtext.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        soundtext_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        soundtext_detail.setMargins(dptopx(135),dptopx(35),0,0);
        soundtext.setLayoutParams(soundtext_detail);

        sensortext = new TextView(this);
        sensortext.setHeight(dptopx(100));
        sensortext.setWidth(dptopx(160));
        sensortext.setId(51);
        sensortext.setText("Sensor");
        sensortext.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sensortext_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        sensortext_detail.setMargins(dptopx(50),dptopx(35),0,0);
        sensortext.setLayoutParams(sensortext_detail);

        spin1=new Spinner(this);
        adapter1 = new ArrayAdapter(this,android.R.layout.simple_spinner_item,sensorpredictionlist);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin1.setAdapter(adapter1);
        spin1.setId(5);
        spin1_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        spin1_detail.setMargins(dptopx(215),dptopx(24),0,0);
        spin1.setLayoutParams(spin1_detail);

        spin2=new Spinner(this);
        adapter2 = new ArrayAdapter(this,android.R.layout.simple_spinner_item,soundpredictionlist);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin2.setAdapter(adapter2);
        spin2.setId(66);
        spin2_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        spin2_detail.setMargins(dptopx(215),dptopx(60),0,0);
        spin2.setLayoutParams(spin2_detail);


        user = new EditText(this);
        user.setText(userstring);
        user.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        user.setHeight(dptopx(40));
        user.setWidth(dptopx(140));
        user.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        user.setId(4);
        user_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        user_detail.setMargins(dptopx(18),dptopx(20),0,0);
        user.setLayoutParams(user_detail);

        addresset=new EditText(this);
        addresset.setText(addressstring);
        //addresset.setHeight(dptopx(30));
        addresset.setWidth(dptopx(305));
        addresset.setFilters(new InputFilter[] { new InputFilter.LengthFilter(40) });
        addresset.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        addresset.setId(6);
        // addresset.setBackgroundColor(Color.argb(220, 144, 240, 199));
        addresset_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        addresset_detail.setMargins(dptopx(25),dptopx(335),0,0);
        addresset.setLayoutParams(addresset_detail);

        portet=new EditText(this);
        portet.setText(portstring);
        // portet.setHeight(dptopx(30));
        portet.setWidth(dptopx(60));
        portet.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        portet.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        portet.setId(7);
        // portet.setBackgroundColor(Color.argb(220, 144, 240, 199));
        portet_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        portet_detail.setMargins(dptopx(250),dptopx(315),0,0);
        portet.setLayoutParams(portet_detail);

        toggle = new Switch(this);
        toggle.setHeight(dptopx(30));
        toggle.setWidth(dptopx(60));
        toggle.setId(50);
        // portet.setBackgroundColor(Color.argb(220, 144, 240, 199));
        toggle_detail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        toggle_detail.setMargins(dptopx(142),dptopx(30),0,0);
        toggle.setLayoutParams(toggle_detail);
        //toggle.s
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    if(isMyServiceRunning(sensorservice.class)==true)
                    {
                        deloadservice();
                        startbutton.setText("Start");
                        startbutton.setBackgroundResource(R.layout.roundcorner);
                    }
                    switchstate = 1;
                }
                else
                {
                    if(isMyServiceRunning(soundservice.class)==true)
                    {

                        deloadservice();
                        startbutton.setText("Start");
                        startbutton.setBackgroundResource(R.layout.roundcorner);
                    }
                    switchstate = 0;
                }
            }
        });
        testlayout = new RelativeLayout(this);
        testlayout.setBackgroundColor(Color.argb(200, 114, 200, 200));
        testlayout.setId(1);
        traininglayout = new RelativeLayout(this);
        traininglayout.setBackgroundColor(Color.argb(200, 124, 230, 180));
        traininglayout.setId(2);
        animation();
        gesturedetector=new GestureDetectorCompat(this,this);
        startbutton.setOnClickListener(this);
        connect.setOnClickListener(this);
        user.setOnClickListener(this);
        portet.setOnClickListener(this);
        spin1.setOnItemSelectedListener(this);
        spin2.setOnItemSelectedListener(this);
        addresset.setOnClickListener(this);
        user.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                {
                    if(user.getText().toString().equals("Username...")) user.setText("");
                    if(startbutton.getText().toString()=="Stop")try{startbutton.setText("Start");startbutton.setBackgroundResource(R.layout.roundcorner);}catch(Exception e){}
                }
                else//lossing focus
                {
                    if(user.getText().length()==0) user.setText(userstring);
                    else{userstring=user.getText().toString();
                    }
                }
            }
        });
        addresset.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if(addresset.getText().toString().equals("192.168.1.1")) addresset.setText("");
                }else {
                    if(addresset.getText().length()==0) addresset.setText(addressstring);
                    else{addressstring=addresset.getText().toString();
                    }
                }
            }
        });
        portet.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if(portet.getText().toString().equals("5435")) portet.setText("");
                }else {
                    if(portet.getText().length()==0) portet.setText(portstring);
                    else{portstring=portet.getText().toString();}
                }
            }
        });

        traininglayout.addView(user);
        traininglayout.addView(spin1);
        traininglayout.addView(spin2);
        testlayout.addView(toggle);
        testlayout.addView(sensortext);
        testlayout.addView(soundtext);
        mode=0;
        permissionasking=new permissionclass(this);
        permissionasking.execute();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    void defaultsetting()
    {
        try
        {
            traininglayout.removeView(status);
            traininglayout.removeView(startbutton);
            traininglayout.removeView(connect);
            traininglayout.removeView(addresset);
            //traininglayout.removeView(portet);
        }catch(Exception e){}
        try{
            testlayout.addView(startbutton, startbutton_detail);
            testlayout.addView(status, status_detail);
            testlayout.addView(connect,connect_detail);
            testlayout.addView(addresset);
           // testlayout.addView(portet);
        }catch(Exception e){}
        setContentView(testlayout);
    }
    protected void onResume() {
        super.onResume();
        background = 0;
        //soundthread = new soundthreadclass(this);
        //soundthread.execute();
        //
    }
    void traininglayoutmanager()
    {
        mode=1;
        try {
            testlayout.removeView(status);
            testlayout.removeView(startbutton);
            testlayout.removeView(connect);
            testlayout.removeView(addresset);
           // testlayout.removeView(portet);

        }catch(Exception e){}
        try{
            traininglayout.addView(startbutton, startbutton_detail);
            traininglayout.addView(status, status_detail);
            traininglayout.addView(connect,connect_detail);
            traininglayout.addView(addresset);
          //  traininglayout.addView(portet);
        }
        catch(Exception e){}
        setContentView(traininglayout);
    }
    @Override
    protected void onPause() {
        super.onPause();
        background = 1;
        //
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.gesturedetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    @Override
    public boolean onDown(MotionEvent e) {return false;}
    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) {return false;}
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        //left
        if(distanceX>70&&mode==0)
        {
            traininglayoutmanager();
        }
        //right
        else if(distanceX<-70&&mode==1)
        {
            mode=0;
            defaultsetting();

        }
        return true;
    }
    @Override
    public void onLongPress(MotionEvent e) {}
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {return false;}

    public void onClick(View v) {
        if (v.equals(startbutton)) {
            if (startbutton.getText() == "Start") {
                startbutton.setText("Stop");
                startbutton.setBackgroundResource(R.layout.roundcornerstop);
                stringsetter();
                loadsevices();
            } else if (startbutton.getText() == "Stop") {
                startbutton.setText("Start");
                startbutton.setBackgroundResource(R.layout.roundcorner);
                deloadservice();

            }
            try {

            } catch (Exception e) {
                status.setText("Exception at start button");
            }
        } else if (v.equals(connect))
        {
            if(isOnline()&&httpget())
                status.setText("Connected");
            else
                status.setText("DisConnected");
        }
        else if (v.equals(user))
        {
        }
        else if (v.equals(addresset))
        {

        }
        else if (v.equals(portet))
        {
        }

    }
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long id) {
        String item = arg0.getItemAtPosition(position).toString();
        if(startbutton.getText().toString()=="Stop")try{startbutton.setText("Start");startbutton.setBackgroundResource(R.layout.roundcorner);}catch(Exception e){}
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
    void loadsevices()
    {
        if(switchstate==1)
        {
            soundintent = new Intent(this,soundservice.class);
        soundintent.putExtra("user",userstring);
        soundintent.putExtra("port",portstring);
        soundintent.putExtra("ip",addressstring);
        soundintent.putExtra("filename","Prediction");
        soundintent.putExtra("mode",mode+"");
        soundintent.putExtra("sensor",spinner1string);
        soundintent.putExtra("sound",spinner2string);
        soundintent.putExtra("sampler",samplingrate);
            this.startService(soundintent);
        }
        else{
        sensorintent = new Intent(this,sensorservice.class);
        sensorintent.putExtra("user",userstring);
        sensorintent.putExtra("port",portstring);
        sensorintent.putExtra("ip",addressstring);
        sensorintent.putExtra("filename","Prediction");
        sensorintent.putExtra("mode",mode+"");
        sensorintent.putExtra("sensor",spinner1string);
        sensorintent.putExtra("sound",spinner2string);
        sensorintent.putExtra("sampler",samplingrate);
            this.startService(sensorintent);
        }



    }

    void deloadservice()
    {
        if(switchstate==1) {
            soundintent = new Intent(this, soundservice.class);
            this.stopService(soundintent);
        }
        else {
            sensorintent = new Intent(this, sensorservice.class);
            this.stopService(sensorintent);
        }
    }
    void stringsetter()
    {
        addressstring=addresset.getText().toString();
        portstring=portet.getText().toString();
        userstring=user.getText().toString();
        spinner1string=spin1.getSelectedItem().toString();
        spinner2string=spin2.getSelectedItem().toString();
    }
    void stringloader()
    {
        status.setText("deafult");
        addresset.setText(addressstring);
        portet.setText(portstring);
        user.setText(userstring);
        spin1.setSelection(Arrays.asList(sensorpredictionlist).indexOf(spinner1string));
        spin2.setSelection(Arrays.asList(soundpredictionlist).indexOf(spinner2string));
    }
    boolean directorymanager() {
        File folder = new File(storagepath);
        if (!(folder.exists()))
            try {folder.mkdir();} catch (Exception e) {Toast.makeText(getApplicationContext(), "EXCEPTION: Prediction Folder Not Created", Toast.LENGTH_LONG).show();}
        folder = new File(storagepath+File.separator + "Setting");
        if (!(folder.exists()))
            try {folder.mkdir();} catch (Exception e) {Toast.makeText(getApplicationContext(), "EXCEPTION: Setting Folder Not Created", Toast.LENGTH_LONG).show();}
        folder = new File(storagepath+File.separator + "Text Training Samples");
        if (!(folder.exists()))
            try {folder.mkdir();} catch (Exception e) {Toast.makeText(getApplicationContext(), "EXCEPTION: Text Training Samples Folder Not Created", Toast.LENGTH_LONG).show();}
        folder = new File(storagepath+File.separator + "Text Test samples");
        if (!(folder.exists()))
            try {folder.mkdir();} catch (Exception e) {Toast.makeText(getApplicationContext(), "EXCEPTION: Text Test Samples Folder Not Created", Toast.LENGTH_LONG).show();}
        folder = new File(storagepath+File.separator + "Audio Training samples");
        if (!(folder.exists()))
            try {folder.mkdir();} catch (Exception e) {Toast.makeText(getApplicationContext(), "EXCEPTION: Audio Training Samples Folder Not Created", Toast.LENGTH_LONG).show();}
        folder = new File(storagepath+File.separator + "Audio Test samples");
        if (!(folder.exists()))
            try {folder.mkdir();} catch (Exception e) {Toast.makeText(getApplicationContext(), "EXCEPTION: Audio Test Samples Folder Not Created", Toast.LENGTH_LONG).show();}
        return true;
    }

    int dptopx(int key) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, key, getResources().getDisplayMetrics());
    }
    void animation()
    {
        animationlayout =new RelativeLayout(this);
        animationlayout.setId(8);
        animationlayout.setBackgroundColor(Color.rgb(255,255,255));
        animationclass am=new animationclass(this);
        am.execute();

        int i;
        /*for (i=255;i>=0;i--)
        {
            try {Thread.sleep(5);} catch (Exception e) {}
            int j=(i/3)+128;
            int k=i/5+32;
            animationlayout.setBackgroundColor(Color.rgb(k,j,i));
            setContentView(animationlayout);
        }
            if(mode==0) {defaultsetting();setContentView(testlayout);}
            else {traininglayoutmanager();setContentView(traininglayout);}*/
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public boolean isOnline()
    {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
    boolean checkhostavailable() {
        boolean exists = false;

        try {
            SocketAddress sockaddr = new InetSocketAddress(InetAddress.getByName(addressstring), Integer.parseInt(portstring));
            // Create an unbound socket
            Socket sock = new Socket();

            // This method will block no more than timeoutMs.
            // If the timeout occurs, SocketTimeoutException is thrown.
            int timeoutMs = 2000;   // 2 seconds
            sock.connect(sockaddr, timeoutMs);
            exists = true;
        } catch (Exception e) {
        }
        return false;
    }
    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.ashwani.uniqueeventprediction/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        lastsettingmanager(true);
      /*  if(isMyServiceRunning(soundservice.class)==true)
        {
            soundintent = new Intent(this, soundservice.class);
            this.stopService(soundintent);
        }
        if(isMyServiceRunning(sensorservice.class)==true)
        {
            sensorintent = new Intent(this, sensorservice.class);
            this.stopService(sensorintent);
        }*/
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.ashwani.uniqueeventprediction/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
    String datename()
    {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");
        dateFormatter.setLenient(false);
        Date today = new Date();
        String date = dateFormatter.format(today);
        return date;
    }
    boolean lastsettingmanager(boolean t)
    {
        String filename = storagepath+File.separator+"Setting"+File.separator+"sat"+".data";
        if(t)
        {
            stringsetter();
            File file = new File(filename);
            try{
                FileWriter fw = new FileWriter(file,false);
                BufferedWriter writer = new BufferedWriter(fw);
                writer.append(userstring);
                writer.newLine();
                writer.append(spinner1string);
                writer.newLine();
                writer.append(spinner2string);
                writer.newLine();
                writer.append(addressstring);
                writer.newLine();
                writer.append(portstring);
                writer.newLine();
                writer.append(mode+"");
                writer.newLine();
                writer.flush();
                writer.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return true;
        }
        else
        {
            try
            {
                File myFile = new File(filename);

                if (myFile.exists())
                {FileInputStream fIn = new FileInputStream(myFile);
                    InputStreamReader inputreader = new InputStreamReader(fIn);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line="",lin="";
                    try
                    {
                        line = buffreader.readLine();
                        lin+=line;
                        userstring=line;
                        line = buffreader.readLine();
                        lin+=line;
                        spinner1string=line;
                        line = buffreader.readLine();
                        lin+=line;
                        spinner2string=line;
                        line = buffreader.readLine();
                        lin+=line;
                        addressstring=line;
                        line = buffreader.readLine();
                        lin+=line;
                        portstring=line;
                        line = buffreader.readLine();
                        lin+=line;
                        mode=Integer.parseInt(line);
                        status.setText(lin);
                        stringloader();
                    }catch (Exception e)
                    {
                        status.setText("Error In openeing in setting file");
                        e.printStackTrace();
                    }

                    return true;
                }
                else {return false;}
            }
            catch (Exception e)
            {
                String error="";
                status.setText("hellish");
                error=e.getMessage();
                return false;
            }
        }
    }
    boolean httpget()
    {
        boolean choice=false;
        HttpResponse response = null;
        try {
            // Create http client object to send request to server
            HttpClient client = new DefaultHttpClient();
            // Create URL string
            String URL = "http://"+addressstring;
            // Create Request to server and get response
            HttpGet httpget= new HttpGet();
            httpget.setURI(new URI(URL));
            response = client.execute(httpget);
            if(EntityUtils.toString(response.getEntity()).equals("OK")) choice=true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return choice;
    }
}
class sensordata
{
    float acc_x=0;
    float acc_y=0;
    float acc_z=0;
    float gyro_x=0;
    float gyro_y=0;
    float gyro_z=0;
    float mag_x=0;
    float mag_y=0;
    float mag_z=0;
    float proximity=0;
}

class animationclass extends AsyncTask<Integer, Integer ,String>
{
    MainActivity mainclass;
    boolean stopper=true;
    animationclass(MainActivity mainclass) {
        this.mainclass = mainclass;
    }
    protected String doInBackground(Integer... params) {
        //int random = new Random().nextInt(10000);
        for (int i=255;i>=0;i--) {
            try{Thread.sleep(3);}catch (Exception e){}
            publishProgress(i);
        }
        publishProgress(0);
        return "DONE";

    }

    protected void onProgressUpdate(Integer... state) {
        int j=(state[0]/3)+128;
        int k=state[0]/5+32;
        mainclass.animationlayout.setBackgroundColor(Color.rgb(k,j,state[0]));
        mainclass.setContentView(mainclass.animationlayout);
        if(state[0]==0)
        {
            boolean b=mainclass.lastsettingmanager(false);
            if(b)
            {
                if(mainclass.mode==0) {mainclass.defaultsetting();mainclass.setContentView(mainclass.testlayout);}
                else {mainclass.traininglayoutmanager();mainclass.setContentView(mainclass.traininglayout);}
            }
        }
    }
    protected void onPostExecute(String... result)
    {
    }
}
class permissionclass extends AsyncTask<Integer, Integer ,String>
{
    MainActivity mainclass;
    final int MY_PERMISSIONS=203;
    permissionclass(MainActivity mainclass) {
        this.mainclass = mainclass;
    }
    protected String doInBackground(Integer... params) {
        try{Thread.sleep(4000);}catch(Exception e){}
        String filename = mainclass.storagepath+File.separator+"Setting"+File.separator+"sat"+".data";
        File file = new File(filename);
        if(!file.exists()) {
            ActivityCompat.requestPermissions(mainclass, new String[]{
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.VIBRATE
            }, MY_PERMISSIONS);
        }
        return "";
    }
    protected void onProgressUpdate(Integer... state)
    {
    }
    protected void onPostExecute(String... result)
    {
    }
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mainclass.getApplicationContext(),"permission granted", Toast.LENGTH_LONG).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
}