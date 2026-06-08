package Servidor;

import ServidorInterfaces.IServicioUsuarios;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación del servicio de usuarios que utiliza un mapa concurrente en memoria
 * sincronizado con un archivo plano CSV para el almacenamiento permanente de credenciales.
 */
public class GestorUsuariosCSV implements IServicioUsuarios {
    private static final String ARCHIVO_USUARIOS = "usuarios.csv";
    private final ConcurrentHashMap<String, String> usuariosRegistrados = new ConcurrentHashMap<>();

    public GestorUsuariosCSV() {
        cargarDesdeCSV();
    }

    private void cargarDesdeCSV() {
        File archivo = new File(ARCHIVO_USUARIOS);
        if (!archivo.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length == 2) {
                    usuariosRegistrados.put(partes[0], partes[1]);
                }
            }
            System.out.println("[Usuarios] Base de datos CSV sincronizada.");
        } catch (IOException e) {
            System.out.println("[Usuarios] Error al cargar el archivo CSV: " + e.getMessage());
        }
    }

    /**
     * Comprueba si un nombre de usuario ya se encuentra registrado en el sistema.
     * @param nombre Cadena con el identificador del usuario.
     * @return true si el usuario existe, false en caso contrario.
     */
    @Override
    public boolean existeUsuario(String nombre) {
        return usuariosRegistrados.containsKey(nombre);
    }

    /**
     * Comprueba si una clave de acceso numérica ya está asignada a algún usuario.
     * @param clave Cadena de cuatro dígitos autogenerada.
     * @return true si la clave ya está en uso, false si está libre.
     */
    @Override
    public boolean existeClave(String clave) {
        return usuariosRegistrados.containsValue(clave);
    }

    /**
     * Valida si la combinación de usuario y clave coincide con los registros almacenados.
     * @param nombre Identificador del usuario.
     * @param clave Clave de acceso del sistema.
     * @return true si las credenciales son válidas, false si fallan.
     */
    @Override
    public boolean validarCredenciales(String nombre, String clave) {
        return existeUsuario(nombre) && usuariosRegistrados.get(nombre).equals(clave);
    }

    /**
     * Registra un nuevo usuario en la estructura de memoria viva y añade la línea al archivo CSV.
     * @param nombre Identificador elegido por el usuario.
     * @param clave Código numérico asignado por el servidor.
     */
    @Override
    public void registrarUsuario(String nombre, String clave) {
        usuariosRegistrados.put(nombre, clave);
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_USUARIOS, true))) {
            pw.println(nombre + "," + clave);
        } catch (IOException e) {
            System.err.println("[Usuarios] Error de escritura en CSV: " + e.getMessage());
        }
    }
}