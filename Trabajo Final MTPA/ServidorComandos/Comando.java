package ServidorComandos;

import ServidorInterfaces.IClienteConectado;

public interface Comando {
    void ejecutar(IClienteConectado cliente, String[] partes);
}