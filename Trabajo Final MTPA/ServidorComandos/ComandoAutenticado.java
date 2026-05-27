package ServidorComandos;

import ServidorInterfaces.IClienteConectado;

public abstract class ComandoAutenticado implements Comando {
    
    @Override
    public final void ejecutar(IClienteConectado cliente, String[] partes) {
        if (!cliente.isAutenticado()) {
            cliente.enviarMensaje("MSG_ERR|Debes iniciar sesión primero.");
            return;
        }
        ejecutarAccion(cliente, partes);
    }

    protected abstract void ejecutarAccion(IClienteConectado cliente, String[] partes);
}