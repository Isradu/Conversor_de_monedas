package com.israduran.conversordemonedas.servicio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.israduran.conversordemonedas.modelo.RegistroConversion;
import com.israduran.conversordemonedas.util.AdaptadorGsonLocalDateTime;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServicioHistorial {
    private static final String ARCHIVO_HISTORIAL = "historial_conversiones.json";
    private Gson gson;
    private List<RegistroConversion> historialConversiones;

    public ServicioHistorial() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new AdaptadorGsonLocalDateTime())
                .setPrettyPrinting() // Para que el json sea legible
                .create();
        this.historialConversiones = cargarHistorial();
    }

    public void agregarConversion(RegistroConversion registro) {
        // Añadir al principio de la lista para que los más recientes aparezcan primero al mostrar.
        historialConversiones.add(0, registro);
        guardarHistorial();
    }

    public List<RegistroConversion> getHistorial() {
        // Devuelve una copia para evitar modificaciones externas no deseadas.
        // El orden ya se mantiene al agregar y cargar (más recientes primero).
        return new ArrayList<>(historialConversiones);
    }

    public void borrarHistorial() {
        historialConversiones.clear();
        // borrar el archivo para que no quede un json vacío.
        try {
            Files.deleteIfExists(Paths.get(ARCHIVO_HISTORIAL));
            System.out.println("Archivo de historial '" + ARCHIVO_HISTORIAL + "' borrado.");
        } catch (IOException e) {
            System.err.println("Error al intentar borrar el archivo de historial: " + e.getMessage());
            // Si falla el borrado del archivo, guardamos la lista vacía en memoria
            // y se intentará escribir un json vacío en el proxximo guardado
            guardarHistorial(); // Guarda un array con el jsion vacío
        }
    }

    private List<RegistroConversion> cargarHistorial() {
        try (FileReader lector = new FileReader(ARCHIVO_HISTORIAL)) {
            Type tipoLista = new TypeToken<ArrayList<RegistroConversion>>() {}.getType();
            List<RegistroConversion> listaCargada = gson.fromJson(lector, tipoLista);
            if (listaCargada != null) {
                return listaCargada;
            }
        } catch (IOException e) {
            // si el archivo no existe la primera vez podemos...
            System.out.println("No se encontró archivo de historial previo ('" + ARCHIVO_HISTORIAL + "'). Se creará uno nuevo");
        }
        return new ArrayList<>();
    }

    private void guardarHistorial() {
        try (FileWriter escritor = new FileWriter(ARCHIVO_HISTORIAL)) {
            gson.toJson(historialConversiones, escritor);
        } catch (IOException e) {
            System.err.println("Error al guardar el historial en '" + ARCHIVO_HISTORIAL + "': " + e.getMessage());
        }
    }
}
