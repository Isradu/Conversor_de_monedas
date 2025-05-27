package com.israduran.conversordemonedas.interfazusuario;

import com.israduran.conversordemonedas.modelo.RegistroConversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InterfazConsola {
    private Scanner escaner;

    //COLORES
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_RED = "\u001B[31m";

    // CONSTRUCTOR
    public InterfazConsola() {
        this.escaner = new Scanner(System.in);
    }

    //MENSAJE DE BIENVENIDA AL USUARIO
    public void mostrarMensajeBienvenida() {
        System.out.println(ANSI_CYAN + "***************************************************" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "* Bienvenido al Conversor de Monedas Mundial *" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "***************************************************" + ANSI_RESET);
        System.out.println();
    }


    //MENÚ PRINCIPAL
    public int mostrarMenuPrincipalYObtenerOpcion() {
        System.out.println(ANSI_YELLOW + "\nMenú Principal:" + ANSI_RESET);
        System.out.println("1. Convertir monedas");
        System.out.println("2. Ver historial de conversiones");
        System.out.println("3. Borrar historial de conversiones");
        System.out.println("0. Salir");
        System.out.print(ANSI_GREEN + "Opción: " + ANSI_RESET);

        try {
            int opcion = escaner.nextInt(); // guarda la opción que el usario ingresa en opcion
            escaner.nextLine(); // consume el salto de línea
            return opcion;
        } catch (InputMismatchException e) {
            escaner.nextLine(); // Consume la entrada inválida
            return -1; // Opción inválida
        }
    }


//     * Pregunta al usuario los códigos de moneda de origen y destino (si los sabe).
//     * O también ofrece la opción de listar todos los códigos soportados de la API.
//     * codigosSoportados: Es un Map con los códigos y nombres de las monedas de cada país.
//     * @return: Un array de String con dos elementos: [códigoOrigen, códigoDestino], o null si el usuario cancela o hay error.

    public String[] solicitarParDeMonedas(Map<String, String> codigosSoportados) {
        if (codigosSoportados.isEmpty()) { // Si está vacía....
            mostrarError("No se pudieron cargar los códigos de moneda soportados. Intente más tarde.");
            return null;
        }

        System.out.println(ANSI_BLUE + "Puede escribir 'LISTA' para ver todos los códigos de moneda disponibles." + ANSI_RESET);

        String monedaOrigen = "";
        boolean origenValido = false;
        while (!origenValido) {
            monedaOrigen = obtenerCodigoMonedaInterno("Ingrese el código de la moneda ORIGEN (ej. USD, MXN, ARS, BRL)");
            if ("LISTA".equalsIgnoreCase(monedaOrigen)) {
                mostrarCodigosDisponibles(codigosSoportados);
                continue; // Volver a pedir moneda origen
            }
            if (codigosSoportados.containsKey(monedaOrigen)) {
                System.out.println(ANSI_GREEN + "Moneda Origen: " + monedaOrigen + " (" + codigosSoportados.get(monedaOrigen) + ")" + ANSI_RESET);
                origenValido = true;
            } else if ("SALIR".equalsIgnoreCase(monedaOrigen)) {
                return null; // Usuario solicita salír
            }
            else {
                mostrarError("Código de moneda origen no válido. Escriba 'LISTA' para ver opciones o 'SALIR' para cancelar.");
            }
        }

        String monedaDestino = "";
        boolean destinoValido = false;
        while (!destinoValido) {
            monedaDestino = obtenerCodigoMonedaInterno("Ingrese el código de la moneda DESTINO (ej. EUR)");
            if ("LISTA".equalsIgnoreCase(monedaDestino)) {
                mostrarCodigosDisponibles(codigosSoportados);
                continue; // Volver a pedir moneda destino
            }
            if (codigosSoportados.containsKey(monedaDestino)) {
                System.out.println(ANSI_GREEN + "Moneda Destino: " + monedaDestino + " (" + codigosSoportados.get(monedaDestino) + ")" + ANSI_RESET);
                destinoValido = true;
            } else if ("SALIR".equalsIgnoreCase(monedaDestino)) {
                return null; // Usuario pide salír
            }
            else {
                mostrarError("Código de moneda destino no válido. Escriba 'LISTA' para ver opciones o 'SALIR' para cancelar.");
            }
        }
        return new String[]{monedaOrigen, monedaDestino};
    }

    // Metodo para obtener el código de moneda
    private String obtenerCodigoMonedaInterno(String mensajeSolicitud) {
        System.out.print(ANSI_YELLOW + mensajeSolicitud + ": " + ANSI_RESET);
        return escaner.nextLine().toUpperCase().trim();
    }

    // Metodo que muestra los códigos de monedas disponibles
    public void mostrarCodigosDisponibles(Map<String, String> codigosSoportados) {
        if (codigosSoportados.isEmpty()) {
            System.out.println(ANSI_YELLOW + "No hay códigos de moneda disponibles para mostrar." + ANSI_RESET);
            return;
        }
        System.out.println(ANSI_CYAN + "\n--- Códigos de Moneda Disponibles ---" + ANSI_RESET);
        // Para mejor lectura y ordenar alfabéticamente por código y mostrar en columnas
        List<String> codigosOrdenados = new ArrayList<>(codigosSoportados.keySet());
        Collections.sort(codigosOrdenados);

        int columnas = 3; // Ajusta según el ancho de consola
        int itemsPorColumna = (int) Math.ceil((double) codigosOrdenados.size() / columnas);

        for (int i = 0; i < itemsPorColumna; i++) {
            for (int j = 0; j < columnas; j++) {
                int index = i + j * itemsPorColumna;
                if (index < codigosOrdenados.size()) {
                    String codigo = codigosOrdenados.get(index);
                    String nombre = codigosSoportados.get(codigo);
                    // Cortar nombre si es muy largo para formato de columna
                    String nombreCorto = nombre.length() > 25 ? nombre.substring(0, 22) + "..." : nombre;
                    System.out.printf("%-7s (%-25s)\t", codigo, nombreCorto);
                } else {
                    System.out.print("\t\t\t\t"); // Espacio para alinear columnas
                }
            }
            System.out.println();
        }
        System.out.println(ANSI_CYAN + "------------------------------------" + ANSI_RESET);
    }

    //  mostrarResultadoConversion, mostrarError,
    //      mostrarMensaje, mostrarHistorial, confirmarBorradoHistorial, cerrarEscaner
    //      permanecen sin cambios estructurales, solo asegúrate que usan tus constantes ANSI)
    // Ejemplo de obtenerCantidadAConvertir (sin cambios, solo para completitud):

    // Metodo para obtener la cantidad a convertir
    public double obtenerCantidadAConvertir() {
        System.out.print(ANSI_YELLOW + "Ingrese la cantidad a convertir: " + ANSI_RESET);
        while (true) {
            try {
                double cantidad = escaner.nextDouble();
                escaner.nextLine(); // consume también el salto de línea
                if (cantidad <= 0) {
                    System.out.println(ANSI_RED + "Por favor, ingrese un monto positivo." + ANSI_RESET);
                    System.out.print(ANSI_YELLOW + "Ingrese la cantidad a convertir: " + ANSI_RESET);
                } else {
                    return cantidad;
                }
            } catch (InputMismatchException e) {
                System.out.println(ANSI_RED + "Entrada inválida. Por favor, ingrese un número." + ANSI_RESET);
                escaner.nextLine(); // consume la entrada inválida
                System.out.print(ANSI_YELLOW + "Ingrese la cantidad a convertir: " + ANSI_RESET);
            }
        }
    }

    // Metodo para mostrar el resultado de la conversión
    public void mostrarResultadoConversion(double cantidadOriginal, String monedaOrigen, double cantidadConvertida, String monedaDestino, double tasaAplicada) {
        System.out.println(ANSI_GREEN + "\n--- Resultado de la Conversión ---" + ANSI_RESET);
        System.out.printf(" %.2f %s equivalen a %.2f %s%n", cantidadOriginal, monedaOrigen, cantidadConvertida, monedaDestino);
        System.out.printf(" (Tasa de cambio: 1 %s = %.4f %s)%n", monedaOrigen, tasaAplicada, monedaDestino);
        System.out.println(ANSI_GREEN + "----------------------------------" + ANSI_RESET);
    }

    public void mostrarError(String mensaje) {
        System.out.println(ANSI_RED + "ERROR: " + mensaje + ANSI_RESET);
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(ANSI_BLUE + mensaje + ANSI_RESET);
    }

    public void mostrarHistorial(List<RegistroConversion> historial) {
        if (historial.isEmpty()) {
            System.out.println(ANSI_YELLOW + "El historial de conversiones está vacío." + ANSI_RESET);
            return;
        }
        System.out.println(ANSI_CYAN + "\n--- Historial de Conversiones (Más recientes primero) ---" + ANSI_RESET);
        for (RegistroConversion registro : historial) {
            System.out.println(registro.toString());
        }
        System.out.println(ANSI_CYAN + "-------------------------------------------------------" + ANSI_RESET);
    }

    public boolean confirmarBorradoHistorial() {
        System.out.print(ANSI_RED + "¿Está seguro de que desea borrar TODO el historial de conversiones? (s/N): " + ANSI_RESET);
        String confirmacion = escaner.nextLine().trim().toLowerCase();
        return "s".equals(confirmacion);
    }

    public void cerrarEscaner() {
        if (escaner != null) {
            escaner.close();
        }
    }
}
