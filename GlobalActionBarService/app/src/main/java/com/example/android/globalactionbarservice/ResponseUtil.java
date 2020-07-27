package com.example.android.globalactionbarservice;

import android.graphics.Rect;
import android.util.JsonReader;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ResponseUtil {
    final static String TAG = "<ResponseUtil>";

    final static String RESOURCE_ID_KEY = "RESOURCE_ID_KEY";
    final static String READY_KEY = "READY_KEY";

    final static int READY_INT_VALUE = 0;

    public static JSONObject getReadyResponse(){
        Log.i(TAG, "returning ready response");
        JSONObject readyResponse = new JSONObject();
        try {
            readyResponse.put(READY_KEY, READY_INT_VALUE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return readyResponse;

    }

    public static JSONObject getBoundsResponse(ArrayList<AccessibilityNodeInfo> nodes, String resouceId){
        Log.i(TAG, "returning bounds response");
        JSONObject nodeBounds = new JSONObject();
        try {
            nodeBounds.put(RESOURCE_ID_KEY, resouceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // find AccessibilityNodeInfo that cooresponds to
        // node requested by server
        AccessibilityNodeInfo node=null;
        for(AccessibilityNodeInfo n : nodes){

            String nId = GlobalActionBarService.getTruncatedId(n.getViewIdResourceName());
            if (nId.equals(resouceId)){
                node = n;
                break;
            }
        }
        if (node!=null){
            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            try {
                nodeBounds.put("boundsLeft",bounds.left);
                nodeBounds.put("boundsTop",bounds.top);
                nodeBounds.put("boundsRight",bounds.right);
                nodeBounds.put("boundsBottom",bounds.bottom);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return nodeBounds;

    }
}
