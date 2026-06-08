package Servidor;

import Cliente.ManejadorClientes;
import ServidorInterfaces.IClienteConectado;
import ServidorInterfaces.IServicioMensajes;
import ServidorInterfaces.IServicioSalones;
import ServidorInterfaces.IServicioUsuarios;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Servidor principal del chat que gestiona el ciclo de vida de las conexiones,
 * la difusión de mensajes por salas y el estado de mantenimiento del sistema.
 */
public class ServidorChat {
    private static final int PUERTO = 5000;
    public static final List<IClienteConectado> clientesConectados = Collections.synchronizedList(new ArrayList<>());
    private static final IServicioMensajes gestorMensajes = new GestorMensajesCSV();
    
    /**
     * Devuelve el servicio encargado de la persistencia e historial de mensajes.
     * @return Instancia del gestor de mensajes en formato CSV.
     */
    public static IServicioMensajes obtenerGestorMensajes() { return gestorMensajes; }
    
    public static volatile boolean aceptarConexiones = true;
    public static volatile boolean mantenimiento = false;

    private static final IServicioUsuarios gestorUsuarios = (IServicioUsuarios) new GestorUsuariosCSV();
    private static final IServicioSalones gestorSalones = (IServicioSalones) new GestorSalonesMemoria();

    /**
     * Devuelve el servicio encargado del registro y autenticación de usuarios.
     * @return Instancia del gestor de usuarios en formato CSV.
     */
    public static IServicioUsuarios obtenerGestorUsuarios() { return gestorUsuarios; }
    /**
     * Devuelve el servicio encargado del control de métricas de los salones.
     * @return Instancia del gestor de salones en memoria.
     */
    public static IServicioSalones obtenerGestorSalones() { return gestorSalones; }

    /**
     * Difunde una trama de notificación a todos los usuarios autenticados excepto al emisor.
     * @param trama Cadena de texto con el protocolo de notificación (ej. NOTIFY_JOIN).
     * @param emisor Cliente conectado que origina la notificación.
     */
    public static void difundirNotificacion(String trama, IClienteConectado emisor) {
        synchronized (clientesConectados) {
            for (IClienteConectado cliente : clientesConectados) {
                if (cliente != emisor && cliente.isAutenticado()) {
                    cliente.enviarMensaje(trama);
                }
            }
        }
    }

    /**
     * Difunde un mensaje a todos los usuarios que tengan el salón activo en sus pantallas.
     * @param salon Nombre de la sala de chat de destino.
     * @param trama Cadena de texto formateada con el mensaje y metadatos.
     */
    public static void difundirPorSala(String salon, String trama) {
        synchronized (clientesConectados) {
            for (IClienteConectado cliente : clientesConectados) {
                if (cliente.isAutenticado() && cliente.getSalonesActivos().contains(salon)) {
                    cliente.enviarMensaje(trama);
                }
            }
        }
    }

    /**
     * Intenta enviar un mensaje privado directo a un usuario específico mediante socket en memoria viva.
     * @param remitente Nombre del usuario que envía el mensaje.
     * @param destino Nombre del usuario que debería recibir el mensaje.
     * @param msg Contenido textual del mensaje privado.
     * @return true si el usuario destino está conectado y autenticado, false en caso contrario.
     */
    public static boolean enviarPrivadoDirecto(String remitente, String destino, String msg) {
        synchronized (clientesConectados) {
            for (IClienteConectado c : clientesConectados) {
                if (c.isAutenticado() && c.getNombreUsuario().equals(destino)) {
                    c.enviarMensaje("RECV_PRIV|" + remitente + "|" + msg);
                    return true;
                }
            }
        }
        return false;
    }

