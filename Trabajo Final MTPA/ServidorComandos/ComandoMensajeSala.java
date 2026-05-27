package ServidorComandos;

import Servidor.ServidorChat;
import ServidorInterfaces.IClienteConectado;
import ServidorInterfaces.IServicioMensajes;
import ServidorInterfaces.IServicioSalones;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComandoMensajeSala extends ComandoAutenticado {
    private final IServicioSalones servicioSalones;
    private final IServicioMensajes servicioMensajes;

    public ComandoMensajeSala(IServicioSalones servicioSalones, IServicioMensajes servicioMensajes) {
        this.servicioSalones = servicioSalones;
        this.servicioMensajes = servicioMensajes;
    }

    @Override
    protected void ejecutarAccion(IClienteConectado cliente, String[] partes) {
        if (ServidorChat.mantenimiento) {
            cliente.enviarMensaje("MSG_ERR|El servidor está en mantenimiento.");
            return;
        }
        if (partes.length >= 3) {
            String salon = partes[1];
            String contenido = partes[2];

            cliente.getSalonesActivos().add(salon);
            servicioSalones.registrarMensaje(salon);

            if (contenido.length() > 190) contenido = contenido.substring(0, 190);
            String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            servicioMensajes.guardarMensaje(salon, cliente.getNombreUsuario(), hora, contenido);

            String trama = "ROOM_BROADCAST|" + salon + "|" + cliente.getNombreUsuario() + "|" + hora + "|" + contenido;
            ServidorChat.difundirPorSala(salon, trama);
        }
    }
}