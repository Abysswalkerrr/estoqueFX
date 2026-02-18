package com.estoquefx.service.patrimonio;

import com.estoquefx.model.patrimonio.Patrimonio;
import com.estoquefx.service.SupabaseService;
import com.estoquefx.util.SupabaseConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PatrimonioService {

    private final OkHttpClient client;
    private final String baseUrl;
    private final String apiKey;
    private final SupabaseService supabaseService;

    public PatrimonioService(SupabaseService supabaseService) {
        this.client = new OkHttpClient();
        this.baseUrl = SupabaseConfig.getSupabaseUrl();
        this.apiKey = SupabaseConfig.getSupabaseKey();
        this.supabaseService = supabaseService;
    }

    public void deletarPatrimoniosEstoque(String estoqueId) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/patrimonios?estoque_id=eq." + estoqueId)
                .delete()
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + supabaseService.getAuthToken())
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Erro ao deletar patrimônios: " + response.code());
        }
    }

    public void salvarPatrimonio(Patrimonio p, String estoqueId) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("estoque_id", estoqueId);
        json.addProperty("codigo", p.getCodigo());
        json.addProperty("nome", p.getNome());
        json.addProperty("descricao", p.getDescricao());
        json.addProperty("localizacao", p.getLocalizacao());
        json.addProperty("estado", p.getEstado());
        json.addProperty("alter_hora", p.getAlterHora());

        RequestBody body = RequestBody.create(
                json.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/patrimonios")
                .post(body)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + supabaseService.getAuthToken())
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Erro ao salvar patrimônio: " + response.code());
        }
    }

    // Salva todos de uma vez (delete + insert)
    public void salvarTodos(List<Patrimonio> patrimonios, String estoqueId) throws IOException {
        deletarPatrimoniosEstoque(estoqueId);
        for (Patrimonio p : patrimonios) {
            salvarPatrimonio(p, estoqueId);
        }
    }

    public List<Patrimonio> carregarPatrimonios(String estoqueId) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/rest/v1/patrimonios?estoque_id=eq." + estoqueId + "&select=*")
                .get()
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + supabaseService.getAuthToken())
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            JsonArray array = JsonParser.parseString(responseBody).getAsJsonArray();

            List<Patrimonio> lista = new ArrayList<>();
            array.forEach(item -> {
                JsonObject obj = item.getAsJsonObject();
                lista.add(new Patrimonio(
                        obj.get("codigo").getAsString(),
                        obj.get("nome").getAsString(),
                        obj.has("descricao") ? obj.get("descricao").getAsString() : "",
                        obj.has("localizacao") ? obj.get("localizacao").getAsString() : "",
                        obj.has("estado") ? obj.get("estado").getAsString() : "BOM",
                        obj.has("alter_hora") ? obj.get("alter_hora").getAsString() : ""
                ));
            });
            return lista;
        }
        throw new IOException("Erro ao carregar patrimônios: " + response.code());
    }
}
