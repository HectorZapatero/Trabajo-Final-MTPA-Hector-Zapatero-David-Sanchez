package ServidorInterfaces;

import java.util.concurrent.ConcurrentHashMap;

public interface IServicioSalones {
    void registrarMensaje(String salon);
    ConcurrentHashMap<String, Integer> obtenerMetricas();
}