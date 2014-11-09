package com.qingluan.darkh.videocontroll.network;

import com.qingluan.darkh.videocontroll.tools.JsonTools;

import org.json.JSONObject;

/**
 * Created by darkh on 11/9/14.
 */
public class NetworkInteract {

    private JsonTools jsonTools;
    public NetworkInteract(){
        jsonTools = new JsonTools();

    }

    public String RegisterRespond(String device_id){
        jsonTools.addData("des","register");
        JsonTools sub_obj  = new JsonTools();
        sub_obj.addData("id",device_id);
        sub_obj.addData("type","device");
        jsonTools.addData("device",sub_obj.getJsonObj());
        return jsonTools.toString();
    }

}
