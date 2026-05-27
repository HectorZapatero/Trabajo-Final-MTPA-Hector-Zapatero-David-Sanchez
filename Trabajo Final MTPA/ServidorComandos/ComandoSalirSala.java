package ServidorComandos;

import ServidorInterfaces.IClienteConectado;

public class ComandoSalirSala extends ComandoAutenticado {
    @Override
    protected void ejecutarAccion(IClienteConectado cliente, String[] partes) {
        if (partes.length >= 2) {
            cliente.getSalonesActivos().remove(partes[1]);
        }
    }
}