package ServidorInterfaces;

import java.util.Set;

public interface IClienteConectado {
    void enviarMensaje(String mensaje);
    void cerrarConexion();
    String getNombreUsuario();
    void setNombreUsuario(String nombre);
    boolean isAutenticado();
    void setAutenticado(boolean estado);
    Set<String> getSalonesActivos();
}