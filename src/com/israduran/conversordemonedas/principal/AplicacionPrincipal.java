package com.israduran.conversordemonedas.principal;

import com.israduran.conversordemonedas.modelo.RegistroConversion;
import com.israduran.conversordemonedas.servicio.ServicioHistorial;
import com.israduran.conversordemonedas.servicio.ServicioTipoCambio;
import com.israduran.conversordemonedas.interfazusuario.InterfazConsola;
import java.io.IOException;
import java.util.Map;

public class AplicacionPrincipal {

    private ServicioTipoCambio servicioTipoCambio;
    private InterfazConsola interfazConsola;
    private ServicioHistorial servicioHistorial;
    private Map<String, String> mapaCodigosSoportados; // Cache con códigos soportados

    public AplicacionPrincipal() {
        this.servicioTipoCambio = new ServicioTipoCambio();
        this.interfazConsola = new InterfazConsola();
        this.servicioHistorial = new ServicioHistorial();

        // Cargar los códigos soportados al iniciar la aplicación
        this.mapaCodigosSoportados = servicioTipoCambio.obtenerMapaCodigosSoportados();
        if (this.mapaCodigosSoportados.isEmpty()) {
            interfazConsola.mostrarError("No se pudieron cargar los códigos de moneda. La aplicación podría no funcionar correctamente.");
            // System.exit(1); (en caso de que sea necesario por error crítico)
        }
    }

    public void ejecutar() {
        interfazConsola.mostrarMensajeBienvenida();
        boolean continuar = true;

        while (continuar) {
            int opcion = interfazConsola.mostrarMenuPrincipalYObtenerOpcion();
            switch (opcion) {
                case 1:
                    if (mapaCodigosSoportados.isEmpty()) {
                        interfazConsola.mostrarError("No hay códigos de moneda disponibles para realizar la conversión.");
                        // Intentar volver a cargar los códigos soportados...
                        this.mapaCodigosSoportados = servicioTipoCambio.obtenerMapaCodigosSoportados();
                        if(this.mapaCodigosSoportados.isEmpty()){
                            interfazConsola.mostrarError("Recarga fallida. Intente reiniciar la aplicación más tarde.");
                            break;
                        } else {
                            interfazConsola.mostrarMensaje("Códigos de moneda recargados.");
                        }
                    }
                    String[] parMonedas = interfazConsola.solicitarParDeMonedas(mapaCodigosSoportados);
                    if (parMonedas != null) {
                        realizarConversionConcreta(parMonedas[0], parMonedas[1]);
                    } else {
                        interfazConsola.mostrarMensaje("Conversión cancelada por el usuario.");
                    }
                    break;
                case 2:
                    interfazConsola.mostrarHistorial(servicioHistorial.getHistorial());
                    break;
                case 3:
                    if (interfazConsola.confirmarBorradoHistorial()) {
                        servicioHistorial.borrarHistorial();
                        interfazConsola.mostrarMensaje("Historial borrado exitosamente.");
                    } else {
                        interfazConsola.mostrarMensaje("Operación de borrado cancelada.");
                    }
                    break;
                case 0:
                    continuar = false;
                    break;
                default:
                    interfazConsola.mostrarError("Opción no válida. Por favor, intente de nuevo.");
                    break;
            }
        }
        interfazConsola.mostrarMensaje("Gracias por usar el Conversor de Monedas. ¡Hasta luego!");
        interfazConsola.cerrarEscaner();
    }

    private void realizarConversionConcreta(String monedaBase, String monedaDestino) {
        try {
            double cantidad = interfazConsola.obtenerCantidadAConvertir();
            double tasa = servicioTipoCambio.obtenerTasaConversion(monedaBase, monedaDestino);

            if (tasa != -1.0) {
                double cantidadConvertida = cantidad * tasa;
                interfazConsola.mostrarResultadoConversion(cantidad, monedaBase, cantidadConvertida, monedaDestino, tasa);
                servicioHistorial.agregarConversion(new RegistroConversion(monedaBase, monedaDestino, cantidad, cantidadConvertida, tasa));
            } else {
                // El servicioTipoCambio debería imprimir un error específico. pero...
                interfazConsola.mostrarError("No se pudo obtener la tasa de cambio."); // enviamos un mensaje genérico si es necesario
            }
        } catch (IOException | InterruptedException e) {
            interfazConsola.mostrarError("Error de conexión o al procesar la solicitud a la API: " + e.getMessage());
        } catch (Exception e) {
            interfazConsola.mostrarError("Ocurrió un error inesperado durante la conversión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AplicacionPrincipal app = new AplicacionPrincipal();
        app.ejecutar();
    }
}