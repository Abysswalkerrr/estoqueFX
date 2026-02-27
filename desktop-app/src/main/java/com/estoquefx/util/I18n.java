package com.estoquefx.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle bundle;
    private static Locale currentLocale;

    public static void init(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("com.estoquefx.i18n.messages", locale);
    }

    public static String t(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key; // fallback: mostra a chave se não achar
        }
    }

    public static ResourceBundle getBundle() { return bundle; }
    public static Locale getLocale() { return currentLocale; }

    public static boolean isPt() {
        return currentLocale != null && currentLocale.getLanguage().equals("pt");
    }
}
