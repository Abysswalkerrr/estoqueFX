package com.estoquefx.updater;

public class UpdateInfo {
    private final String versaoAtual;
    private final String versaoRemota;
    private final String urlInstaller;

    public UpdateInfo(String versaoAtual, String versaoRemota, String urlInstaller) {
        this.versaoAtual = versaoAtual;
        this.versaoRemota = versaoRemota;
        this.urlInstaller = urlInstaller;
    }

    public String getVersaoAtual() { return versaoAtual; }
    public String getVersaoRemota() { return versaoRemota; }
    public String getUrlInstaller() { return urlInstaller; }

    public boolean hasUpdate() {
        return versaoRemota != null && !versaoRemota.equals(versaoAtual);
    }
}
