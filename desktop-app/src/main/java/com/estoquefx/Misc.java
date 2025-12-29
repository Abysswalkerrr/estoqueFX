package com.estoquefx;

import java.util.HashSet;

//todo nomes iguais
//todo autocomplete

public class Misc {
    public static HashSet<String> categorias = new HashSet<>();
    public static HashSet<String> nomes = new HashSet<>();

    private static double total;

    public static void addCategoria(String categoria) {
        categorias.add(categoria);
    }

    public static void carregaCategorias(){
        for (Produto p : Produto.estoque) {
            addCategoria(p.getCategoria());
        }
    }

    public static void carregaNomes(){
        for (Produto p : Produto.estoque) {
            addNome(p.getNome());
        }
    }

    public static void addNome(String nome) {
        nomes.add(nome);
    }

    public static void removeNome(String nome){
        nomes.remove(nome);
    }

    public static boolean isNumeric(String n) {
        try{
            Double.parseDouble(n);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static String isUrgente(Produto produto){
        if (produto.getCompra()){
            return "Compra urgente";
        } else {
            return "Estoque suficiente";
        }
    }

    public static void atualizaTotal() {
        total = 0;
        for (Produto p : Produto.estoque) {
            total += p.getQtd() * p.getVlrUnd();
        }
    }

    public static double getTotal(){
        return total;
    }

}



/**             <plugin>
 <groupId>org.apache.maven.plugins</groupId>
 <artifactId>maven-shade-plugin</artifactId>
 <version>3.5.1</version>
 <executions>
 <execution>
 <phase>package</phase>
 <goals>
 <goal>shade</goal>
 </goals>
 <configuration>
 <transformers>
 <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
 <mainClass>com.estoquefx.Launcher</mainClass>
 </transformer>
 </transformers>
 </configuration>
 </execution>
 </executions>
 </plugin>
**/

