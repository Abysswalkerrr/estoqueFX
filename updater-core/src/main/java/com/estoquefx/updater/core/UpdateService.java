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

    public UpdateInfo verificarUpdate() throws Exception {
        URL url = new URL(AppInfo.UPDATE_URL);
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

            String json = in.lines().collect(Collectors.joining());

            String versaoRemota;
            String linkInstaller;
            String changeLog;

            if (AppInfo.isBeta()) {
                versaoRemota = extrairPrimeiraVersao(json);
                linkInstaller = extrairPrimeiroInstaller(json);
                changeLog = extrairPrimeiroChangelog(json);
            } else {
                versaoRemota = extrairVersao(json);
                linkInstaller = extrairInstaller(json);
                changeLog = extrairChangelog(json);
            }

            if (versaoRemota == null || linkInstaller == null) {
                return new UpdateInfo(AppInfo.VERSAO, null, null, null);
            }

            return new UpdateInfo(AppInfo.VERSAO, versaoRemota, linkInstaller, changeLog);
        }
    }

    public Path downloadSilencioso(String urlInstaller, String versao) throws Exception {
        URL url = new URL(urlInstaller);
        Path temp = Files.createTempFile("SistemaEstoqueFX-" + versao + "-", ".msi");

        try (InputStream in = url.openStream()) {
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
        }

        return temp;
    }

    public static Path downloadComBarraDeProgresso(String urlInstaller, String versao,
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
        // Salvar flag para reabrir após instalação
        salvarFlagReabrir();

        // Executar instalador
        new ProcessBuilder("msiexec", "/i", installerPath.toAbsolutePath().toString(), "/qb+")
                .inheritIO()
                .start();
        System.exit(0);
    }

    private static void salvarFlagReabrir() {
        try {
            Path flagFile = Path.of(System.getProperty("user.home"), ".estoquefx_reopen");
            Files.writeString(flagFile, "true");
        } catch (Exception e) {
            System.err.println("Erro ao salvar flag de reabertura: " + e.getMessage());
        }
    }

    public static boolean deveReabrir() {
        try {
            Path flagFile = Path.of(System.getProperty("user.home"), ".estoquefx_reopen");
            if (Files.exists(flagFile)) {
                Files.delete(flagFile);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar flag de reabertura: " + e.getMessage());
        }
        return false;
    }

    // === Métodos para versão Stable (API /latest) ===

    private String extrairVersao(String json) {
        String tagPattern = "\"tag_name\":\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(tagPattern);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            String tag = matcher.group(1); // v1.0.1
            return tag.replace("v", "");   // 1.0.1
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

    private String extrairChangelog(String json) {
        String changelogPattern = "\"body\":\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(changelogPattern);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1).replace("\\n", "\n").replace("\\r", "");
        }
        return null;
    }

    // === Métodos para versão Beta (API /releases - array) ===

    private String extrairPrimeiraVersao(String json) {
        // Busca o primeiro "tag_name" no array de releases
        String tagPattern = "\"tag_name\":\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(tagPattern);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            String tag = matcher.group(1);
            return tag.replace("v", "");
        }
        return null;
    }

    private String extrairPrimeiroInstaller(String json) {
        // Busca o primeiro .msi no array
        String msiPattern = "\"browser_download_url\":\"([^\"]+\\.msi)\"";
        Pattern pattern = Pattern.compile(msiPattern);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extrairPrimeiroChangelog(String json) {
        // Busca o primeiro "body" no array
        String changelogPattern = "\"body\":\"([^\"]+?)\"";
        Pattern pattern = Pattern.compile(changelogPattern);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1).replace("\\n", "\n").replace("\\r", "");
        }
        return null;
    }
}
