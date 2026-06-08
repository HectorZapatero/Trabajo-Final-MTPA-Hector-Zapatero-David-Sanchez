package Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConexionServidor {
    private Socket socket;
    private PrintWriter salida
    private BufferedReader entrada;

    public void conectar(String ip, int puerto) throws IOException {
        this.socket = new Socket(ip, puerto);
        this.salida = new PrintWriter(socket.getOutputStream(), true);
        this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void enviarComando(String comando) {
        if (salida != null) {
            salida.println(comando);
        }
    }

    public void escucharServidor(java.util.function.Consumer<String> alRecibirMensaje) {
        new Thread(() -> {
            try {
                String linea;
                while ((linea = entrada.readLine()) != null) {
                    alRecibirMensaje.accept(linea);
                }
            } catch (IOException e) {
                System.out.println("Conexión perdida con el servidor.");
            }
        }).start();
    }

    private Thread hiloHeartbeat;
    private volatile boolean ejecutarHeartbeat = false;
    private static final int TIEMPO_LATIDO_MS = 60000; 

    public void iniciarSistemaHeartbeat() {
        detenerSistemaHeartbeat();
        ejecutarHeartbeat = true;
        
        Runnable tareaLatido = new Runnable() {
            @Override
            public void run() {
                try {
                    while (ejecutarHeartbeat && !socket.isClosed()) {
                        if (salida != null) {
                            salida.println("PING");
                            salida.flush();
                        }
                        Thread.sleep(TIEMPO_LATIDO_MS);
                    }
                } catch (InterruptedException e) {
                    // Hilo interrumpido pacíficamente
                } catch (Exception e) {
                    detenerSistemaHeartbeat();
                }
            }
        };

        hiloHeartbeat = new Thread(tareaLatido, "Hilo-Heartbeat-Cliente");
        hiloHeartbeat.setDaemon(true);
        hiloHeartbeat.start();
    }

    public void detenerSistemaHeartbeat() {
        ejecutarHeartbeat = false;
        if (hiloHeartbeat != null && hiloHeartbeat.isAlive()) {
            hiloHeartbeat.interrupt();
        }
    }
}