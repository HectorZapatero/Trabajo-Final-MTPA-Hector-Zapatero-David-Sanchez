package Servidor;

import ServidorInterfaces.IServicioMensajes;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de mensajes encargado de persistir las conversaciones
 * en archivos CSV y de gestionar las lecturas selectivas del historial y paginaciones.
 */
public class GestorMensajesCSV implements IServicioMensajes {
    private static final String ARCHIVO_MENSAJES = "mensajes.csv";

    /**
     * Persiste un mensaje de sala en el archivo CSV general formateando los datos con separadores.
     * @param salon Nombre de la sala donde se envió el mensaje.
     * @param usuario Nombre del emisor.
     * @param fechaHora Cadena con la fecha y hora del sistema (yyyy-MM-dd HH:mm).
     * @param contenido Texto plano del mensaje enviado.
     */
    @Override
    public synchronized void guardarMensaje(String salon, String usuario, String fechaHora, String contenido) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_MENSAJES, true))) {
            pw.println(fechaHora + "|" + salon + "|" + usuario + "|" + contenido);
        } catch (IOException e) {
            System.err.println("[Mensajes] Error al guardar mensaje: " + e.getMessage());
        }
    }

    /**
     * Filtra y obtiene exclusivamente los mensajes enviados durante la fecha actual para un salón determinado.
     * @param salon Sala de chat a consultar.
     * @return Lista de cadenas formateadas bajo el protocolo ROOM_BROADCAST.
     */
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

    /**
     * Lee el archivo histórico omitiendo los mensajes de hoy y extrae un bloque paginado hacia atrás.
     * @param salon Sala de chat a consultar.
     * @param offset Número de mensajes antiguos a saltar en la paginación.
     * @param cantidad Volumen máximo de registros a recuperar en la lectura actual.
     * @return Lista de mensajes históricos formateados bajo el protocolo HIST_DATA.
     */
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
                    
                    if (sln.equals(salon) && !fechaHora.startsWith(hoy)) {
                        todosLosHistoricos.add("HIST_DATA|" + salon + "|" + usr + "|" + fechaHora + "|" + txt);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Mensajes] Error al leer historial: " + e.getMessage());
        }

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