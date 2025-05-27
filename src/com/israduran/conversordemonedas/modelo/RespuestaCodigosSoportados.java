package com.israduran.conversordemonedas.modelo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record RespuestaCodigosSoportados(
        @SerializedName("result") String resultado,
        @SerializedName("supported_codes") List<List<String>> codigosSoportados) // Lista de [código, nombre]
{
}
