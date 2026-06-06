package Servidor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GestorLogs {
    public static synchronized void registrar(String nombreArchivo, String mensaje) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo, true))) {
            String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println("[" + hora + "] " + mensaje);
        } catch (IOException e) {
        }
    }
}
