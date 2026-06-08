package Cliente;

import Servidor.EnrutadorComandos;
import ServidorInterfaces.IClienteConectado;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Componente del lado del servidor que encapsula el estado físico y lógico de una sesión activa,
 * encargándose de las notificaciones de desconexión forzosa y lectura de flujos.
 */
public class ManejadorClientes implements Runnable, IClienteConectado {
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    public String nombreUsuario = "Anonimo";
    public boolean autenticado = false;
    private boolean yaDesconectado = false;
    public Set<String> salonesActivos = Collections.synchronizedSet(new HashSet<>());

    public ManejadorClientes(Socket socket) {
        this.socket = socket;
    }

    /**
     * Transmite de forma síncrona una línea de texto formateada directamente al buffer de red del cliente.
     * @param mensaje Trama de caracteres con el protocolo o texto a mostrar.
     */
    @Override
    public void enviarMensaje(String mensaje) {
        if (salida != null) salida.println(mensaje);
    }

    /**
     * Recupera el identificador textual de la sesión actual de usuario.
     * @return El nombre de usuario en formato cadena.
     */
    @Override
    public String getNombreUsuario() { return this.nombreUsuario; }

    /**
     * Asigna un identificador formal de sesión al cliente conectado.
     * @param nombre Nombre del usuario autenticado.
     */
    @Override
    public void setNombreUsuario(String nombre) { this.nombreUsuario = nombre; }

    /**
     * Informa sobre el estado de autenticación del cliente en el servidor.
     * @return true si pasó el login correctamente, false si es anónimo.
     */
    @Override
    public boolean isAutenticado() { return this.autenticado; }

    /**
     * Fuerza el cambio de estado de validez de la sesión actual.
     * @param estado Nuevo valor lógico de autenticación.
     */
    @Override
    public void setAutenticado(boolean estado) { this.autenticado = estado; }

    /**
     * Obtiene el listado de las salas de chat públicas o privadas que el usuario tiene abiertas.
     * @return Conjunto sincronizado de cadenas con los salones activos.
     */
    @Override
    public Set<String> getSalonesActivos() { return this.salonesActivos; }

    @Override
    public void run() {
        try {
            EnrutadorComandos enrutador = new EnrutadorComandos(
            Servidor.ServidorChat.obtenerGestorUsuarios(), 
            Servidor.ServidorChat.obtenerGestorSalones(),
            Servidor.ServidorChat.obtenerGestorMensajes()
            );
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            
        
            String mensajeCrudo;
            while ((mensajeCrudo = entrada.readLine()) != null) {
                mensajeCrudo = mensajeCrudo.trim();
                if (mensajeCrudo.isEmpty()) continue;
                
                
                enrutador.procesar(mensajeCrudo, this);
            }
        } catch (IOException e) {
            System.out.println("Pérdida de socket con el cliente.");
        } finally {
            cerrarConexion();
        }
    }

    @Override
    public void cerrarConexion() {
        try {
            if (yaDesconectado) return;
            yaDesconectado = true;
            Servidor.GestorLogs.registrar("servidor.log", "Cliente desconectado: " + this.nombreUsuario);
            Servidor.ServidorChat.clientesConectados.remove(this);
            if (autenticado) {
                Servidor.ServidorChat.difundirNotificacion("NOTIFY_LEAVE|" + this.nombreUsuario, this);
            }
            if (socket != null && !socket.isClosed()) socket.close();
            Servidor.ServidorChat.imprimirMetricas();
        } catch (IOException e) {}
    }
}