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
import java.util.Optional;
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

            String versaoRemota = extrairVersao(json);
            String linkInstaller = extrairInstaller(json);
            String changeLog = extrairChangelog(json);

            if (versaoRemota == null || linkInstaller == null) {
                return new UpdateInfo(AppInfo.VERSAO, null, null, null);
            }

            return new UpdateInfo(AppInfo.VERSAO, versaoRemota, linkInstaller, changeLog);
        }
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
        // Salvar flag com caminho do executável
        String appPath = getApplicationPath();
        salvarFlagReabrir(appPath);

        // Criar script de reabertura temporário
        Path scriptPath = criarScriptReabertura();

        System.out.println("🔄 Iniciando instalador...");

        // Executar instalador
        ProcessBuilder installerProcess = new ProcessBuilder(
                "cmd", "/c",
                "start /wait msiexec /i \"" + installerPath.toAbsolutePath().toString() + "\" /qb /norestart" +
                        " && \"" + scriptPath.toAbsolutePath().toString() + "\""
        );
        installerProcess.start();

        // Aguardar um pouco e fechar
        Thread.sleep(500);
        System.exit(0);
    }

    private static Path criarScriptReabertura() throws Exception {
        Path scriptPath = Files.createTempFile("estoquefx_reopen_", ".bat");

        String script = """
        @echo off
        timeout /t 3 /nobreak >nul
        set FLAG_FILE=%USERPROFILE%\\.estoquefx_reopen
        if exist "%FLAG_FILE%" (
            set /p APP_PATH=<"%FLAG_FILE%"
            del "%FLAG_FILE%"
            start "" "%APP_PATH%"
        )
        del "%~f0"
        """;

        Files.writeString(scriptPath, script);
        return scriptPath;
    }

    private static String getApplicationPath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Tentar obter o caminho completo via ProcessHandle
            try {
                ProcessHandle currentProcess = ProcessHandle.current();
                Optional<String> command = currentProcess.info().command();

                if (command.isPresent()) {
                    String fullPath = command.get();
                    System.out.println("✓ Executável detectado: " + fullPath);
                    return fullPath;
                }
            } catch (Exception e) {
                System.err.println("Erro ao detectar executável: " + e.getMessage());
            }

            // Fallback: procurar no Program Files
            String programFiles = System.getenv("ProgramFiles");
            if (programFiles != null) {
                Path exePath = Path.of(programFiles, "SistemaEstoqueFX", "SistemaEstoqueFX.exe");
                if (Files.exists(exePath)) {
                    System.out.println("✓ Executável encontrado: " + exePath);
                    return exePath.toString();
                }
            }

            // Se não encontrar, usar apenas o nome (assumindo que está no PATH)
            System.out.println("⚠️ Usando apenas nome do executável");
            return "SistemaEstoqueFX.exe";
        }

        return "SistemaEstoqueFX";
    }

    private static void salvarFlagReabrir(String appPath) {
        try {
            Path flagFile = Path.of(System.getProperty("user.home"), ".estoquefx_reopen");

            // Salvar o caminho do executável para reabrir
            Files.writeString(flagFile, appPath);

            System.out.println("✓ Flag de reabertura salva: " + appPath);
        } catch (Exception e) {
            System.err.println("Erro ao salvar flag de reabertura: " + e.getMessage());
        }
    }

    public static String getCaminhoParaReabrir() {
        try {
            Path flagFile = Path.of(System.getProperty("user.home"), ".estoquefx_reopen");
            if (Files.exists(flagFile)) {
                String caminho = Files.readString(flagFile).trim();
                Files.delete(flagFile);
                return caminho;
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar flag de reabertura: " + e.getMessage());
        }
        return null;
    }

    public static boolean deveReabrir() {
        return getCaminhoParaReabrir() != null;
    }

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

}
