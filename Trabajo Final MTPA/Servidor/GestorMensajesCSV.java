package Servidor;

import ServidorInterfaces.IServicioMensajes;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorMensajesCSV implements IServicioMensajes {
    // Si tienes problemas de rutas en VS Code, recuerda que puedes poner "src/mensajes.csv"
    private static final String ARCHIVO_MENSAJES = "mensajes.csv";

    @Override
    public synchronized void guardarMensaje(String salon, String usuario, String fechaHora, String contenido) {
        // Solo se guarda lo que entra aquí (y solo entran los MSG_ROOM)
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_MENSAJES, true))) {
            pw.println(fechaHora + "|" + salon + "|" + usuario + "|" + contenido);
        } catch (IOException e) {
            System.err.println("[Mensajes] Error al guardar mensaje: " + e.getMessage());
        }
    }

    @Override
    public synchronized List<String> obtenerMensajesDeHoy(String salon) {
        List<String> deHoy = new ArrayList<>();
        String hoy = LocalDate.now().toString(); 
        File archivo = new File(ARCHIVO_MENSAJES);
        if (!archivo.exists()) return deHoy;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length >= 4) {
                    String fechaHora = partes[0];
                    String sln = partes[1];
                    String usr = partes[2];
                    String txt = partes[3];
                    
                    // Solo cogemos los mensajes de ESTA sala y de HOY
                    if (sln.equals(salon) && fechaHora.startsWith(hoy)) {
                        deHoy.add("ROOM_BROADCAST|" + salon + "|" + usr + "|" + fechaHora + "|" + txt);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Mensajes] Error al leer mensajes de hoy: " + e.getMessage());
        }
        return deHoy;
    }

    @Override
    public synchronized List<String> obtenerHistorialAnterior(String salon, int offset, int cantidad) {
        List<String> historiales = new ArrayList<>();
        String hoy = LocalDate.now().toString();
        File archivo = new File(ARCHIVO_MENSAJES);
        if (!archivo.exists()) return historiales;

        List<String> todosLosHistoricos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length >= 4) {
                    String fechaHora = partes[0];
                    String sln = partes[1];
                    String usr = partes[2];
                    String txt = partes[3];
                    
                    // Solo cogemos los mensajes de ESTA sala que NO sean de hoy
                    if (sln.equals(salon) && !fechaHora.startsWith(hoy)) {
                        todosLosHistoricos.add("HIST_DATA|" + salon + "|" + usr + "|" + fechaHora + "|" + txt);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Mensajes] Error al leer historial: " + e.getMessage());
        }

        // Lógica para paginar de 50 en 50 hacia atrás
        int total = todosLosHistoricos.size();
        int inicio = total - 1 - offset;
        int fin = Math.max(0, inicio - cantidad + 1);

        for (int i = inicio; i >= fin; i--) {
            if (i >= 0 && i < total) {
                historiales.add(todosLosHistoricos.get(i));
            }
        }
        return historiales;
    }
}