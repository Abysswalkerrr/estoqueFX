package com.estoquefx.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle bundle;

    public static void init(Locale locale) {
        bundle = ResourceBundle.getBundle("com.estoquefx.i18n.messages", locale);
    }

    public static String t(String key) {
        return bundle.getString(key);
    }

    public static ResourceBundle getBundle() { return bundle; }
}
