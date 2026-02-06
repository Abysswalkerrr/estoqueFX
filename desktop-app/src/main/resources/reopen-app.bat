private static Path criarScriptReabertura() throws Exception {
    Path scriptPath = Files.createTempFile("estoquefx_reopen_", ".bat");

    String script = """
        @echo off
        echo Aguardando instalacao...
        timeout /t 3 /nobreak >nul

        set FLAG_FILE=%USERPROFILE%\\.estoquefx_reopen

        if exist "%FLAG_FILE%" (
            echo Lendo caminho do executavel...
            set /p APP_PATH=<"%FLAG_FILE%"

            echo Caminho: %APP_PATH%

            REM Deletar flag
            del "%FLAG_FILE%"

            REM Verificar se é caminho completo ou nome do executável
            if exist "%APP_PATH%" (
                echo Abrindo via caminho completo...
                start "" "%APP_PATH%"
            ) else (
                REM Tentar abrir pelo nome (se estiver no PATH)
                echo Abrindo via nome do executavel...
                start "" %APP_PATH%
            )

            echo Aplicativo reaberto
        ) else (
            echo Flag nao encontrada
        )

        REM Auto-deletar este script
        (goto) 2>nul & del "%~f0"
        """;

    Files.writeString(scriptPath, script);
    return scriptPath;
}
