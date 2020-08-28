package com.ansross.accessibilityplugin;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

public class TouchTargetSizeUtil {
    final static String TAG = "TARGETSIZE";

    public static boolean shouldTestForSize(AccessibilityNodeInfo node){
        if (!(node.isClickable() || node.isLongClickable())){
            return false;
        }
        return true;
    }

    // based on TouchTargetSizeCheck.java in ATF

    public static boolean testSize(AccessibilityNodeInfo node){
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        //TODO dynamic required size
        Point requiredSize = new Point(48,48);
        float density = AccessibilityPluginService.SCREEN_DPI/160;
        int actualHeight = Math.round(bounds.height()/density);
        int actualWidth = Math.round(bounds.width()/density);
        Log.i(TAG,"Height: "+actualHeight+". Width: "+actualWidth);

        if(!meetsRequiredSize(bounds,requiredSize,density)){
            // Before we know a view fails this check, we must check if another View may be handling
            // touches on its behalf. One mechanism for this is a TouchDelegate.
            boolean hasDelegate = false;
            // There are two approaches to detecting such a delegate.  One (on Android Q+) allows us
            // access to the hit-Rect.  Since this is the most precise signal, we try to use this first.
            Rect largestDelegateHitRect = null;
            if(hasTouchDelegateWithHitRects(node)){
                hasDelegate = true;
                if(hasTouchDelegateOfRequireSize(node, requiredSize, density)) {
                    // Emit no result if a delegate's hit-Rect is above the required size
                    //TODO assure htis is correct interpretation
                    return true;
                }
                // If no associated hit-Rect is of the required size, reference the largest one for
                // inclusion in the result message.
                largestDelegateHitRect = getLargestTouchDelegateHitRect(node);
            } else {
                // Without hit-Rects, another approach is to check (View) ancestors for the presence of
                // any TouchDelegate, which indicates that the element may have its hit-Rect adjusted,
                // but does not tell us what its size is.
                hasDelegate = hasAncestorWithTouchDelegate(node);
            }
            // Another approach is to have the parent handle touches for smaller child views, such as a
            // android.widget.Switch, which retains its clickable state for a "handle drag" effect. In
            // these cases, the parent must perform the same action as the child, which is beyond the
            // scope of this test.  We append this important exception message to the result by setting
            // KEY_HAS_CLICKABLE_ANCESTOR within the result metadata.
            // TODO not out of scope here
            // TODO will take parameters to decide min allowable size at some point
            boolean hasClickableAncestor = hasQualifyingClickableAncestor(node, requiredSize);
            // When evaluating a View-based hierarchy, we can check if the visible size of the view is
            // less than the drawing (nonclipped) size, which indicates an ancestor may scroll,
            // expand/collapse, or otherwise constrain the size of the clickable item.
            //TODO get clipped information
            //boolean isClippedByAncestor = hasQualifyingClippingAncestor(node, requiredSize, density);

            // In each of these cases, with the exception of when we have precise hit-Rect coordinates,
            // we cannot determine how exactly click actions are being handled by the underlying
            // application, so to avoid false positives, we will demote ERROR to WARNING.
            // We must also detect the case where an item is indicated as a small target because it
            // appears along the scrollable edge of a scrolling container.  In this case, we cannot
            // determine the native nonclipped bounds of the view, so we demote to NOT_RUN.
           /*AccessibilityCheckResultType resultType =
                    ((hasDelegate && (largestDelegateHitRect == null))
                            || hasClickableAncestor
                            || isClippedByAncestor)
                            ? AccessibilityCheckResultType.WARNING
                            : AccessibilityCheckResultType.ERROR;*/

           //TODO scrollable edge
            //boolean isAtScrollableEdge = node.isAgainstScrollableEdge();
            //resultType = isAtScrollableEdge ? AccessibilityCheckResultType.NOT_RUN : resultType;
            //line 258

            if(hasDelegate){
                if(largestDelegateHitRect != null){
                    //information for result
                    //Has touch delegate with hit rect
                    // hit rect width
                    //hit rect height
                } else {
                    //has touch delegate
                }
            }
            if(hasClickableAncestor){
                // result: has clickable ancestor
            }
            //TODO if(isAtScrollableEdge){
                // is against scrollable edge
            //}
            // TODO is clipped
            //TODO customized target size

            if((actualHeight < requiredSize.y) && (actualWidth < requiredSize.x)){
                // to short and too narrow
                Log.i(TAG, "Neither wide enough nor tall enough");
                return false;
            } else if (actualHeight < requiredSize.y){
                //too short only
                Log.i(TAG, "Not tall enough only");
                return false;
            } else if (actualWidth < requiredSize.x){
                //too narrow only
                Log.i(TAG, "Not wide enough only");
                return false;
            } else {
                return true;
            }

        }
        //TODO IMPLEMENT
        Log.i(TAG, "Large Enough");
        return true;

    }



