package com.ansross.accessibilityplugin;

import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.ansross.accessibilityplugin.messaging.LabelContributorNode;
import com.ansross.accessibilityplugin.messaging.LabelNode;
import com.ansross.accessibilityplugin.messaging.LabelNodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import static com.ansross.accessibilityplugin.messaging.MessagingNodesConstants.*;

public class ResponseUtil {



    final static String TAG = "<ResponseUtil>";

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

        /*
            resourceId:
                label: String
                nodesCreatingLabel:
                    id:
                        attribute: <CONTDESC, LABELBY, TEXT>
                        label: String


         */
        LabelNodes nodes = new LabelNodes();
        //JSONObject nodes = new JSONObject();
        ArrayList<AccessibilityNodeInfo> accessibilityNodeInfos = new ArrayList<>();
        NodeUtil.getImportantForAccessibilityNodeInfo(root,accessibilityNodeInfos);

        for (AccessibilityNodeInfo nodeInfo : accessibilityNodeInfos){
            String resourceId = getResourceId(nodeInfo);
            ArrayList<LabelContributorNode> contributorNodes = new ArrayList<>();
            String label = ATFUtil.getSpeakableText(nodeInfo,contributorNodes);
            //TODO figure out
            LabelNode node = new LabelNode(nodeInfo,
                    resourceId,
                    label,
                    contributorNodes);

            nodes.addNode(node);

        }
        return nodes.toJson();
    }

    public static JSONObject getNodesForDisplayResponse(AccessibilityNodeInfo root){
        JSONObject nodesJson = new JSONObject();
        ArrayList<AccessibilityNodeInfo> accessibilityNodeInfos = new ArrayList<>();
        NodeUtil.getImportantForAccessibilityNodeInfo(root,accessibilityNodeInfos);
        for (AccessibilityNodeInfo nodeInfo : accessibilityNodeInfos){
            // <resourceId>:
            //          bounds:  left: int
            //                  top:  int
            //                  right: int
            //                  bottom: int
            //          label: String

            JSONObject singleNodeJson = new JSONObject();
            String id = getResourceId(nodeInfo);
            String label = getLabel(nodeInfo);

            JSONObject jsonBounds = new JSONObject();
            Rect bounds = new Rect();
            nodeInfo.getBoundsInScreen(bounds);
            try {
                jsonBounds.put(BOUNDS_LEFT_KEY, bounds.left);
                jsonBounds.put(BOUNDS_TOP_KEY, bounds.top);
                jsonBounds.put(BOUNDS_RIGHT_KEY, bounds.right);
                jsonBounds.put(BOUNDS_BOTTOM_KEY, bounds.bottom);

                singleNodeJson.put(BOUNDS_KEY, jsonBounds);
                singleNodeJson.put(LABEL_KEY, label);

                nodesJson.put(id, singleNodeJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return nodesJson;
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

    public static JSONObject getNavigationOrderResponse(ArrayList<AccessibilityNodeInfo> navOrderNodes){
        JSONObject response = new JSONObject();
        for(int i=0; i<navOrderNodes.size(); i++){
            try {
                response.put(getResourceId(navOrderNodes.get(i)),i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return response;

    }

    /************'
     * Private support utils
     */

    //TODO need more elegant dealing with "<NO_???>"
    private static String getResourceId(AccessibilityNodeInfo node){
        String resourceId = node.getViewIdResourceName();
        if (resourceId!=null){
            resourceId = NodeUtil.getTruncatedId(resourceId);
        } else{
            resourceId = NO_RESOURCE_ID_VALUE;
        }
        return  resourceId;
    }

    private static void putResourceId(AccessibilityNodeInfo node, JSONObject jsonObject){
        try {
            jsonObject.put(RESOURCE_ID_KEY,getResourceId(node));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String getLabel(AccessibilityNodeInfo node){
        String label = NodeUtil.getLabel(node);
        if (label == null) {
            label=NO_LABEL_VALUE;
        }
        return label;
    }

    private static void putLabel(AccessibilityNodeInfo node, JSONObject jsonObject){
        try {
                jsonObject.put(LABEL_KEY, getLabel(node));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
