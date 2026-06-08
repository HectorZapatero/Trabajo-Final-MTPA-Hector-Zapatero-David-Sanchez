package Servidor;

import ServidorInterfaces.IServicioSalones;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación volátil en memoria encargada de contabilizar la actividad y
 * el volumen acumulado de mensajes procesados de forma independiente por cada sala.
 */
public class GestorSalonesMemoria implements IServicioSalones {
    private final ConcurrentHashMap<String, Integer> mensajesPorSalon = new ConcurrentHashMap<>();

    /**
     * Incrementa en una unidad el contador de mensajes históricos registrados para un salón específico.
     * @param salon Identificador de la sala activa.
     */
    @Override
    public void registrarMensaje(String salon) {
        mensajesPorSalon.put(salon, mensajesPorSalon.getOrDefault(salon, 0) + 1);
    }

    /**
     * Recupera el mapa completo con las estadísticas de volumen de mensajes acumulados por sala.
     * @return Mapa concurrente con pares de valores (NombreSalón, MensajesTotales).
     */
    @Override
    public ConcurrentHashMap<String, Integer> obtenerMetricas() {
        return mensajesPorSalon;
    }
}