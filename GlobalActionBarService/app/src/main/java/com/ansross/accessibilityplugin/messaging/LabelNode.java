package com.ansross.accessibilityplugin.messaging;
import static com.ansross.accessibilityplugin.messaging.MessagingNodesConstants.*;
///// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
///// VERSION NUMBER 1.5 8/21

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LabelNode {
    /**
     * RESOURCE_ID : <String_id>
     * LABEL_KEY : String_label
     * LABEL_ATTRIBUTE_KEY : Enum_String_Attribute
     * BOUNDS_KEY :
     *      BOUNDS_LEFT_KEY : Int_Bound
     *      BOUNDS_TOP_KEY : Int_Bound
     *      BOUNDS_RIGHT_KEY : Int_Bound
     *      BOUNDS_BOTTOM_KEY : Int_Bound
     *
     * CONTRIBUTOR_NODES_KEY:
     *          id1 : contributor_node_1
     *          ...
     */

    public String id;
    //should have self in contributor nodes
    public ArrayList<LabelContributorNode> contributorNodes;
    public String label;
    // using HashMap because had trouble finding
    // rectangle/rect class shared between android implementation
    // and plugin implementation
    public HashMap<String, Integer> bounds;

    public LabelNode(){
        id = null;
        label = null;
        bounds = null;
        bounds = new HashMap<>();
    }


    public LabelNode (JSONObject nodeJson){
        //TODO deal with incorrect JSON
        id = null;
        label = null;
        bounds = null;
        bounds = new HashMap<>();
        try {
            id = nodeJson.getString(RESOURCE_ID_KEY);
            label = nodeJson.getString(LABEL_KEY);

            JSONObject boundsJson = nodeJson.getJSONObject(BOUNDS_KEY);
            bounds.put(BOUNDS_LEFT_KEY,boundsJson.getInt(BOUNDS_LEFT_KEY));
            bounds.put(BOUNDS_TOP_KEY,boundsJson.getInt(BOUNDS_TOP_KEY));
            bounds.put(BOUNDS_RIGHT_KEY, boundsJson.getInt(BOUNDS_RIGHT_KEY));
            bounds.put(BOUNDS_BOTTOM_KEY, boundsJson.getInt(BOUNDS_BOTTOM_KEY));
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

            JSONObject boundsJson = new JSONObject();
            boundsJson.put(BOUNDS_LEFT_KEY, bounds.get(BOUNDS_LEFT_KEY));
            boundsJson.put(BOUNDS_TOP_KEY, bounds.get(BOUNDS_TOP_KEY));
            boundsJson.put(BOUNDS_RIGHT_KEY, bounds.get(BOUNDS_RIGHT_KEY));
            boundsJson.put(BOUNDS_BOTTOM_KEY, bounds.get(BOUNDS_BOTTOM_KEY));

            nodeJson.put(BOUNDS_KEY,boundsJson);


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
