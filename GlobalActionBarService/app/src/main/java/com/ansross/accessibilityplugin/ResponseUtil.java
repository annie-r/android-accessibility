package com.ansross.accessibilityplugin;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import java.util.ArrayList;

public class ResponseUtil {
    public final static int GET_FOCUSED_ELEMENT_ID = 0;
    public final static int GET_ACCESS_NODES_AND_LABELS = 1;
    public final static int GET_BOUNDS_FOR_ELEMENT_ID = 2;

    final static String TAG = "<ResponseUtil>";

    final static String RESOURCE_ID_KEY = "RESOURCE_ID_KEY";
    final static String READY_KEY = "READY_KEY";
    final static String LABEL_KEY = "LABEL_KEY";

    final static String NO_RESOURCE_ID_VALUE = "<NO_ID>";
    final static String INVALID_NODE_VALUE = "<NO_NODE>";
    final static String NO_LABEL_VALUE = "<NO_LABEL>";

    final static int READY_INT_VALUE = 100;

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

    public static JSONObject getFocusedElementResponse(AccessibilityNodeInfo focus){
        JSONObject nodeJson = new JSONObject();
        try{
            if(focus != null){
                putResourceId(focus,nodeJson);
                putLabel(focus,nodeJson);
            } else {
                //TODO better messaging
                nodeJson.put(RESOURCE_ID_KEY,INVALID_NODE_VALUE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return nodeJson;
    }

    public static JSONObject getNodesAndLabelsResponse(AccessibilityNodeInfo root){
        JSONObject nodes = new JSONObject();
        ArrayList<AccessibilityNodeInfo> accessibilityNodeInfos = new ArrayList<>();
        NodeUtil.getImportantForAccessibilityNodeInfo(root,accessibilityNodeInfos);
        for (AccessibilityNodeInfo nodeInfo : accessibilityNodeInfos){
            String resourceId = nodeInfo.getViewIdResourceName();
            if (resourceId!=null){
                resourceId = NodeUtil.getTruncatedId(resourceId);
            } else{
                resourceId = NO_RESOURCE_ID_VALUE;
            }
            String label = NodeUtil.getLabel(nodeInfo);
            if (label == null) {
                label=NO_LABEL_VALUE;
            }
            try {
                nodes.put(resourceId,label);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return nodes;
    }

    public static JSONObject getBoundsResponse(AccessibilityNodeInfo root, String resourceId){
        //TODO need to cache nodes
        ArrayList<AccessibilityNodeInfo> nodeInfos = new ArrayList<>();
        NodeUtil.getImportantForAccessibilityNodeInfo(root,nodeInfos);
        return getBoundsResponse(nodeInfos, resourceId);
    }

    public static JSONObject getBoundsResponse(ArrayList<AccessibilityNodeInfo> nodes, String resourceId){
        //TODO need to cache nodes

        resourceId = NodeUtil.getTruncatedId(resourceId);
        Log.i(TAG, "returning bounds response");
        JSONObject nodeBounds = new JSONObject();
        try {
            nodeBounds.put(RESOURCE_ID_KEY, resourceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // find AccessibilityNodeInfo that corresponds to
        // node requested by server
        AccessibilityNodeInfo node=null;
        for(AccessibilityNodeInfo n : nodes){

            String nId = NodeUtil.getTruncatedId(n.getViewIdResourceName());
            if (nId.equals(resourceId)){
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

    /************'
     * Private support utils
     */
    private static void putResourceId(AccessibilityNodeInfo node, JSONObject jsonObject){
        String resourceId = node.getViewIdResourceName();
        try {
            if( resourceId != null){
                jsonObject.put(RESOURCE_ID_KEY,NodeUtil.getTruncatedId(resourceId));
            } else {
                //TODO need more elegant dealing with "<NO_???>"
                jsonObject.put(RESOURCE_ID_KEY,NO_RESOURCE_ID_VALUE);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void putLabel(AccessibilityNodeInfo node, JSONObject jsonObject){
        String label = NodeUtil.getLabel(node);
        try {
            if (label != null) {
                jsonObject.put(LABEL_KEY, label);
            } else {
                jsonObject.put(LABEL_KEY, NO_LABEL_VALUE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
