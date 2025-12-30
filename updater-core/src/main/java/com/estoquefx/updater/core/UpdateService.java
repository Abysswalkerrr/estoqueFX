package com.estoquefx.updater.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.LongConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UpdateService {

    public static interface ProgressCallback {
        void updateProgress(double progress); // 0.0 a 1.0
        void updateMessage(String message);
    }

    public UpdateInfo checkForUpdate() throws Exception {
        URL url = new URL(AppInfo.UPDATE_URL);
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

            String json = in.lines().collect(Collectors.joining());

            String versaoRemota = extrairVersao(json);
            String linkInstaller = extrairInstaller(json);

            if (versaoRemota == null || linkInstaller == null) {
                return new UpdateInfo(AppInfo.VERSAO, null, null);
            }

            return new UpdateInfo(AppInfo.VERSAO, versaoRemota, linkInstaller);
        }
    }

    public Path downloadInstaller(String urlInstaller, String versao) throws Exception {
        URL url = new URL(urlInstaller);
        Path temp = Files.createTempFile("SistemaEstoqueFX-" + versao + "-", ".msi");

        try (InputStream in = url.openStream()) {
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
        }

        return temp;
    }

    public static Path downloadInstallerWithProgress(String urlInstaller, String versao,
                                                     LongConsumer progressBytes, long totalBytes) throws Exception {
        URL url = new URL(urlInstaller);
        Path temp = Files.createTempFile("SistemaEstoqueFX-" + versao + "-", ".msi");

        URLConnection conn = url.openConnection();
        long contentLength = conn.getContentLengthLong();
        if (totalBytes <= 0) {
            totalBytes = contentLength;
        }

        try (InputStream in = conn.getInputStream();
             OutputStream out = Files.newOutputStream(temp)) {

            byte[] buffer = new byte[8192];
            int l;
            long downloaded = 0;
            while ((l = in.read(buffer)) != -1) {
                out.write(buffer, 0, l);
                downloaded += l;
                if (progressBytes != null) {
                    progressBytes.accept(downloaded);
                }
            }
        }

        return temp;
    }

    public static void runInstaller(Path installerPath) throws Exception {
        new ProcessBuilder("msiexec", "/i", installerPath.toAbsolutePath().toString(), "/passive")
                .inheritIO()
                .start();
        System.exit(0);
    }

    private String extrairVersao(String json) {
        String tagPattern = "\"tag_name\":\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(tagPattern);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            String tag = matcher.group(1); // "v1.0.1"
            return tag.replace("v", "");   // "1.0.1"
        }
        return null;
    }

    private String extrairInstaller(String json) {
        String msiPattern = "\"browser_download_url\":\"([^\"]+\\.msi)\"";
        Pattern pattern = Pattern.compile(msiPattern);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
