package com.estoquefx.updater;

import java.nio.file.Path;

public class UpdaterApp {
    public static void main(String[] args) {
        UpdateService service = new UpdateService();

        try {
            UpdateInfo info = service.checkForUpdate();

            if (!info.hasUpdate()) {
                System.out.println("Sem atualização disponível. Versão atual: " + info.getVersaoAtual());
                return;
            }

            System.out.println("Nova versão encontrada: " + info.getVersaoRemota());
            Path path = service.downloadInstaller(info.getUrlInstaller(), info.getVersaoRemota());
            service.runInstaller(path);
            System.out.println("Instalador iniciado: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
