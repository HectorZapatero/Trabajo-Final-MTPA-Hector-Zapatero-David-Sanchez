package Servidor;

import ServidorInterfaces.IServicioSalones;
import java.util.concurrent.ConcurrentHashMap;

public class GestorSalonesMemoria implements IServicioSalones {
    private final ConcurrentHashMap<String, Integer> mensajesPorSalon = new ConcurrentHashMap<>();

    @Override
    public void registrarMensaje(String salon) {
        mensajesPorSalon.put(salon, mensajesPorSalon.getOrDefault(salon, 0) + 1);
    }

    @Override
    public ConcurrentHashMap<String, Integer> obtenerMetricas() {
        return mensajesPorSalon;
    }
}