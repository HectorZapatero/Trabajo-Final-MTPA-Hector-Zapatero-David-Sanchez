package Cliente;

import Servidor.ServidorChat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    public String nombreUsuario = "Anonimo";
    public boolean autenticado = false;
    private boolean yaDesconectado = false;
    
    // --- VARIABLE DEL HITO 2: Saber en qué salón está participando ---
    public String salonActivo = "Ninguno";

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    public void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }

    @Override
    public void run() {
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            String mensajeCrudo;
            while ((mensajeCrudo = entrada.readLine()) != null) {
                mensajeCrudo = mensajeCrudo.trim();
                if (mensajeCrudo.isEmpty()) {
                    continue;
                }
                System.out.println("Recibido del cliente: " + mensajeCrudo);
                String[] partes = mensajeCrudo.split("\\|");
                String comando = partes[0]; 

                switch (comando) {
                    case "REG":
                        if (partes.length >= 2) {
                            String nuevoUsuario = partes[1];
                            if (ServidorChat.usuariosRegistrados.containsKey(nuevoUsuario)) {
                                salida.println("REG_ERR|Usuario ya registrado");
                            } else {
                                int numeroAleatorio = (int) (Math.random() * 9000 + 1000);
                                String claveAutonumerica = String.valueOf(numeroAleatorio);
                                ServidorChat.usuariosRegistrados.put(nuevoUsuario, claveAutonumerica);
                                guardarEnCSV(nuevoUsuario, claveAutonumerica);
                                salida.println("REG_OK|" + claveAutonumerica);
                                System.out.println("Registro exitoso ---> Usuario " + nuevoUsuario + "|Clave: " + claveAutonumerica);
                            }
                        } else {
                            salida.println("REG_ERR|Formato incorrecto");
                        }
                        break;
                        
                    case "LOGIN":
                        if (partes.length >= 3) {
                            String usuario = partes[1];
                            String clave = partes[2];
                            if (ServidorChat.usuariosRegistrados.containsKey(usuario) && ServidorChat.usuariosRegistrados.get(usuario).equals(clave)) {
                                this.nombreUsuario = usuario;
                                this.autenticado = true;
                                salida.println("LOGIN_OK|IA,DEPORTES,THERIAN,MANGA,UEMC");
                                System.out.println(usuario + " ha iniciado sesion ");
                                String notificacionEntrada = "NOTIFY_JOIN|" + this.nombreUsuario;
                                for (ManejadorCliente cliente : ServidorChat.clientesConectados) {
                                    if (cliente != this && cliente.autenticado) {
                                        cliente.enviarMensaje(notificacionEntrada);
                                    }
                                }
                            } else {
                                salida.println("LOGIN_ERR|Credenciales_incorrectas");
                            }
                        }
                        break;

                    case "MSG_ROOM":
                        // --- FRENO DE MANTENIMIENTO ---
                        if (ServidorChat.mantenimiento) {
                            this.enviarMensaje("MSG_ERR|🛠️ El servidor está en mantenimiento. Mensajes pausados.");
                            break;
                        }

                        if (partes.length >= 3 && autenticado) {
                            String salon = partes[1];
                            String contenido = partes[2];

                            // --- ESTADÍSTICAS: Actualizamos salón activo y sumamos mensaje ---
                            this.salonActivo = salon;
                            ServidorChat.mensajesPorSalon.put(salon, ServidorChat.mensajesPorSalon.getOrDefault(salon, 0) + 1);

                            if (contenido.length() > 190) {
                                contenido = contenido.substring(0, 190);
                            }
                            String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                            String difusion = "ROOM_BROADCAST|" + salon + "|" + this.nombreUsuario + "|" + fechaHora + "|" + contenido;
                            
                            for (ManejadorCliente cliente : ServidorChat.clientesConectados) {
                                cliente.enviarMensaje(difusion);
                            }
                        }
                        break;
                        
                    case "LIST_USERS":
                        if (autenticado) {
                            StringBuilder nombres = new StringBuilder();
                            for (ManejadorCliente c : ServidorChat.clientesConectados) {
                                if (c.autenticado) {
                                    nombres.append(c.nombreUsuario).append(",");
                                }
                            }
                            salida.println("USER_LIST|" + nombres.toString());
                        }
                        break;

                    case "LOGOUT":
                        this.autenticado = false;
                        cerrarConexion();
                        break;
                        
                    case "MSG_PRIV":
                        // --- FRENO DE MANTENIMIENTO ---
                        if (ServidorChat.mantenimiento) {
                            this.enviarMensaje("MSG_ERR|🛠️ El servidor está en mantenimiento. DMs pausados.");
                            break;
                        }

                        if (autenticado && partes.length >= 3) {
                            String destinatario = partes[1];
                            String mensajePrivado = partes[2];
                            boolean encontrado = false;
                            
                            this.salonActivo = "Privados"; // Actualizamos su estado para las estadísticas

                            String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                            for (ManejadorCliente c : ServidorChat.clientesConectados) {
                                if (c.autenticado && c.nombreUsuario.equals(destinatario)) {
                                    c.enviarMensaje("RECV_PRIV|" + this.nombreUsuario + "|Privado|" + fechaHora + "|" + mensajePrivado);
                                    encontrado = true;
                                    this.enviarMensaje("RECV_PRIV|Tú|Privado para " + destinatario + "|" + fechaHora + "|" + mensajePrivado);
                                    break;
                                }
                            }

                            if (!encontrado) {
                                this.enviarMensaje("MSG_ERR|El usuario " + destinatario + " no está conectado o no existe.");
                            }
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado.");
        } finally {
            cerrarConexion();
        }
    }

    private void guardarEnCSV(String usuario, String claveAutonumerica) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("usuarios.csv", true))) {
            pw.println(usuario + "," + claveAutonumerica);
        } catch (IOException e) {
            System.err.println("Error al escribir en el csv:" + e.getMessage());
        }
    }

    private void cerrarConexion() {
        try {
            if (yaDesconectado) return;
            yaDesconectado = true;
            ServidorChat.clientesConectados.remove(this);
            if (autenticado) {
                String notificacionSalida = "NOTIFY_LEAVE|" + this.nombreUsuario;
                for (ManejadorCliente cliente : ServidorChat.clientesConectados) {
                    if (cliente.autenticado) {
                        cliente.enviarMensaje(notificacionSalida);
                    }
                }
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Sesion cerrada para " + nombreUsuario);
            }
        } catch (IOException e) {}
    }
}