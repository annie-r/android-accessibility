package com.ansross.accessibilityplugin;

import android.view.accessibility.AccessibilityNodeInfo;

//references
//https://github.com/rayfok/MobileAccessRepair/blob/master/detect/check_utils.py
//Reference at https://github.com/google/Accessibility-Test-Framework-for-Android/blob/master/src/main/java/com/google/android/apps/common/testing/accessibility/framework/ViewHierarchyElementUtils.java
public class ATFUtil {

    static String getSpeakableText(AccessibilityNodeInfo node){
       String speakableText = "";
       if(!node.isImportantForAccessibility()){
           return speakableText;
       }

       //TODO inherited label

        if (!node.getContentDescription().toString().isEmpty()){
            return node.getContentDescription().toString();
        }

        if(!node.getText().toString().isEmpty()){
            speakableText += node.getText().toString();
        }
        // TODO think about how to deal with appended labels for e.g., Checkboxes or switches

        for (int i = 0; i<node.getChildCount(); i++){
            AccessibilityNodeInfo childInfo = node.getChild(i);
            if (childInfo.isVisibleToUser() && !isActionableForAccessibility(childInfo)){
                String childText = getSpeakableText(childInfo);
                if(!childText.isEmpty()){
                    speakableText+= childText;
                }
            }
        }

        return speakableText;
    }

    static boolean shouldFocusNode(AccessibilityNodeInfo node){
        if(!node.isVisibleToUser()){
            return false;
        }
        if (isAccessibilityFocusable(node)){
            if(!hasImportantDescendant(node)){
                return true;
            }
            
            if (isSpeakingElement(node)){
                return true;
            }
            
            return false;
        }
        boolean nodeHasText = hasText(node);
        boolean isImportant = node.isImportantForAccessibility();
        boolean hasFocusAnces = hasFocusableAncestor(node);
        return (hasText(node) && node.isImportantForAccessibility() && !hasFocusableAncestor(node));
    }

    static private boolean hasFocusableAncestor(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo focusableParent = getImportantForAccessibilityAncestor(node);
        if(focusableParent == null){
            return false;
        }
        if(isAccessibilityFocusable(focusableParent)){
            return true;
        }
        return hasFocusableAncestor(focusableParent);
    }

    static private AccessibilityNodeInfo getImportantForAccessibilityAncestor(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = node.getParent();
        while(parent!=null && !parent.isImportantForAccessibility()){
            parent = parent.getParent();
        }
        return parent;

    }

    static private boolean hasText(AccessibilityNodeInfo node) {
        CharSequence nodeText = node.getText();
        CharSequence nodeCD = node.getContentDescription();
        return ((node.getText() != null && !node.getText().equals(""))
                || (node.getContentDescription()!= null && !node.getContentDescription().equals(""))
        );
    }

    static private boolean isSpeakingElement(AccessibilityNodeInfo node) {
        boolean hasNonfocusableSpeakingChildren = hasNonfocusableSpeakingChildren(node);
        return (hasText(node) || node.isCheckable() || hasNonfocusableSpeakingChildren);
    }

    static private boolean hasNonfocusableSpeakingChildren(AccessibilityNodeInfo node) {
        for(int i = 0; i<node.getChildCount(); i++){
            AccessibilityNodeInfo child = node.getChild(i);
            if(!child.isVisibleToUser() || isAccessibilityFocusable(child)){
                continue;
            }
            if (child.isImportantForAccessibility() && (hasText(child) || child.isCheckable())){
                return true;
            }
            if (hasNonfocusableSpeakingChildren(child)){
                return true;
            }
        }
        return false;
    }

    static private boolean hasImportantDescendant(AccessibilityNodeInfo node) {
        for(int i =0; i<node.getChildCount(); i++){
            AccessibilityNodeInfo child = node.getChild(i);
            if(child.isImportantForAccessibility()){
                return true;
            }
            if(child.getChildCount()>0){
                if(hasImportantDescendant(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    static private boolean isActionableForAccessibility(AccessibilityNodeInfo node) {
        return node.isClickable() || node.isLongClickable() || node.isFocusable();
    }

    static private boolean isAccessibilityFocusable(AccessibilityNodeInfo node){
        if(!node.isVisibleToUser()){
            return false;
        }
        if(!node.isImportantForAccessibility()){
            return false;
        }
        if(isActionableForAccessibility(node)){
            return true;
        }

        return (isChildOfScrollableContainer(node) && isSpeakingElement(node));
    }

    static private boolean isChildOfScrollableContainer(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = getImportantForAccessibilityAncestor(node);
        if(parent==null){
            return false;
        }

        if(parent.isScrollable()){
            return true;
        }

        if(parent.getClassName().equals("android.widget.Spinner")){
            return false;
        }

        return (parent.getClassName().equals("android.widget.AdapterView") ||
                parent.getClassName().equals("android.widget.ScrollView") ||
                parent.getClassName().equals("android.widget.HorizontalScrollView"));
    }

    static boolean isAccessibilityFocusableAll(AccessibilityNodeInfo node){
        return node.isImportantForAccessibility() && isActionableForAccessibility(node);
    }

}
