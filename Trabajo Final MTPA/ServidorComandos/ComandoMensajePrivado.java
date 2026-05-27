package ServidorComandos;

import Servidor.ServidorChat;
import ServidorInterfaces.IClienteConectado;

public class ComandoMensajePrivado extends ComandoAutenticado {
    @Override
    protected void ejecutarAccion(IClienteConectado cliente, String[] partes) {
        if (ServidorChat.mantenimiento) {
            cliente.enviarMensaje("MSG_ERR|El servidor está en mantenimiento.");
            return;
        }
        if (partes.length >= 3) {
            String destino = partes[1];
            String texto = partes[2];
            
            // Se registra el canal activo en memoria para el remitente
            cliente.getSalonesActivos().add("Priv_" + destino);
            
            // Enrutamiento directo por socket en memoria viva (No toca el CSV)
            boolean enviado = ServidorChat.enviarPrivadoDirecto(cliente.getNombreUsuario(), destino, texto);
            if (!enviado) {
                cliente.enviarMensaje("PRIV_ERR|usuario_no_conectado");
            }
        }
    }
}