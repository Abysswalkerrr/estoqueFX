package com.estoquefx;

import okhttp3.*;
import com.google.gson.*;
import java.io.IOException;
import java.util.*;

public class SupabaseService {
    private final OkHttpClient client;
    private final String baseUrl;
    private final String apiKey;
    private String authToken = null; // Token do usuário logado

    public SupabaseService() {
        this.client = new OkHttpClient();
        this.baseUrl = SupabaseConfig.SUPABASE_URL;
        this.apiKey = SupabaseConfig.SUPABASE_KEY;
    }

    // LOGIN
    public boolean login(String email, String senha) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", senha);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(baseUrl + "/auth/v1/token?grant_type=password")
                .post(body)
                .addHeader("apikey", apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            JsonObject result = JsonParser.parseString(responseBody).getAsJsonObject();
            authToken = result.get("access_token").getAsString();
            return true;
        }
        return false;
    }

    // REGISTRAR NOVO USUÁRIO
    public boolean registrar(String email, String senha) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", senha);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(baseUrl + "/auth/v1/signup")
                .post(body)
                .addHeader("apikey", apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }

    // CRIAR ESTOQUE (após login)
    public String criarEstoque(String nomeEstoque) throws IOException {
        if (authToken == null) throw new IllegalStateException("Usuário não logado");

        JsonObject json = new JsonObject();
        json.addProperty("nome", nomeEstoque);
        // NÃO adicione usuario_id aqui - o trigger faz automaticamente

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/estoques")
                .post(body)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            JsonArray result = JsonParser.parseString(responseBody).getAsJsonArray();
            return result.get(0).getAsJsonObject().get("id").getAsString();
        }

        // Imprimir erro detalhado
        String errorBody = response.body() != null ? response.body().string() : "sem detalhes";
        throw new IOException("Erro ao criar estoque: " + response.code() + " - " + errorBody);
    }
    // BUSCAR ESTOQUES DO USUÁRIO
    public List<JsonObject> listarEstoques() throws IOException {
        if (authToken == null) throw new IllegalStateException("Usuário não logado");

        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/estoques?select=*")
                .get()
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            JsonArray array = JsonParser.parseString(responseBody).getAsJsonArray();

            List<JsonObject> estoques = new ArrayList<>();
            array.forEach(e -> estoques.add(e.getAsJsonObject()));
            return estoques;
        }
        throw new IOException("Erro ao listar estoques: " + response.code());
    }

    // DELETAR todos produtos de um estoque (antes de salvar novamente)
    public void deletarProdutosEstoque(String estoqueId) throws IOException {
        if (authToken == null) throw new IllegalStateException("Usuário não logado");

        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/produtos?estoque_id=eq." + estoqueId)
                .delete()
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Erro ao deletar produtos: " + response.code());
        }
    }


    // SALVAR PRODUTO
    public void salvarProduto(Produto produto, String estoqueId) throws IOException {
        if (authToken == null) throw new IllegalStateException("Usuário não logado");

        JsonObject json = new JsonObject();
        json.addProperty("estoque_id", estoqueId);
        json.addProperty("codigo", produto.getCodigo());
        json.addProperty("nome", produto.getNome());
        json.addProperty("categoria", produto.getCategoria());
        json.addProperty("qtd_min", produto.getVlrMin());
        json.addProperty("valor_unitario", produto.getVlrUnd());
        json.addProperty("quantidade", produto.getQtd());
        json.addProperty("descricao", produto.getDescricao());

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/produtos")
                .post(body)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Erro ao salvar produto: " + response.code());
        }
    }

    // BUSCAR PRODUTOS DE UM ESTOQUE
    public List<Produto> carregarProdutos(String estoqueId) throws IOException {
        if (authToken == null) throw new IllegalStateException("Usuário não logado");

        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/produtos?estoque_id=eq." + estoqueId + "&select=*")
                .get()
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            JsonArray array = JsonParser.parseString(responseBody).getAsJsonArray();

            List<Produto> produtos = new ArrayList<>();
            array.forEach(item -> {
                JsonObject obj = item.getAsJsonObject();
                Produto p = new Produto(
                        obj.get("codigo").getAsString(),
                        obj.get("nome").getAsString(),
                        obj.get("categoria").getAsString(),
                        obj.get("qtd_min").getAsInt(),
                        obj.get("valor_unitario").getAsDouble(),
                        obj.get("quantidade").getAsInt(),
                        obj.has("descricao") ? obj.get("descricao").getAsString() : "",
                        obj.has("ultima_alteracao") ? obj.get("ultima_alteracao").getAsString() : ""
                );
                produtos.add(p);
            });
            return produtos;
        }
        throw new IOException("Erro ao carregar produtos: " + response.code());
    }

    public boolean isLogado() {
        return authToken != null;
    }
}
