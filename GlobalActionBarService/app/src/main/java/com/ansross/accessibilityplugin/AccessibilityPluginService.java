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

package com.ansross.accessibilityplugin;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.SystemClock;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class AccessibilityPluginService extends AccessibilityService {
    WindowManager wm;
    int count;
    ArrayList<AccessibilityNodeInfo> nodes;


    ArrayList<AccessibilityNodeInfo> navigationOrderNodes;
    String startNodeId;
    boolean parsingTraversalOrder = false;


    final String TAG = "<AS_DEV_TOOL>";




    public final int REQUEST_VOL_DOWN = 100;

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


        Button swipeButton = (Button) mLayout.findViewById(R.id.swipe);
        swipeButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                getNavigationOrder();
           }
        });

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
                                case ResponseUtil.GET_FOCUSED_ELEMENT_ID:
                                    AccessibilityNodeInfo focus = getRootInActiveWindow().findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                                    outToServer.println(ResponseUtil.getFocusedElementResponse(focus).toString());
                                    break;
                                case ResponseUtil.GET_ACCESS_NODES_AND_LABELS:
                                    JSONObject nodesAndLabel = ResponseUtil.getNodesAndLabelsResponse(getRootInActiveWindow());
                                    outToServer.println(nodesAndLabel.toString());
                                    break;
                                case ResponseUtil.GET_BOUNDS_FOR_ELEMENT_ID:
                                    // ready to receive resource ID from server
                                    outToServer.println(ResponseUtil.getReadyResponse().toString());
                                    //get resource ID
                                    String resourceId = inFromServer.readLine();
                                    Log.i(TAG, "resourceID: "+resourceId);
                                    // get bounds for node with resource ID
                                    outToServer.println(ResponseUtil.
                                            getBoundsResponse(getRootInActiveWindow(),resourceId).
                                            toString());
                                    break;
                                case ResponseUtil.GET_NAV_ORDER:
                                    getNavigationOrder();
                                    outToServer.println(ResponseUtil.
                                            getNavigationOrderResponse(navigationOrderNodes).
                                            toString());
                                    break;


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




    public void getRecording(){
        /*MediaProjectionManager mpm = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);
        Intent recordIntent =  mpm.createScreenCaptureIntent();
        AudioRecord recorder = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(32000)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build())
                .setBufferSizeInBytes(2*2500)
                .build();
        AudioPlaybackCaptureConfiguration config = AudioPlaybackCaptureConfiguration.Builder.build();*/
        /*MediaProjection mediaProjection;
        // Retrieve a audio capable projection from the MediaProjectionManager
        AudioPlaybackCaptureConfiguration config =
                new AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .build();
        AudioRecord record = new AudioRecord.Builder()
                .setAudioPlaybackCaptureConfig(config)
                .build();
*/



    }

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


    public void getNavigationOrder(){
        parsingTraversalOrder = true;
        navigationOrderNodes = new ArrayList<>();
        startNodeId = null;
        for (int i =0; i<20; i++){
            if(!parsingTraversalOrder){
                break;
            }
            swipe();
            SystemClock.sleep(3000);
        }
        Log.i(TAG, "done swiping");
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "acces event "+event.toString());
        if(parsingTraversalOrder) {
            AccessibilityNodeInfo focus = getRootInActiveWindow().findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
            if (focus != null) {
                focus.refresh();
                navigationOrderNodes.add(focus);
                if (startNodeId == null){
                    startNodeId = focus.getViewIdResourceName();
                } else if (focus.getViewIdResourceName()!=null &&
                        focus.getViewIdResourceName().equals(startNodeId)){
                    parsingTraversalOrder = false;
                    return;
                }

            }
        }
    }


    private void swipe(){
        Path swipePath = new Path();
        swipePath.moveTo(100, 1000);
        swipePath.lineTo(1000, 1000);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
        dispatchGesture(gestureBuilder.build(), null, null);
        Log.i(TAG,"swiped");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return true;
    }


    @Override
    public void onInterrupt() {
    }


/** GRAVEYARD
    private void volumeDownPress(){

        Thread btnThread = new Thread(){
            public void run(){
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
            }
        };
        btnThread.start();



    }



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
