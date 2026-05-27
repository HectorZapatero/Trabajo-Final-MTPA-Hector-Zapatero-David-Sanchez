package Servidor;

import ServidorInterfaces.IServicioUsuarios;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

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

    @Override
    public boolean existeUsuario(String nombre) {
        return usuariosRegistrados.containsKey(nombre);
    }

    // --- NUEVA IMPLEMENTACIÓN ---
    @Override
    public boolean existeClave(String clave) {
        return usuariosRegistrados.containsValue(clave);
    }

    @Override
    public boolean validarCredenciales(String nombre, String clave) {
        return existeUsuario(nombre) && usuariosRegistrados.get(nombre).equals(clave);
    }

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