  private static void iniciarConsolaAdmin() {
        new Thread(() -> {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            System.out.println("\n=== CONSOLA DE ADMINISTRACIÓN ACTIVA ===");
            System.out.println("Escribe 'maintenance on' para expulsar a todos.");
            
            while (true) {
                String comando = scanner.nextLine().trim().toLowerCase(); 
                
                if (comando.equals("1")) {
                    aceptarConexiones = false;
                    System.out.println(" Accesos cerrados.");
                } else if (comando.equals("2")) {
                    aceptarConexiones = true;
                    System.out.println(" Accesos abiertos.");
                } else if (comando.equals("stats") || comando.equals("3")) {
                    imprimirMetricas();
                } else if (comando.equals("maintenance on")) {
                    mantenimiento = true;
                    System.out.println(" Mantenimiento activado. Expulsando clientes...");
                    GestorLogs.registrar("servidor.log", "¡ALERTA! El administrador ha ACTIVADO el modo mantenimiento.");
                    difundirATodos("MAINTENANCE|ON");
                    
                } else if (comando.equals("maintenance off")) {
                    mantenimiento = false;
                    System.out.println(" Mantenimiento desactivado.");
                
                    difundirATodos("MAINTENANCE|OFF");
                } else {
                    System.out.println("Comando desconocido.");
                }
            }
        }).start();
    }

    /**
     * Calcula y muestra por la terminal del servidor las métricas en tiempo real:
     * usuarios online totales, usuarios activos por sala y mensajes acumulados por salón.
     */
    public static synchronized void imprimirMetricas() {
        System.out.println("--- ESTADÍSTICAS EN TIEMPO REAL ---");
        int activos = 0;
        java.util.Map<String, Integer> salasActivas = new java.util.HashMap<>();
        
        synchronized (clientesConectados) {
            activos = clientesConectados.size();
            for (IClienteConectado c : clientesConectados) {
                for (String sala : c.getSalonesActivos()) {
                    salasActivas.put(sala, salasActivas.getOrDefault(sala, 0) + 1);
                }
            }
        }
        System.out.println("Usuarios conectados al instante: " + activos);
        System.out.println("Usuarios activos por salón:");
        String[] salonesSoportados = {"IA", "Deportes", "Manga", "Therian", "UEMC"};
        for (String s : salonesSoportados) {
            int cant = salasActivas.getOrDefault(s, 0);
            System.out.println(" - " + s + ": " + cant + " activo(s)");
        }
        System.out.println("Mensajes totales enviados por salón:");
        for (String s : salonesSoportados) {
            int cant = gestorSalones.obtenerMetricas().getOrDefault(s, 0);
            System.out.println(" - " + s + ": " + cant + " mensaje(s)");
        }
    }
    /**
     * Difunde una trama de control o texto a absolutamente todos los clientes conectados en el socket.
     * @param trama Cadena de texto con el protocolo o mensaje para los clientes.
     */
    public static void difundirATodos(String trama) {
    synchronized (clientesConectados) {
        for (IClienteConectado cliente : clientesConectados) {
            cliente.enviarMensaje(trama);
        }
    }
}

    /**
     * Arranca el bucle principal del servidor, inicializa la consola de administración
     * y acepta conexiones entrantes de sockets asociándoles un hilo dedicado.
     */
    public void iniciar() {
        iniciarConsolaAdmin();
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor operativo en puerto " + PUERTO);
            GestorLogs.registrar("servidor.log", "SERVIDOR INICIADO EN PUERTO " + PUERTO);
            while (true) {
                Socket socketCliente = serverSocket.accept();
                if (!aceptarConexiones) {
                    PrintWriter salidaTemp = new PrintWriter(socketCliente.getOutputStream(), true);
                    salidaTemp.println("LOGIN_ERR|Credenciales_invalidas");
                    socketCliente.close();
                    continue;
                }
                ManejadorClientes manejador = new ManejadorClientes(socketCliente);
                clientesConectados.add(manejador);
                new Thread(manejador).start();
                imprimirMetricas();
            }
        } catch (IOException e) {
            System.out.println("Error en servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ServidorChat().iniciar();
    }
}