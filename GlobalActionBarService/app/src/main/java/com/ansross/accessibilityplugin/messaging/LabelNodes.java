package com.ansross.accessibilityplugin.messaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LabelNodes {
    // resourceId:Node
    HashMap<String,LabelNode> labelNodes;

    public LabelNodes(){
        labelNodes = new HashMap<>();
    }

    public LabelNodes(JSONObject nodesJson) {
        labelNodes = new HashMap<>();
        Iterator iter = nodesJson.keys();
        while (iter.hasNext()) {
            String id = iter.next().toString();

            try {
                labelNodes.put(id, new LabelNode((JSONObject) nodesJson.get(id)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void addNode(LabelNode node){
        labelNodes.put(node.id, node);
    }

    public JSONObject toJson(){
        JSONObject nodesJson = new JSONObject();
        for(String id : labelNodes.keySet()){
            try {
                nodesJson.put(id, labelNodes.get(id).toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return nodesJson;
    }
}
