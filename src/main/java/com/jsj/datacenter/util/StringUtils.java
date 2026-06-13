package com.jsj.datacenter.util;

public class StringUtils {
    public static String alignLeft(String text, int totalWidth) {
        if (text.length() >= totalWidth) {
            return text;
        }
        return text + String.format("%" + (totalWidth - text.length()) + "s", "");
    }

    public static String alignRight(String text, int totalWidth) {
        if (text.length() >= totalWidth) {
            return text;
        }
        return String.format("%" + (totalWidth - text.length()) + "s", "") + text;
    }

    public static String alignCenter(String text, int totalWidth) {
        if (text.length() >= totalWidth) {
            return text;
        }
        int leftPadding = (totalWidth - text.length()) / 2;
        int rightPadding = totalWidth - text.length() - leftPadding;
        return String.format("%" + leftPadding + "s", "") + text + String.format("%" + rightPadding + "s", "");
    }
}
