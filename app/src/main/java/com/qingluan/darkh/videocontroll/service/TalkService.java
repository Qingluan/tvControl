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


    private String ws = ARGUMENTS.WS_CONNECT_URL;
    private String tag = TalkService.class.getName();
    private IntentFilter command_filter;
    private RecivedBroadcastReceiver command_receiver;
    private NetworkHandler connection;

    public TalkService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        command_filter = new IntentFilter();
        command_filter.addAction(ARGUMENTS.SEND_BROADCAST_ACTION);

        command_receiver = new RecivedBroadcastReceiver();
        command_receiver.setReceivedListener(new RecivedBroadcastReceiver.ReceivedListener() {
            @Override
            public void recieved(String info) {
                Log.d(tag,info);
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle info = intent.getExtras();
        int signal = info.getInt(ARGUMENTS.SIGNAL_KEY);
        switch (signal){
            case TalkService.START_CONNET:
                String url = info.getString(ARGUMENTS.WS_CONNECT_URL);
                connection = new NetworkHandler(getApplicationContext(),url);
                break;
            case TalkService.STOP_CONNET:
                break;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
