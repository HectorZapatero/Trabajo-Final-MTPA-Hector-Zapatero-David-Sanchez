package ServidorComandos;

import ServidorInterfaces.IClienteConectado;

public class ComandoLogout extends ComandoAutenticado {
    @Override
    protected void ejecutarAccion(IClienteConectado cliente, String[] partes) {
        cliente.setAutenticado(false);
        cliente.cerrarConexion();
    }
}