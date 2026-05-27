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
public class ServidorChat {
    private static final int PUERTO = 5000;
    public static final List<IClienteConectado> clientesConectados = Collections.synchronizedList(new ArrayList<>());
    private static final IServicioMensajes gestorMensajes = new GestorMensajesCSV();
    public static IServicioMensajes obtenerGestorMensajes() { return gestorMensajes; }
    
    public static volatile boolean aceptarConexiones = true;
    public static volatile boolean mantenimiento = false;

    private static final IServicioUsuarios gestorUsuarios = (IServicioUsuarios) new GestorUsuariosCSV();
    private static final IServicioSalones gestorSalones = (IServicioSalones) new GestorSalonesMemoria();

    public static IServicioUsuarios obtenerGestorUsuarios() { return gestorUsuarios; }
    public static IServicioSalones obtenerGestorSalones() { return gestorSalones; }

    public static void difundirNotificacion(String trama, IClienteConectado emisor) {
        synchronized (clientesConectados) {
            for (IClienteConectado cliente : clientesConectados) {
                if (cliente != emisor && cliente.isAutenticado()) {
                    cliente.enviarMensaje(trama);
                }
            }
        }
    }

    public static void difundirPorSala(String salon, String trama) {
        synchronized (clientesConectados) {
            for (IClienteConectado cliente : clientesConectados) {
                if (cliente.isAutenticado() && cliente.getSalonesActivos().contains(salon)) {
                    cliente.enviarMensaje(trama);
                }
            }
        }
    }

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
                } else if (comando.equals("maintenance on")) {
                    mantenimiento = true;
                    System.out.println(" Mantenimiento activado. Expulsando clientes...");
                 
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

    private void imprimirMetricas() {
        System.out.println("\n--- ESTADÍSTICAS ---");
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
        System.out.println("Usuarios online: " + activos);
        System.out.println("Ventanas abiertas por sala/usuario:");
        salasActivas.forEach((sala, cant) -> System.out.println(" - " + sala + ": " + cant + " activo(s)"));
        System.out.println("Mensajes totales procesados por sala:");
        gestorSalones.obtenerMetricas().forEach((sala, cant) -> System.out.println(" - " + sala + ": " + cant + " mensajes"));
    }
    public static void difundirATodos(String trama) {
    synchronized (clientesConectados) {
        for (IClienteConectado cliente : clientesConectados) {
            cliente.enviarMensaje(trama);
        }
    }
}

    public void iniciar() {
        iniciarConsolaAdmin();
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor operativo en puerto " + PUERTO);
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
            }
        } catch (IOException e) {
            System.out.println("Error en servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ServidorChat().iniciar();
    }
}