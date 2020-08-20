///// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
///// VERSION NUMBER 1.0 8/20!!!!!!!!!!!!!!!!

package com.ansross.accessibilityplugin.messaging;

import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import static com.ansross.accessibilityplugin.messaging.MessagingNodesConstants.*;

public class LabelNode {
    /**
     * RESOURCE_ID : <String_id>
     * LABEL_KEY : String_label
     * LABEL_ATTRIBUTE_KEY : Enum_String_Attribute
     * CONTRIBUTOR_NODES_KEY:
     *          id1 : contributor_node_1
     *          ...
     */

    AccessibilityNodeInfo node;
    public String id;
    //should have self in contributor nodes
    public ArrayList<LabelContributorNode> contributorNodes;
    public String label;

    public LabelNode(AccessibilityNodeInfo nodeArg,
                     String idArg,
                     String labelArg,
                     ArrayList<LabelContributorNode> contributorNodesArg){
        node=nodeArg;
        id=idArg;
        label=labelArg;
        contributorNodes=contributorNodesArg;
    }
    public LabelNode (JSONObject nodeJson){
        // TODO is it problematic that node is null?
        node = null;
        //TODO deal with incorrect JSON
        id = null;
        label = null;

        try {
            id = nodeJson.getString(RESOURCE_ID_KEY);
            label = nodeJson.getString(LABEL_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        contributorNodes = new ArrayList<>();

        try {
            JSONObject contributorNodesJson = nodeJson.getJSONObject(CONTRIBUTING_NODES_KEY);

            Iterator iter = contributorNodesJson.keys();
            while(iter.hasNext()){
                String id = iter.next().toString();
                JSONObject contributorNodeJson = contributorNodesJson.getJSONObject(id);
                contributorNodes.add(new LabelContributorNode(contributorNodeJson));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public JSONObject toJSON(){

        JSONObject nodeJson = new JSONObject();
        try {
            nodeJson.put(RESOURCE_ID_KEY, id);
            nodeJson.put(LABEL_KEY, label);
            JSONObject contributingNodesJson = new JSONObject();
            for(LabelContributorNode contributorNode: contributorNodes){
                contributingNodesJson.put(contributorNode.id,contributorNode.toJSON());
            }
            nodeJson.put(CONTRIBUTING_NODES_KEY,contributingNodesJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
            return nodeJson;
    }




}
