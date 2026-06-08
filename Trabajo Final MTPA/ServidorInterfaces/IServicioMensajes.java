package ServidorInterfaces;

import java.util.List;

/**
 * Contrato del servicio encargado de la persistencia de mensajería y la
 * lógica de consulta selectiva de datos antiguos y de hoy.
 */
public interface IServicioMensajes {
    /**
     * Almacena un registro de mensaje en el sistema físico.
     */
    void guardarMensaje(String salon, String usuario, String fechaHora, String contenido);
    /**
     * Obtiene los mensajes del día de hoy para un salón específico.
     */
    List<String> obtenerMensajesDeHoy(String salon);
    /**
     * Recupera un bloque paginado de mensajes antiguos.
     */
    List<String> obtenerHistorialAnterior(String salon, int offset, int cantidad);
}