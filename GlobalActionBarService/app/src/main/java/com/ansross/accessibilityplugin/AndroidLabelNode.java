package com.ansross.accessibilityplugin;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import com.ansross.accessibilityplugin.messaging.LabelContributorNode;
import com.ansross.accessibilityplugin.messaging.LabelNode;

import java.util.ArrayList;
import java.util.HashMap;

import static com.ansross.accessibilityplugin.messaging.MessagingNodesConstants.*;

public class AndroidLabelNode extends LabelNode {
    AccessibilityNodeInfo node;

    public AndroidLabelNode(AccessibilityNodeInfo nodeArg,
                     String idArg,
                     String labelArg,
                     ArrayList<LabelContributorNode> contributorNodesArg){
        node=nodeArg;
        id=idArg;
        label=labelArg;
        contributorNodes=contributorNodesArg;
        Rect androidBounds = new Rect();
        nodeArg.getBoundsInScreen(androidBounds);
        bounds = new HashMap<>();
        bounds.put(BOUNDS_LEFT_KEY,androidBounds.left);
        bounds.put(BOUNDS_TOP_KEY,androidBounds.top);
        bounds.put(BOUNDS_RIGHT_KEY, androidBounds.right);
        bounds.put(BOUNDS_BOTTOM_KEY,androidBounds.bottom);
    }
}
