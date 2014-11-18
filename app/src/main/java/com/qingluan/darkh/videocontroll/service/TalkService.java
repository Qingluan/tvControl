package com.qingluan.darkh.videocontroll.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.qingluan.darkh.videocontroll.arguments.ARGUMENTS;
import com.qingluan.darkh.videocontroll.network.NetworkHandler;
import com.qingluan.darkh.videocontroll.network.NetworkInteract;
import com.qingluan.darkh.videocontroll.tools.JsonTools;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class TalkService extends Service {
    /*
        signal
     */
    public static final int START_CONNET = 1;
    public static final int STOP_CONNET = 0;
    public static final int SEND_INFO = 2;

    private String ws = ARGUMENTS.WS_CONNECT_URL;
    private String tag = TalkService.class.getName();
    private IntentFilter command_filter;
    private RecivedBroadcastReceiver command_receiver;
    private NetworkHandler connection;
    private BroadcastNotifer notifer;
    public TalkService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        command_filter = new IntentFilter();
        notifer = new BroadcastNotifer(getApplicationContext());
        command_filter.addAction(ARGUMENTS.SEND_BROADCAST_ACTION);

        command_receiver = new RecivedBroadcastReceiver();

        this.registerReceiver(command_receiver, command_filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle info = null;
        int signal  = STOP_CONNET;
        try {
            info = intent.getExtras();
            Log.d(tag,"this is ok .no above");
        }catch (NullPointerException e){
            Log.d(tag,"here is eroor");
            e.printStackTrace();
        }

        try {
            signal = info.getInt(ARGUMENTS.SIGNAL_KEY);
        }catch (NullPointerException e){
            Log.d(tag,"signal is null  ");
            e.printStackTrace();
        }
        switch (signal){
            case TalkService.START_CONNET:
                String url = info.getString(ARGUMENTS.URL_KEY);

                command_receiver.setReceivedListener(new RecivedBroadcastReceiver.ReceivedListener() {
                    @Override
                    public void recieved(String info) {
                        Log.d(tag,info);
                        connection.send(info);
                    }
                });
                connection = new NetworkHandler(getApplicationContext(),url);
                break;
            case TalkService.STOP_CONNET:
                break;
            case TalkService.SEND_INFO:
                String message = info.getString(ARGUMENTS.SEND_MESSAGE);
                if (connection.isConnected()){
                    this.notifer.sendIntent(ARGUMENTS.INFO_ACTION,"connected ");
                }
                connection.send(message);
                break;
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.d(tag,"this is reach? destroy service");
        unregisterReceiver(command_receiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
