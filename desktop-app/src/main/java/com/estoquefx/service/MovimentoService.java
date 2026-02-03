package com.estoquefx.service;

import com.estoquefx.model.Movimento;
import com.estoquefx.model.Historico;
import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MovimentoService {

    private final String baseUrl;
    private final String apiKey;
    private String authToken;
    private final OkHttpClient client;

    public MovimentoService(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    //movimentos de quantidade
    public void salvarMovimento(Movimento movimento, String estoqueId) throws IOException {
        if (authToken == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        // todo investigar pq ajuste saida e vlrUnd não estão indo
        String tipo = movimento.getTipo().toUpperCase();
        if (!tipo.equals("ENTRADA") && !tipo.equals("SAIDA") &&
                !tipo.equals("AJUSTE") && !tipo.equals("ALTERACAO_VALOR")) {
            System.out.println("Movimento do tipo '" + tipo + "' não será salvo no Supabase");
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("estoque_id", estoqueId);
        json.addProperty("produto_codigo", movimento.getCodigo());
        json.addProperty("produto_nome", movimento.getNome());
        json.addProperty("tipo", tipo);
        json.addProperty("quantidade_anterior", movimento.getQuantidadeAnterior());
        json.addProperty("quantidade_nova", movimento.getQuantidadeNova());
        json.addProperty("quantidade_alterada", movimento.getDiff());

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/movimentacoes")
                .post(body)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro ao salvar movimento: " + response.code());
            }
            System.out.println("✓ Movimento salvo no Supabase: " + movimento.getTipo());
        }
    }


    public void carregarMovimentos(String estoqueId) throws IOException {
        if (authToken == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        String url = baseUrl + "/rest/v1/movimentacoes"
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
                throw new IOException("Erro ao carregar movimentos: " + response.code());
            }

            String responseBody = response.body().string();
            JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();

            Historico.limpar();

            System.out.println("📦 Carregando " + jsonArray.size() + " movimentos do Supabase...");

            for (JsonElement element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();

                String produtoCodigo = obj.get("produto_codigo").getAsString();
                String produtoNome = obj.get("produto_nome").getAsString();
                String tipo = obj.get("tipo").getAsString();
                int qtdAnterior = obj.get("quantidade_anterior").getAsInt();
                int qtdNova = obj.get("quantidade_nova").getAsInt();
                int qtdAlterada = obj.get("quantidade_alterada").getAsInt();

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
                    System.err.println("⚠ Erro ao parsear data: " + dataHoraStr);
                    dataHora = LocalDateTime.now();
                }

                // Construtor: (codigo, nome, tempo, tipo, quantidadeNova, diff, velhaQuantidade)
                //todo ver se vlrUnd e vlrMin funcionam com isso - talvez ignorar zeros no fim?
                new Movimento(produtoCodigo, produtoNome, dataHora, tipo,
                        qtdNova, qtdAlterada, qtdAnterior);
            }

            System.out.println("✓ Carregados " + jsonArray.size() + " movimentos do Supabase");
        }
    }

    public List<Movimento> buscarMovimentosProduto(String estoqueId, String produtoCodigo) throws IOException {
        if (authToken == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        String url = baseUrl + "/rest/v1/movimentacoes"
                + "?estoque_id=eq." + estoqueId
                + "&produto_codigo=eq." + produtoCodigo
                + "&order=data_hora.desc"
                + "&limit=100";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        List<Movimento> movimentos = new ArrayList<>();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro ao buscar movimentos do produto: " + response.code());
            }

            String responseBody = response.body().string();
            JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();

                String id = obj.get("id").getAsString();
                String codigo = obj.get("produto_codigo").getAsString();
                String nome = obj.get("produto_nome").getAsString();
                String tipo = obj.get("tipo").getAsString();
                int qtdAnterior = obj.get("quantidade_anterior").getAsInt();
                int qtdNova = obj.get("quantidade_nova").getAsInt();
                int qtdAlterada = obj.get("quantidade_alterada").getAsInt();
                String observacao = obj.has("observacao") && !obj.get("observacao").isJsonNull()
                        ? obj.get("observacao").getAsString() : "";

                String dataHoraStr = obj.get("data_hora").getAsString();
                LocalDateTime dataHora = LocalDateTime.parse(
                        dataHoraStr.substring(0, 19),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );

                Movimento mov = new Movimento(codigo, nome, dataHora, tipo,
                        qtdAnterior, qtdNova, qtdAlterada);
                movimentos.add(mov);
            }
        }

        return movimentos;
    }
}
