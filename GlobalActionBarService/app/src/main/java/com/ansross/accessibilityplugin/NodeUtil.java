package com.ansross.accessibilityplugin;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NodeUtil {
    private final static String TAG = "<NODE_UTIL>";


    public static String getTruncatedId(String resourceID){
        if (resourceID.contains("/")){
            resourceID = resourceID.split("/")[1];
        }
        return  resourceID;
    }

    // TODO actually follow labeling logic
    public static String getLabel(AccessibilityNodeInfo focus) {
        String label = null;
        if (focus.isImportantForAccessibility()){
            CharSequence text = focus.getText();
            if(text!=null) {
                label = text.toString();
            } else {
                CharSequence contDesc = focus.getContentDescription();
                if(contDesc != null) {
                    label = focus.getContentDescription().toString();
                }
            }
        }
        return label;
    }

    public static void getImportantForAccessibilityNodeInfo(AccessibilityNodeInfo root, List<AccessibilityNodeInfo> accessibilityNodeInfos){
        if (root==null){
            return;
        }
        for(int child = 0; child<root.getChildCount(); child++){
            AccessibilityNodeInfo childNode = root.getChild(child);
            if (childNode != null) {
                String id = childNode.getViewIdResourceName();
                if (id != null) {
                    Log.i(TAG, childNode.getViewIdResourceName());
                } else {
                    Log.i(TAG, "NULL ID");
                }
                getImportantForAccessibilityNodeInfo(childNode, accessibilityNodeInfos);
            }
        }

        //if(root.isImportantForAccessibility()) {
        if(ATFUtil.shouldFocusNode(root)){
            String id = root.getViewIdResourceName();

            if (id!= null) {
                if(id.contains("/")){
                    // TODO manage how to crop out the "com.example:id/<RESOURCEID>"
                    id = id.split("/")[1];
                }
                accessibilityNodeInfos.add(root);
            }
        }

    }

}
