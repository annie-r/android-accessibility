// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.android.globalactionbarservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class GlobalActionBarService extends AccessibilityService {
    WindowManager wm;
    int count;
    final Handler handler = new Handler();
    ArrayList<AccessibilityNodeInfo> nodes;

    final String TAG = "<AS_DEV_TOOL>";

    public final int GET_FOCUSED_ELEMENT_ID = 0;
    public final int GET_ACCESS_NODES_AND_LABELS = 1;
    public final int GET_BOUNDS_FOR_ELEMENT_ID = 2;

    FrameLayout mLayout;

    @Override
    protected void onServiceConnected() {
        // Create an overlay and display the action bar

        Log.i(TAG, "log test");

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, lp);

        //configureSwipeButton();
        startClient();
    }



    /*********************
     * Sockets and Networking with Android Studio
     */

    private void startClient(){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    String hostName = "127.0.0.1";
                    int portNumber = 7100; //device forwarded port

                    // Open Socket
                    try (
                            Socket kkSocket = new Socket(hostName, portNumber);
                            PrintWriter outToServer = new PrintWriter(kkSocket.getOutputStream(), true);
                            BufferedReader inFromServer = new BufferedReader(
                                    new InputStreamReader(kkSocket.getInputStream()));
                    ) {

                        String fromServer;

                        while ((fromServer = inFromServer.readLine()) != null) {
                            Log.i(TAG,"Server: " + fromServer);

                            if (fromServer.equals("Bye."))
                                break;

                            int serverCommand = Integer.parseInt(fromServer);

                            switch(serverCommand){
                                case GET_FOCUSED_ELEMENT_ID:
                                    AccessibilityNodeInfo focus = getRootInActiveWindow().findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                                    JSONObject nodeJson = new JSONObject();
                                    if(focus != null){
                                        if(focus.getViewIdResourceName() != null){
                                            nodeJson.put("resourceID",focus.getViewIdResourceName());
                                        } else {
                                            //TODO need more elegant dealing with "<NO_???>"
                                            nodeJson.put("resourceID","<NO_ID>");
                                        }

                                        String label = getLabel(focus);
                                        if (label!= null){
                                            nodeJson.put("label",label);
                                        } else {
                                            nodeJson.put("label","<NO_LABEL>");
                                        }
                                    } else {
                                        nodeJson.put("resourceID","<NO_NODE>");
                                    }
                                    outToServer.println(nodeJson.toString());
                                    break;
                                case GET_ACCESS_NODES_AND_LABELS:
                                    JSONObject nodesAndLabel = getNodesAndLabels();
                                    outToServer.println(nodesAndLabel.toString());
                                    break;
                                case GET_BOUNDS_FOR_ELEMENT_ID:
                                    JSONObject readyResponse = new JSONObject();
                                    readyResponse.put("RESPONSE","READY");
                                    outToServer.println(readyResponse.toString());
                                    //get resouce ID
                                    fromServer = inFromServer.readLine();
                                    Log.i(TAG, "resouceID: "+fromServer);
                                    JSONObject nodeBounds = new JSONObject();
                                    nodeBounds.put("resourceID",fromServer);

                                    // find AccessibilityNodeInfo that cooresponds to
                                    // node requested by server
                                    AccessibilityNodeInfo node=null;
                                    for(AccessibilityNodeInfo n : nodes){

                                        String nId = getTruncatedId(n.getViewIdResourceName());
                                        if (nId.equals(fromServer)){
                                            node = n;
                                            break;
                                        }
                                    }
                                    if (node!=null){
                                        Rect bounds = new Rect();
                                        node.getBoundsInScreen(bounds);
                                        nodeBounds.put("boundsLeft",bounds.left);
                                        nodeBounds.put("boundsTop",bounds.top);
                                        nodeBounds.put("boundsRight",bounds.right);
                                        nodeBounds.put("boundsBottom",bounds.bottom);
                                    }
                                    outToServer.println(nodeBounds.toString());


                            }
                        }
                    } catch (UnknownHostException e) {
                        Log.e(TAG,"Don't know about host " + hostName);
                        System.exit(1);
                    } catch (IOException e) {
                        Log.e(TAG,"Couldn't get I/O for the connection to " +
                                hostName);
                        System.exit(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private String getTruncatedId(String resourceID){
        if (resourceID.contains("/")){
            resourceID = resourceID.split("/")[1];
        }
        return  resourceID;
    }

    private String getLabel(AccessibilityNodeInfo focus) {
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
        if (label==null){
            return "<NO_LABEL>";
        }
        return label;
    }



    private JSONObject getNodesAndLabels(){
        JSONObject nodes = new JSONObject();
        AccessibilityNodeInfo root = getRootInActiveWindow();
        iterateHierarchy(root,nodes);
        return nodes;
    }

    private void iterateHierarchy(AccessibilityNodeInfo root, JSONObject nodes){
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
                iterateHierarchy(childNode, nodes);
            }
        }
        try {

            if(root.isImportantForAccessibility()) {
                String id = root.getViewIdResourceName();

                if (id!= null) {
                    if(id.contains("/")){
                        // TODO manage how to crop out the "com.example:id/<RESOURCEID>"
                        id = id.split("/")[1];
                    }
                    this.nodes.add(root);
                    nodes.put(id, getLabel(root));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /***************************
     *
     * @param context
     * @param bounds
     * @return
     */

    public View addOverlay(Context context, Rect bounds) {
        TextView image_view = new TextView(context);
        image_view.setText("#"+count);
        image_view.setBackgroundColor(Color.argb(200, 0, 180, 0));
        WindowManager.LayoutParams temp_params = new WindowManager.LayoutParams(
                bounds.width(),
                bounds.height(),
                bounds.left,
                bounds.top,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,//WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        temp_params.gravity = Gravity.TOP | Gravity.LEFT;
        wm.addView(image_view, temp_params);
       //Utility.list_overlays.add(image_view);
        return image_view;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "log test");
        nodes = new ArrayList<>();
        count = 0;
        //Utility.statusBarHeight = Utility.getStatusBarHeight(this);

    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
       /* if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        AccessibilityNodeInfo eventNode = event.getSource();//getRootInActiveWindow();//event.getSource();
        AccessibilityNodeInfo root = getRootInActiveWindow();
        AccessibilityNodeInfo child0 = root.getChild(0);
        AccessibilityNodeInfo child1 = child0.getChild(0);
        AccessibilityNodeInfo child = child1.getChild(0);
*/
        AccessibilityNodeInfo focus = getRootInActiveWindow().findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        if(focus!=null ) {
            focus.refresh();
        }
        //Parcel parcel;
        //focus.writeToParcel(parcel);
        //nodes.add(focus);
        /*if (count < 2) {
            swipe();
            count += 1;
        } else {
            Log.i(TAG, "complete");
        }*//*

        AccessibilityNodeInfo childNext = focus.getTraversalAfter();
        AccessibilityNodeInfo childBefore = focus.getTraversalBefore();
        if (focus != null){
            Rect bounds = new Rect();
            count = 0;
            focus.getBoundsInScreen(bounds);

            //Rect test = new Rect(0, 0, 20, 20);
            addOverlay(this, bounds);//test);
            for (int i=0; i<2;i++){
                count +=1;
                swipe();

               focus = getRootInActiveWindow().findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
               nodes.add(focus);
            }
            Log.i(TAG,"over");

            int g =0;
        }
        }*/

    }


    private void swipe(){
        Path swipePath = new Path();
        swipePath.moveTo(100, 1000);
        swipePath.lineTo(1000, 1000);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
        dispatchGesture(gestureBuilder.build(), null, null);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return true;
    }


    @Override
    public void onInterrupt() {
    }

/** GRAVEYARD
    private void configureSwipeButton() {
        // playing with ports
        //https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
        // adb port forwarding

        Button swipeButton = (Button) mLayout.findViewById(R.id.swipe);
        swipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Path swipePath = new Path();
                swipePath.moveTo(1000, 1000);
                swipePath.lineTo(100, 1000);
                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
                dispatchGesture(gestureBuilder.build(), null, null);
            }
        });
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.i("<TEST>", "event");
        AccessibilityNodeInfo eventNode = event.getSource();
        if (node != null || eventNode == null){
            Log.i("<TEST>", "null node");
        } else {
            LinearLayout overlay = new LinearLayout(this);
            overlay.setOrientation(LinearLayout.VERTICAL);

            WindowManager.LayoutParams temp_params = new WindowManager.LayoutParams(
                    200,//rect.width(),
                    200,//rect.height(),
                    20,//rect.left,
                    1000,//rect.top - statusBarHeight,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.addView(overlay, temp_params);


            node = eventNode;
            LinearLayout ll = (LinearLayout)mLayout.findViewById(R.id.base);

            Button btn = new Button(this);
            btn.setText("Manual Add");




            temp_params.gravity = Gravity.TOP | Gravity.LEFT;
            btn.setLayoutParams(temp_params);
            overlay.addView(btn);
        }

        // Log.i("<TEST>", node.getViewIdResourceName());

    }

    @Override
    public void onInterrupt() {

    }*/





}
