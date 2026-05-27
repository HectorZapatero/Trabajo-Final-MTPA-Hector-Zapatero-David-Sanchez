package Servidor;

import Cliente.ManejadorCliente;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorChat {

    private static final int PUERTO = 5000;
    public static ConcurrentHashMap<String, String> usuariosRegistrados = new ConcurrentHashMap<>();
    public static List<ManejadorCliente> clientesConectados = Collections.synchronizedList(new ArrayList<>());
    private static final String ARCHIVO_USUARIOS = "usuarios.csv";

    
    public static volatile boolean aceptarConexiones = true;
    public static volatile boolean mantenimiento = false;
    public static ConcurrentHashMap<String, Integer> mensajesPorSalon = new ConcurrentHashMap<>();

    private void cargarUsuariosDesdeCSV() {
        java.io.File archivo = new java.io.File(ARCHIVO_USUARIOS);
        if (!archivo.exists()) return;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length == 2) {
                    usuariosRegistrados.put(partes[0], partes[1]);
                }
            }
            System.out.println("Memoria sincronizada con el archivo CSV");
        } catch (IOException e) {
            System.out.println("Error al cargar usuarios desde el archivo CSV: " + e.getMessage());
        }
    }

    
    private void iniciarConsolaAdmin() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n=== CONSOLA DE ADMINISTRACIÓN ACTIVA ===");
            System.out.println("[1] CERRAR ACCESO (No entran nuevos)");
            System.out.println("[2] ABRIR ACCESO  (Vuelven a entrar)");
            System.out.println("[3] MANTENIMIENTO ON  (Cortar mensajes)");
            System.out.println("[4] MANTENIMIENTO OFF (Reanudar mensajes)");
            System.out.println("[5] VER ESTADÍSTICAS DEL SERVIDOR");
            
            while (true) {
                String comando = scanner.nextLine().trim();
                switch (comando) {
                    case "1":
                        aceptarConexiones = false;
                        System.out.println("⛔ El servidor ya NO acepta nuevas conexiones.");
                        break;
                    case "2":
                        aceptarConexiones = true;
                        System.out.println("✅ El servidor vuelve a aceptar conexiones.");
                        break;
                    case "3":
                        mantenimiento = true;
                        System.out.println("🛠️ Mantenimiento ACTIVADO. Mensajería pausada.");
                        break;
                    case "4":
                        mantenimiento = false;
                        System.out.println("▶️ Mantenimiento DESACTIVADO. Mensajería reanudada.");
                        break;
                    case "5":
                        mostrarEstadisticas();
                        break;
                    default:
                        System.out.println("❌ Comando no reconocido. Usa 1, 2, 3, 4 o 5.");
                        break;
                }
            }
        }).start();
    }

   
    private void mostrarEstadisticas() {
        System.out.println("\n--- ESTADÍSTICAS DEL SERVIDOR ---");
        int usuariosOnline = 0;
        java.util.Map<String, Integer> usuariosPorSalon = new java.util.HashMap<>();

       
        for (ManejadorCliente c : clientesConectados) {
            if (c.autenticado) {
                usuariosOnline++;
                String salon = c.salonActivo;
                usuariosPorSalon.put(salon, usuariosPorSalon.getOrDefault(salon, 0) + 1);
            }
        }

        System.out.println("Usuarios conectados ahora mismo: " + usuariosOnline);
        System.out.println("Usuarios por salón activo:");
        if (usuariosPorSalon.isEmpty()) System.out.println(" - Ninguno");
        for (String salon : usuariosPorSalon.keySet()) {
            System.out.println(" - " + salon + ": " + usuariosPorSalon.get(salon) + " usuario(s)");
        }

        System.out.println("Total de mensajes enviados por salón:");
        if (mensajesPorSalon.isEmpty()) {
            System.out.println(" - Aún no hay mensajes.");
        } else {
            for (String salon : mensajesPorSalon.keySet()) {
                System.out.println(" - " + salon + ": " + mensajesPorSalon.get(salon) + " mensaje(s)");
            }
        }
        System.out.println("---------------------------------\n");
    }

    public void iniciar() {
        cargarUsuariosDesdeCSV();
        iniciarConsolaAdmin(); 

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor de chat iniciado en el puerto " + PUERTO);
            
            while (true) {
                Socket socketCliente = serverSocket.accept();
                
               
                if (!aceptarConexiones) {
                    System.out.println("Rechazando conexión (Acceso cerrado): " + socketCliente.getInetAddress());
                    PrintWriter salidaTemp = new PrintWriter(socketCliente.getOutputStream(), true);
                    salidaTemp.println("MSG_ERR|El servidor está cerrado temporalmente.");
                    socketCliente.close();
                    continue; 
                }

                System.out.println("Nueva conexion: " + socketCliente.getInetAddress());
                ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                clientesConectados.add(manejador);
                new Thread(manejador).start();
            }
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ServidorChat().iniciar();
    }
}