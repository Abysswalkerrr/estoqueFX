package com.estoquefx.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Time {
    public static void updateTime(){
        LocalDateTime hora = LocalDateTime.now();
        Misc.setUltimaAtualizacao(hora.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
    }

    public static LocalDateTime getTime(boolean mod){
        return LocalDateTime.now();
    }

    public static String getTime(){
        LocalDateTime tempo = LocalDateTime.now();
        return tempo.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

}
