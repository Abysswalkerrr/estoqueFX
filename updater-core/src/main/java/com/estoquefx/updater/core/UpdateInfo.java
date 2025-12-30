package com.estoquefx.updater.core;

public class UpdateInfo {
    private final String versaoAtual;
    private final String versaoRemota;
    private final String urlInstaller;
    private final String changeLog;

    public UpdateInfo(String versaoAtual, String versaoRemota, String urlInstaller, String changeLog) {
        this.versaoAtual = versaoAtual;
        this.versaoRemota = versaoRemota;
        this.urlInstaller = urlInstaller;
        this.changeLog = changeLog;
    }

    public String getVersaoAtual() { return versaoAtual; }
    public String getVersaoRemota() { return versaoRemota; }
    public String getUrlInstaller() { return urlInstaller; }
    public String getChangeLog() { return changeLog; }

    public boolean hasUpdate() {
        if (versaoRemota == null) return false;

        String[] atualParts = versaoAtual.split("\\.");
        String[] remotaParts = versaoRemota.split("\\.");

        for (int i = 0; i < Math.min(atualParts.length, remotaParts.length); i++) {
            int atualNum = Integer.parseInt(atualParts[i]);
            int remotaNum = Integer.parseInt(remotaParts[i]);

            if (remotaNum > atualNum) return true;
            else if (remotaNum < atualNum) return false;
        }

        // Mesma versão (ex: 1.1.1 vs 1.1.1.0)
        return remotaParts.length > atualParts.length;
    }
}
