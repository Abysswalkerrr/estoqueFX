package com.estoquefx.updater.core;

public class AppInfo {
    public static final String NOME_APP =
            "SistemaEstoqueFX";
    public static final String VERSAO   =
            "3.4.1.0";

    public static final String BUG_REPORT_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSd_phUkuqlleT4CsKnvZPnEruQDdZK7qeCkvGU3HXa8D6ruWw/viewform?usp=dialog";

    public static final String SUGGESTIONS_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSdbPIgxiZlIa5eCqGVBFEs1BI2vEEjZvDG-zoP6pcg59yVlFw/viewform";

    public static final String UPDATE_URL = getUpdateUrl();

    public static final String RELEASES_URL =
            "https://api.github.com/repos/Abysswalkerrr/estoque_releases/releases";

    public static final String LATEST_RELEASES_URL =
            "https://api.github.com/repos/abysswalkerrr/estoque_releases/releases/latest";

    private static String getUpdateUrl() {
        return LATEST_RELEASES_URL;
    }

}
