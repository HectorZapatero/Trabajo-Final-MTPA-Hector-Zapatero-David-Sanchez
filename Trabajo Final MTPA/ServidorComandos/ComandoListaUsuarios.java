package ServidorComandos;

import ServidorInterfaces.IClienteConectado;
import Servidor.ServidorChat;

public class ComandoListaUsuarios extends ComandoAutenticado {
    @Override
    protected void ejecutarAccion(IClienteConectado cliente, String[] partes) {
        StringBuilder listado = new StringBuilder();
        synchronized (ServidorChat.clientesConectados) {
            for (IClienteConectado c : ServidorChat.clientesConectados) {
                if (c.isAutenticado()) {
                    listado.append(c.getNombreUsuario()).append(",");
                }
            }
        }
        cliente.enviarMensaje("USERS_LIST|" + listado.toString());
    }
}