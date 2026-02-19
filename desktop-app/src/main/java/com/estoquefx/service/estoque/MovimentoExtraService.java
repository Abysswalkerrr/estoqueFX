package com.estoquefx.service.estoque;

import com.estoquefx.model.estoque.Movimento;
import com.google.gson.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MovimentoExtraService {

    private final String baseUrl;
    private final String apiKey;
    private String authToken;
    private final OkHttpClient client;

    public MovimentoExtraService(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }


    public void salvarMovimento(Movimento movimento, String estoqueId) throws IOException {
        if (authToken == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        String tipo = movimento.getTipo().toUpperCase();

        // não salva movimentos de quantidade
        if (tipo.equals("ENTRADA") || tipo.equals("SAIDA") ||
                tipo.equals("AJUSTE") || tipo.equals("CRIACAO") ||
                tipo.equals("ALTERACAO_DADOS")) {
            return;
        }

        JsonObject json = criarJson(movimento, estoqueId, tipo);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/alteracoes_produto")
                .post(body)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("Erro ao salvar alteração (" + response.code() + "): " + errorBody);
            }
            System.out.println("✓ Alteração salva no Supabase: " + tipo);
        }
    }

    @NotNull
    private static JsonObject criarJson(Movimento movimento, String estoqueId, String tipo) {
        JsonObject json = new JsonObject();
        json.addProperty("estoque_id", estoqueId);
        json.addProperty("produto_codigo", movimento.getCodigo());
        json.addProperty("produto_nome", movimento.getNome());
        json.addProperty("tipo", tipo);

        // Preencher campos específicos por tipo
        if (tipo.equals("ALTERACAO_VALOR") || tipo.equals("AJUSTE_VALOR")) {
            json.addProperty("valor_antigo", movimento.getVelhoValor());
            json.addProperty("valor_novo", movimento.getValorNovo());
            json.addProperty("delta_valor", movimento.getDelta());
        }
        return json;
    }


    public void carregarAlteracoes(String estoqueId) throws IOException {
        if (authToken == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        String url = baseUrl + "/rest/v1/alteracoes_produto"
                + "?estoque_id=eq." + estoqueId
                + "&order=data_hora.desc"
                + "&limit=500";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro ao carregar alterações: " + response.code());
            }

            assert response.body() != null;
            String responseBody = response.body().string();
            JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();

            System.out.println("📦 Carregando " + jsonArray.size() + " alterações do Supabase...");

            for (JsonElement element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();

                String produtoCodigo = obj.get("produto_codigo").getAsString();
                String produtoNome = obj.get("produto_nome").getAsString();
                String tipo = obj.get("tipo").getAsString();

                String dataHoraStr = obj.get("data_hora").getAsString();
                LocalDateTime dataHora;
                try {
                    if (dataHoraStr.contains("+") || dataHoraStr.contains("Z")) {
                        dataHora = LocalDateTime.parse(
                                dataHoraStr.substring(0, 19),
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        );
                    } else {
                        dataHora = LocalDateTime.parse(dataHoraStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                } catch (Exception e) {
                    dataHora = LocalDateTime.now();
                }
                if (tipo.equals("ALTERACAO_VALOR")) {
                    double valorAntigo = obj.get("valor_antigo").getAsDouble();
                    double valorNovo = obj.get("valor_novo").getAsDouble();
                    double delta = obj.get("delta_valor").getAsDouble();

                    new Movimento(produtoCodigo, produtoNome, dataHora, tipo,
                            delta, valorNovo, valorAntigo);
                }
            }

            System.out.println("✓ Carregadas " + jsonArray.size() + " alterações do Supabase");
        }
    }
}
