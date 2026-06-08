package ServidorInterfaces;

import java.util.Set;

/**
 * Contrato de red que define las operaciones permitidas para interactuar con un cliente
 * remoto: control de buffers, flujo de desconexión y obtención de variables de sesión.
 */
public interface IClienteConectado {
    void enviarMensaje(String mensaje);
    void cerrarConexion();
    String getNombreUsuario();
    void setNombreUsuario(String nombre);
    boolean isAutenticado();
    void setAutenticado(boolean estado);
    Set<String> getSalonesActivos();
}