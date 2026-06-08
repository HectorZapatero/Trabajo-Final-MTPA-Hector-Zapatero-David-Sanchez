package ServidorInterfaces;

/**
 * Contrato del servicio de seguridad encargado del alta y validación de usuarios.
 */
public interface IServicioUsuarios {
    boolean existeUsuario(String nombre);
    boolean existeClave(String clave);
    boolean validarCredenciales(String nombre, String clave);
    void registrarUsuario(String nombre, String clave);
}