package com.example.android.globalactionbarservice;

import org.json.JSONException;
import org.json.JSONObject;

public class PluginViewNode {

    private final JSONObject node;

    public PluginViewNode(JSONObject node){
        this.node=node;
    }

    public String getResourceId(){
        String rId = null;
        try {
            rId = node.getString("RESOURCE_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (rId == null){
            return "";
        }
        return rId;
    }


}
