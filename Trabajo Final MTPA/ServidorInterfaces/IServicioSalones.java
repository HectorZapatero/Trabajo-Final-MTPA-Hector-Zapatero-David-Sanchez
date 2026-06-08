package ServidorInterfaces;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Contrato del servicio de control de salas encargado de monitorizar el flujo de uso por salón.
 */
public interface IServicioSalones {
    void registrarMensaje(String salon);
    ConcurrentHashMap<String, Integer> obtenerMetricas();
}