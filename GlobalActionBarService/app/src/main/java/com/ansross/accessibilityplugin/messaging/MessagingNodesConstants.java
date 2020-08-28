package com.ansross.accessibilityplugin.messaging;

////////////////////////////////////////////////
////////// VERSION 1.0 8/28

public class MessagingNodesConstants {
    /* MESSAGE CODES */
    public final static int GET_FOCUSED_ELEMENT_ID = 0;
    public final static int GET_ACCESS_NODES_AND_LABELS = 1;
    public final static int GET_BOUNDS_FOR_ELEMENT_ID = 2;
    public final static int GET_NODE_FOR_DISPLAY = 3;
    public final static int GET_NAV_ORDER = 4;

    /* NODE JSON KEYS*/
    public final static String RESOURCE_ID_KEY = "RESOURCE_ID_KEY";
    public final static String SCREEN_DPI_KEY = "SCREEN_DPI_KEY";
    public final static String READY_KEY = "READY_KEY";
    public final static String LABEL_KEY = "LABEL_KEY";
    public final static String CONTRIBUTING_NODES_KEY = "CONTRIBUTING_NODES_KEY";
    public final static String LABEL_ATTRIBUTE_KEY = "LABEL_ATTRIBUTE_KEY";

    public final static String BOUNDS_KEY="BOUNDS_KEY";
    public final static String BOUNDS_LEFT_KEY = "BOUNDS_LEFT_KEY";
    public final static String BOUNDS_TOP_KEY = "BOUNDS_TOP_KEY";
    public final static String BOUNDS_RIGHT_KEY = "BOUNDS_RIGHT_KEY";
    public final static String BOUNDS_BOTTOM_KEY = "BOUNDS_BOTTOM_KEY";

    /* NODE JSON VALUES */
    public final static String NO_RESOURCE_ID_VALUE = "<NO_ID>";
    public final static String INVALID_NODE_VALUE = "<NO_NODE>";
    public final static String NO_LABEL_VALUE = "<NO_LABEL>";
}
