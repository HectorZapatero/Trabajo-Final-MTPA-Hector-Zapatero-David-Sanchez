package Servidor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad encargada del registro centralizado de eventos y errores del sistema
 * añadiendo marcas de tiempo automáticas en archivos de texto plano (.log).
 */
public class GestorLogs {
    /**
     * Escribe una nueva línea de log en un archivo de forma síncrona y segura entre hilos.
     * @param nombreArchivo Ruta o nombre del archivo de destino (ej. servidor.log).
     * @param mensaje Texto explicativo del evento ocurrido en el sistema.
     */
    public static synchronized void registrar(String nombreArchivo, String mensaje) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo, true))) {
            String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println("[" + hora + "] " + mensaje);
        } catch (IOException e) {
        }
    }
}
