package com.qingluan.darkh.videocontroll;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qingluan.darkh.videocontroll.arguments.ARGUMENTS;
import com.qingluan.darkh.videocontroll.service.BroadcastNotifer;
import com.qingluan.darkh.videocontroll.service.RecivedBroadcastReceiver;
import com.qingluan.darkh.videocontroll.service.RecivedBroadcastReceiver.ReceivedListener;
import com.qingluan.darkh.videocontroll.service.RecivedIntentService;
import com.qingluan.darkh.videocontroll.service.TalkService;
import com.qingluan.darkh.videocontroll.tools.LogTools;


public class MainActivity extends Activity {
    /*
        widget area
     */
    TextView tv;
    EditText info_editer;

    /*
        normal arguments area
     */
    private Intent recived_intent_service_intent;
    private IntentFilter filter ;
    private RecivedBroadcastReceiver broadcaster;
    private RecivedBroadcastReceiver info_broadcaster;

    public BroadcastNotifer broadcast_notifer;
    public String tag = MainActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.UI_init();

        this.Service_init();


        this.init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    private void UI_init(){
        this.tv = (TextView)findViewById(R.id.disText);
        this.info_editer = (EditText)findViewById(R.id.edit);
    }

    private void init(){

        /*
            start service in background
         */
        recived_intent_service_intent = new Intent(this,TalkService.class);
        Bundle bundle_start = new Bundle();
        bundle_start.putInt(ARGUMENTS.SIGNAL_KEY,RecivedIntentService.SIGNAL_CONNECT);
        bundle_start.putString(ARGUMENTS.URL_KEY,ARGUMENTS.WS_CONNECT_URL);
        recived_intent_service_intent.putExtras(bundle_start);
        this.startService(recived_intent_service_intent);
        LogTools.ToastLog(this, "starting service");
        Log.d(tag,"starting service ");
    }

    private void Service_init(){
        Log.d(tag,"service init");
        Toast.makeText(this,"server init ",Toast.LENGTH_SHORT).show();
        this.broadcast_notifer = new BroadcastNotifer(this);
        filter = new IntentFilter();
        filter.addAction(ARGUMENTS.GET_BROADCAST_ACTION);

        this.broadcaster = new RecivedBroadcastReceiver();

        /*
            this setting text to display received info
         */

        this.broadcaster.setReceivedListener(new ReceivedListener() {

            @Override
            public void recieved(String info) {
                tv.setText(info);
                unregisterReceiver(broadcaster);
            }
        });



        /*
            register broadcast to listening  background
         */
        this.registerReceiver(this.broadcaster,this.filter);

    }

    private void info_broadcast_init(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ARGUMENTS.INFO_ACTION);
        info_broadcaster.setReceivedListener(new ReceivedListener() {
            @Override
            public void recieved(String info) {

            }
        });
        info_broadcaster = new RecivedBroadcastReceiver();
        info_broadcaster.setReceivedListener(new ReceivedListener() {
            @Override
            public void recieved(String info) {
                Toast.makeText(getApplicationContext(),info,Toast.LENGTH_SHORT).show();
            }
        });
    }


    /*
        some ui listener area
     */
    public void bt_send_click(View view){
        String now_text = this.tv.getText().toString();
        this.broadcast_notifer.sendIntentService(this,now_text);


    }

    public void startService(){

    }

}
