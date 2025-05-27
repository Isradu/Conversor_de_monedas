package com.israduran.conversordemonedas.servicio;

import com.google.gson.Gson;
import com.israduran.conversordemonedas.modelo.RespuestaAPI; // Tu record existente
import com.israduran.conversordemonedas.modelo.RespuestaCodigosSoportados; // Nuevo record

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;


public class ServicioTipoCambio {

    private static String CLAVE_API;
    private static final String URL_BASE_API = "https://v6.exchangerate-api.com/v6/";
    private static final String NOMBRE_ARCHIVO_CONFIG = "config.properties";

    private HttpClient clienteHttp;
    private Gson gson;
    private Map<String, String> mapaCodigosSoportadosCache; // Cache para los códigos

    static {
        Properties propiedades = new Properties();
        try (InputStream input = ServicioTipoCambio.class.getClassLoader().getResourceAsStream(NOMBRE_ARCHIVO_CONFIG)) {
            if (input == null) {
                try (InputStream fileInput = new FileInputStream(NOMBRE_ARCHIVO_CONFIG)) {
                    propiedades.load(fileInput);
                } catch (IOException exFile) {
                    System.err.println("ADVERTENCIA: No se pudo cargar '" + NOMBRE_ARCHIVO_CONFIG + "' desde el directorio raíz. " + exFile.getMessage());
                    throw exFile;
                }
            } else {
                propiedades.load(input);
            }
            CLAVE_API = propiedades.getProperty("API_KEY");
            if (CLAVE_API == null || CLAVE_API.equals("TU_API_KEY_AQUI") || CLAVE_API.trim().isEmpty()) {
                mostrarErrorConfiguracionApiKey("La API Key no se encontró o no está configurada en '" + NOMBRE_ARCHIVO_CONFIG + "'.");
                CLAVE_API = "CLAVE_INVALIDA_VERIFICAR_CONFIG";
            }
        } catch (IOException ex) {
            mostrarErrorConfiguracionApiKey("No se pudo cargar el archivo '" + NOMBRE_ARCHIVO_CONFIG + "'. " + ex.getMessage());
            CLAVE_API = "CLAVE_INVALIDA_VERIFICAR_CONFIG";
        }
    }

    private static void mostrarErrorConfiguracionApiKey(String mensaje) {
        System.err.println("--------------------------------------------------------------------");
        System.err.println("ERROR DE CONFIGURACIÓN:");
        System.err.println(mensaje);
        System.err.println("Por favor, asegúrate de que exista el archivo '" + NOMBRE_ARCHIVO_CONFIG + "'");
        System.err.println("(ya sea en 'src/main/resources/' o en la raíz del proyecto)");
        System.err.println("y contenga: API_KEY=tu_clave_real");
        System.err.println("Puedes copiar 'config.properties.example' y modificarlo.");
        System.err.println("--------------------------------------------------------------------");
    }

    public ServicioTipoCambio() {
        this.clienteHttp = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.mapaCodigosSoportadosCache = null; // Inicializar cache como null
    }

