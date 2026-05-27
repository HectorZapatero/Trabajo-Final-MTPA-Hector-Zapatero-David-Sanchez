package ServidorInterfaces;

import java.util.List;

public interface IServicioMensajes {
    void guardarMensaje(String salon, String usuario, String fechaHora, String contenido);
    List<String> obtenerMensajesDeHoy(String salon);
    List<String> obtenerHistorialAnterior(String salon, int offset, int cantidad);
}