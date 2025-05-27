package com.israduran.conversordemonedas.modelo;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

// Record para mapear la respuesta json de la API
public record RespuestaAPI(
        @SerializedName("result") String resultado,
        @SerializedName("base_code") String codigoBase,
        @SerializedName("target_code") String codigoDestino, // Para el endpoint /pair
        @SerializedName("conversion_rate") Double tasaDeConversionUnica, // Para el endpoint /pair
        @SerializedName("conversion_rates") Map<String, Double> tasasDeConversion // Para el endpoint /latest
) {
   @Override
   public String toString() {
       return "RespuestaAPI{" +
               "resultado='" + resultado + '\'' +
               ", codigoBase='" + codigoBase + '\'' +
               ", tasasDeConversion=" + tasasDeConversion +
               ", tasaDeConversionUnica=" + tasaDeConversionUnica +
               '}';
   }
}
