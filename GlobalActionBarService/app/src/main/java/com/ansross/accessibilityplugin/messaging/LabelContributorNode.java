package com.ansross.accessibilityplugin.messaging;

import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ansross.accessibilityplugin.messaging.MessagingNodesConstants.*;
public class LabelContributorNode {
    public final static String CONTENT_DESC_VALUE_ATTRIBUTE = "CONTENT_DESC_VALUE";
    public final static String TEXT_VALUE_ATTRIBUTE = "TEXT_VALUE";
    public final static String LABELFOR_VALUE_ATTRIBUTE = "LABELFOR_VALUE";

    public AccessibilityNodeInfo node;
    public String id;
    public String attribute;
    public String label;

    public LabelContributorNode(AccessibilityNodeInfo nodeArg,
                                String idArg,
                                String attributeArg,
                                String labelArg){
        node=nodeArg;
        id=idArg;
        attribute=attributeArg;
        label=labelArg;
    }

    public LabelContributorNode(JSONObject contributorNodeJson){
        // TODO : when have JSON, don't have full node
        node=null;
        try {
            id=contributorNodeJson.getString(RESOURCE_ID_KEY);
            attribute=contributorNodeJson.getString(LABEL_ATTRIBUTE_KEY);
            label=contributorNodeJson.getString(LABEL_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONObject toJSON(){
        JSONObject contributorNodeJSON = new JSONObject();
        /*
        ID_KEY : <String ID>
        ATTRIBUTE_KEY : <Static String Attribute>
        LABEL_KEY : <String Label>
         */
        try {
            contributorNodeJSON.put(RESOURCE_ID_KEY, id);
            contributorNodeJSON.put(LABEL_ATTRIBUTE_KEY, attribute);
            contributorNodeJSON.put(LABEL_KEY, label);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contributorNodeJSON;

    }

}
