package ServidorInterfaces;

public interface IServicioUsuarios {
    boolean existeUsuario(String nombre);
    boolean existeClave(String clave);
    boolean validarCredenciales(String nombre, String clave);
    void registrarUsuario(String nombre, String clave);
}