    public double obtenerTasaConversion(String monedaBase, String monedaDestino) throws IOException, InterruptedException {
        if ("CLAVE_INVALIDA_VERIFICAR_CONFIG".equals(CLAVE_API)) {
            System.err.println("La API Key no está configurada correctamente. No se puede realizar la conversión.");
            return -1.0;
        }
        String urlCompleta = URL_BASE_API + CLAVE_API + "/pair/" + monedaBase + "/" + monedaDestino;
        HttpRequest peticionHttp = HttpRequest.newBuilder().uri(URI.create(urlCompleta)).build();
        HttpResponse<String> respuestaHttp = clienteHttp.send(peticionHttp, HttpResponse.BodyHandlers.ofString());
        if (respuestaHttp.statusCode() == 200) {
            RespuestaAPI respuestaApiConvertida = gson.fromJson(respuestaHttp.body(), RespuestaAPI.class);
            if ("success".equals(respuestaApiConvertida.resultado()) && respuestaApiConvertida.tasaDeConversionUnica() != null) {
                return respuestaApiConvertida.tasaDeConversionUnica();
            } else {
                System.err.println("Error en la respuesta interna de la API (endpoint /pair): " + respuestaHttp.body());
                if (respuestaHttp.body() != null && respuestaHttp.body().contains("invalid-key")) {
                    System.err.println("La API Key parece ser inválida según la API. Revisa tu configuración.");
                } else if (respuestaHttp.body() != null && (respuestaHttp.body().contains("unknown-code") || respuestaHttp.body().contains("unsupported-code") || respuestaHttp.body().contains("malformed-pair"))) {
                    System.err.println("Uno o ambos códigos de moneda son desconocidos, no soportados o el par está malformado.");
                }
                return -1.0;
            }
        } else {
            System.err.println("Error al conectar con la API (endpoint /pair). Código de estado: " + respuestaHttp.statusCode());
            System.err.println("Respuesta completa: " + respuestaHttp.body());
            if (respuestaHttp.statusCode() == 401 || respuestaHttp.statusCode() == 403) {
                System.err.println("Error de autenticación (401/403). Verifica tu API Key en la configuración.");
            } else if (respuestaHttp.statusCode() == 404 && respuestaHttp.body() != null && respuestaHttp.body().contains("unsupported-code")) {
                System.err.println("Uno de los códigos de moneda no es soportado por la API.");
            }
            return -1.0;
        }
    }


//     * Obterer un mapa de códigos de moneda soportados (ej. "USD" -> "US Dollar")
//     * Usa una cache para evitar llamadas repetidas a la API.
//     * @return: Un Map<String, String> de códigos a nombres de moneda, o un mapa vacío si hay un error.

    public Map<String, String> obtenerMapaCodigosSoportados() {
        if ("CLAVE_INVALIDA_VERIFICAR_CONFIG".equals(CLAVE_API)) {
            System.err.println("La API Key no está configurada. No se pueden obtener los códigos soportados.");
            return Collections.emptyMap();
        }
        if (this.mapaCodigosSoportadosCache != null) {
            return this.mapaCodigosSoportadosCache; // Devuelve desde la caché
        }

        String urlCompleta = URL_BASE_API + CLAVE_API + "/codes";
        HttpRequest peticionHttp = HttpRequest.newBuilder()
                .uri(URI.create(urlCompleta))
                .build();
        try {
            HttpResponse<String> respuestaHttp = clienteHttp.send(peticionHttp, HttpResponse.BodyHandlers.ofString());
            if (respuestaHttp.statusCode() == 200) {
                RespuestaCodigosSoportados respuestaConvertida = gson.fromJson(respuestaHttp.body(), RespuestaCodigosSoportados.class);
                if ("success".equals(respuestaConvertida.resultado()) && respuestaConvertida.codigosSoportados() != null) {
                    // Convierte List<List<String>> a Map<String, String>
                    this.mapaCodigosSoportadosCache = respuestaConvertida.codigosSoportados().stream()
                            .filter(par -> par.size() == 2) // Aseguramos que cada entrada tiene un código y nombre de país
                            .collect(Collectors.toMap(
                                    par -> par.get(0), // Código de moneda (ej. "USD")
                                    par -> par.get(1), // Nombre de moneda (ej. "US Dollar")
                                    (nombreExistente, nombreNuevo) -> nombreExistente, // En caso de duplicados, mantenemos el primero
                                    HashMap::new // Usar HashMap
                            ));
                    return this.mapaCodigosSoportadosCache;
                } else {
                    System.err.println("Error en la respuesta interna de la API (endpoint /codes): " + respuestaHttp.body());
                }
            } else {
                System.err.println("Error al conectar con la API (endpoint /codes). Código: " + respuestaHttp.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al obtener códigos soportados: " + e.getMessage());
        }
        return Collections.emptyMap(); // Devuelve mapa vacío en caso de error
    }
}
