package ServidorComandos;

import ServidorInterfaces.IClienteConectado;
import ServidorInterfaces.IServicioMensajes;
import java.util.List;

public class ComandoUnirSala extends ComandoAutenticado {
    private final IServicioMensajes servicioMensajes;

    public ComandoUnirSala(IServicioMensajes servicioMensajes) {
        this.servicioMensajes = servicioMensajes;
    }

    @Override
    protected void ejecutarAccion(IClienteConectado cliente, String[] partes) {
        if (partes.length >= 2) {
            String salon = partes[1];
            cliente.getSalonesActivos().add(salon);
            
            List<String> mensajesDeHoy = servicioMensajes.obtenerMensajesDeHoy(salon);
            for (String msg : mensajesDeHoy) {
                cliente.enviarMensaje(msg);
            }
        }
    }
}