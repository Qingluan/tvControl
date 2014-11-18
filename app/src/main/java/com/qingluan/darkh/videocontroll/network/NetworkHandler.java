package com.qingluan.darkh.videocontroll.network;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.qingluan.darkh.videocontroll.arguments.ARGUMENTS;
import com.qingluan.darkh.videocontroll.service.BroadcastNotifer;
import com.qingluan.darkh.videocontroll.service.RecivedIntentService;
import com.qingluan.darkh.videocontroll.tools.FileTools;
import com.qingluan.darkh.videocontroll.tools.JsonTools;

import org.apache.http.Header;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by darkh on 11/6/14.
 */
public class NetworkHandler {

    private String tag = NetworkHandler.class.getName();
    private WebSocketConnection connect_client;
    private Context context;
    private NetworkInteract interact_handler;
    private AsyncHttpClient httpClient;
    private AsyncHttpListener listener;
    BroadcastNotifer notifer;

    /*
        this is websocket
     */
    public NetworkHandler(final Context context,String url){
        this.context = context;
        notifer = new BroadcastNotifer(this.context);
        interact_handler = new NetworkInteract();
        Log.d(tag,"try to connecting "+url);

        this.connect_client = new WebSocketConnection();

        try{
            this.connect_client.connect(url,new WebSocketHandler(){
                @Override
                public void onOpen() {
                    Log.d(tag,"ok connected");
                    String respond_register = interact_handler.RegisterRespond(ARGUMENTS.DEVICE_ID);
                    notifer.sendIntent(ARGUMENTS.INFO_ACTION,respond_register);
                    connect_client.sendTextMessage(respond_register);
                    notifer.sendIntent(ARGUMENTS.GET_BROADCAST_ACTION,"ok  register is ok");
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d(tag,"get message" + payload);
                    JsonTools respond_json = new JsonTools(payload);
                    String destination = (String)respond_json.getData("des");
                    Log.d(tag,destination);
                    if (destination.equals("respond")){
                        Log.d(tag,"is this reach ?");
                        String res = (String)respond_json.getData("res");
                        if (res.equals("ok")){
                            notifer.sendIntent(ARGUMENTS.GET_BROADCAST_ACTION,"ok");
                        }


                    }else if (destination.equals("command")){
                        String command = (String) respond_json.getData("command");
                        notifer.sendIntent(ARGUMENTS.GET_BROADCAST_ACTION,command);
                    }else if (destination.equals("download")){
                        String file = (String)respond_json.getData("name");
                        String url  = (String)respond_json.getData("url");
                        Intent intent = new Intent(context,RecivedIntentService.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(ARGUMENTS.KEY_DOWNLOAD_URL,url);
                        bundle.putString(ARGUMENTS.KEY_DOWNLOAD_FILE_NAME,file);

                        intent.putExtras(bundle);
                        context.startService(intent);

                    }
                    notifer.sendIntent(ARGUMENTS.GET_BROADCAST_ACTION,payload); // this is for test

                }

                @Override
                public void onClose(int code, String reason) {
                    notifer.sendIntent(ARGUMENTS.GET_BROADCAST_ACTION,reason);

                }
            });
        }catch (WebSocketException e){
            Log.d(tag,"error :\n"+e.toString());
        }
        if (this.connect_client != null){
            Log.d(tag,"connected ?");
        }


    }

    public NetworkHandler(final Context context,String url,String key ,String json_data,final String filename){
        httpClient  = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put(key,json_data);
        httpClient.post(url,params,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                FileTools fileTools = new FileTools(context);
                fileTools.saveFile(responseBody,filename);
                listener.afterSave(ARGUMENTS.FILE_ROOT_PATH+"/"+filename);
            }
        });

    }

    public NetworkHandler(final Context context,String url,final String filename){
        httpClient = new AsyncHttpClient();

        httpClient.get(url,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                FileTools fileTools = new FileTools(context);
                fileTools.saveFile(responseBody,filename);
                listener.afterSave(ARGUMENTS.FILE_ROOT_PATH+"/"+filename);
            }
        });
    }


    public boolean send(String info){
        if (this.connect_client.isConnected()){

            Log.d(tag, "sending .......");
            this.connect_client.sendTextMessage(info);
            return  true;

        }
        Log.d(tag,"can not sned ");
        return  false;
    }

    public boolean closeConnect(){
        if(this.connect_client != null){
            this.connect_client.disconnect();
            return true;
        }
        return false;
    }

    public void setAsyncHttpHandler(AsyncHttpListener asyncHttpListener){
        this.listener = asyncHttpListener;
    }

    public interface AsyncHttpListener{
        public void afterSave(String file_path);
    }

    public boolean isConnected(){
        return this.connect_client.isConnected();
    }
}
