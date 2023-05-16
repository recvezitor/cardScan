package com.dimas.img_to_text;

import java.util.regex.Pattern;

public class Const {
//    public static final String ROOT_PATH = "C:\\work\\img_to_text\\";
    public static final String ROOT_PATH = "";
    public static final String FEED_PATH = "feed\\";
    public static final String TEMPLATE_PATH = "templates\\";
    public static final String TARGET_PATH = "src\\main\\resources\\target\\";
    public static final String NOTFOUND = "??";
    public static final Pattern CARD_PATTERN = Pattern.compile("([\\dTJQKA]{1,2}[sdch])");


    public static final int width = 55;
    public static final int height = 78;
    public static final int xOffset = 146;
    public static final int yOffset = 590;
    public static final int stepWidth = 72;
    public static final String PNG_EXT = ".png";

}
