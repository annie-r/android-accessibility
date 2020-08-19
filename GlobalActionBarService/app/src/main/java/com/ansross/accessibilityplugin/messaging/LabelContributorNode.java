package com.ansross.accessibilityplugin.messaging;

import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ansross.accessibilityplugin.messaging.MessagingNodesConstants.LABEL_ATTRIBUTE_KEY;
import static com.ansross.accessibilityplugin.messaging.MessagingNodesConstants.LABEL_KEY;
import static com.ansross.accessibilityplugin.messaging.MessagingNodesConstants.RESOURCE_ID_KEY;

public class LabelContributorNode {
    public final static String CONTENT_DESC_VALUE_ATTRIBUTE = "CONTENT_DESC_VALUE";
    public final static String TEXT_VALUE_ATTRIBUTE = "TEXT_VALUE";
    public final static String LABELFOR_VALUE_ATTRIBUTE = "LABELFOR_VALUE";

    public final AccessibilityNodeInfo node;
    public final String id;
    public final String attribute;
    public final String label;

    public LabelContributorNode(AccessibilityNodeInfo nodeArg,
                                String idArg,
                                String attributeArg,
                                String labelArg){
        node=nodeArg;
        id=idArg;
        attribute=attributeArg;
        label=labelArg;

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

    public static LabelContributorNode fromJSON(JSONObject contributorNodeJSON){
        // TODO : when have JSON, don't have full node
        LabelContributorNode contributorNode = null;
        try {
            contributorNode = new LabelContributorNode(
                    null,
                    contributorNodeJSON.getString(RESOURCE_ID_KEY),
                    contributorNodeJSON.getString(LABEL_ATTRIBUTE_KEY),
                    contributorNodeJSON.getString(LABEL_ATTRIBUTE_KEY)
                    );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contributorNode;
    }

}
