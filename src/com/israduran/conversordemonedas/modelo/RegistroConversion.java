package com.israduran.conversordemonedas.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record RegistroConversion(
        String monedaOrigen,
        String monedaDestino,
        double cantidadOriginal,
        double cantidadConvertida,
        double tasaAplicada,
        LocalDateTime fechaHora // Estees el único atributo que necesita manejo especial por gson (necesita un adaptador)
) {
    // Constructor personalizado para inicializar fechaHora automáticamente si no se le coloca manualmente
    public RegistroConversion(String monedaOrigen, String monedaDestino, double cantidadOriginal, double cantidadConvertida, double tasaAplicada) {
        this(monedaOrigen, monedaDestino, cantidadOriginal, cantidadConvertida, tasaAplicada, LocalDateTime.now());
    }

    // Sobrescribimos toString para un formato más amigable
    @Override
    public String toString() {
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] " +
                        "%.2f %s -> %.2f %s " +
                        "(Tasa: %.4f)",
                fechaHora.format(formateador),
                cantidadOriginal, monedaOrigen,
                cantidadConvertida, monedaDestino,
                tasaAplicada);
    }
}
