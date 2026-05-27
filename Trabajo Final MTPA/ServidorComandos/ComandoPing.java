package ServidorComandos;

import ServidorInterfaces.IClienteConectado;

public class ComandoPing implements Comando {
    @Override
    public void ejecutar(IClienteConectado cliente, String[] partes) {
        cliente.enviarMensaje("PONG");
    }
}