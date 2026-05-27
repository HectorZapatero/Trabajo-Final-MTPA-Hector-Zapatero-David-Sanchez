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

    @Override
    public void enviarMensaje(String mensaje) {
        if (salida != null) salida.println(mensaje);
    }

    @Override
    public String getNombreUsuario() { return this.nombreUsuario; }

    @Override
    public void setNombreUsuario(String nombre) { this.nombreUsuario = nombre; }

    @Override
    public boolean isAutenticado() { return this.autenticado; }

    @Override
    public void setAutenticado(boolean estado) { this.autenticado = estado; }

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
            Servidor.ServidorChat.clientesConectados.remove(this);
            if (autenticado) {
                Servidor.ServidorChat.difundirNotificacion("NOTIFY_LEAVE|" + this.nombreUsuario, this);
            }
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {}
    }
}