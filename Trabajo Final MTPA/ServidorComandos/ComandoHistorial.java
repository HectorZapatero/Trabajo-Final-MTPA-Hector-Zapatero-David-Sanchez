package ServidorComandos;

import ServidorInterfaces.IClienteConectado;
import ServidorInterfaces.IServicioMensajes;
import java.util.List;

public class ComandoHistorial extends ComandoAutenticado {
    private final IServicioMensajes servicioMensajes;

    public ComandoHistorial(IServicioMensajes servicioMensajes) {
        this.servicioMensajes = servicioMensajes;
    }

    @Override
    protected void ejecutarAccion(IClienteConectado cliente, String[] partes) {
        if (partes.length >= 3) {
            String salon = partes[1];
            try {
                int offset = Integer.parseInt(partes[2]);
                List<String> historial = servicioMensajes.obtenerHistorialAnterior(salon, offset, 50);
                for (String msg : historial) {
                    cliente.enviarMensaje(msg);
                }
            } catch (NumberFormatException e) {
                cliente.enviarMensaje("MSG_ERR|Formato de offset inválido");
            }
        }
    }
}