    private static boolean hasQualifyingClickableAncestor(AccessibilityNodeInfo node, Point requiredSize) {
        boolean targetIsClickable = node.isClickable();
        boolean targetIsLongClickable = node.isLongClickable();
        AccessibilityNodeInfo evalNode = node.getParent();

        while(evalNode!= null){
            if((evalNode.isClickable() && targetIsClickable)
            || (evalNode.isLongClickable() && targetIsLongClickable)){
                Rect bounds = new Rect();
                evalNode.getBoundsInScreen(bounds);
                if((!node.getClassName().equals("android.widget.AbsListView"))
                        && (bounds.width() >= requiredSize.x)
                && (bounds.height() >= requiredSize.y)){
                    return true;
                }
            }
            evalNode = evalNode.getParent();
        }
        return false;
    }

    private static boolean hasAncestorWithTouchDelegate(AccessibilityNodeInfo node) {
        for(AccessibilityNodeInfo evalNode = node.getParent(); evalNode!=null; evalNode = evalNode.getParent()){
            if(evalNode.getTouchDelegateInfo() != null){
                return true;
            }
        }
        return false;
    }

    private static Rect getLargestTouchDelegateHitRect(AccessibilityNodeInfo node) {
        int largestArea = -1;
        Rect largestHitRect = null;
        AccessibilityNodeInfo.TouchDelegateInfo touchDelegateInfo = node.getTouchDelegateInfo();
        for (int i = 0; i<touchDelegateInfo.getRegionCount(); i++){
            Region region = touchDelegateInfo.getRegionAt(i);
            Rect regionBounds = region.getBounds();
            int area = regionBounds.width() * regionBounds.height();
            if (area > largestArea){
                largestArea = area;
                largestHitRect = regionBounds;
            }
        }
        return largestHitRect;
    }

    private static boolean hasTouchDelegateWithHitRects(AccessibilityNodeInfo node) {
        return node.getTouchDelegateInfo()!=null &&
                node.getTouchDelegateInfo().getRegionCount()>0;
    }

    private static boolean hasTouchDelegateOfRequireSize(AccessibilityNodeInfo node, Point requiredSize, float density) {
       //TODO TEST WITH DELEGATE
        AccessibilityNodeInfo.TouchDelegateInfo touchDelegate = node.getTouchDelegateInfo();
        if (touchDelegate != null) {
            Log.i(TAG, "touch delegates for " + node.getViewIdResourceName());
            for (int i = 0; i < touchDelegate.getRegionCount(); i++) {
                Region region = touchDelegate.getRegionAt(i);
                if (meetsRequiredSize(region.getBounds(), requiredSize, density)) {
                    return true;
                }
            }
        }
       return false;
    }

    public static boolean meetsRequiredSize(Rect boundsInPx, Point requiredSizeInDp, float density){
        return (Math.round(boundsInPx.width() / density) >= requiredSizeInDp.x)
                && (Math.round(boundsInPx.height() / density) >= requiredSizeInDp.y);
    }
}
