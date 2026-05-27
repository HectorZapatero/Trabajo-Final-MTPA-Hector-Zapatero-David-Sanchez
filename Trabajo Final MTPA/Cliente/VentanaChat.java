package Cliente;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;

public class VentanaChat extends JFrame {
    private JTextArea areaChat; 
    private JTextField campoMensaje;
    private ConexionServidor conexion;
    private ConcurrentHashMap<String, VentanaCanal> ventanasAbiertas = new ConcurrentHashMap<>();

    public VentanaChat() {
        setTitle("UEMC Chat - Terminal Principal");
        setSize(700, 400); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel etiquetaSalones = new JLabel(" SALONES DISPONIBLES: IA | Deportes | Manga | Therian | UEMC | Mensajes Privados");
        etiquetaSalones.setFont(new Font("Arial", Font.BOLD, 13));
        etiquetaSalones.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaSalones.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(etiquetaSalones, BorderLayout.NORTH);

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setBackground(Color.WHITE); 
        areaChat.setForeground(Color.BLACK); 
        areaChat.setFont(new Font("Consolas", Font.PLAIN, 14));
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        JButton botonEnviar = new JButton("Ejecutar");
        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        conectarAlServidor();

        botonEnviar.addActionListener(e -> enviarComandoTerminal());
        campoMensaje.addActionListener(e -> enviarComandoTerminal());
    }

    private void abrirVentana(String nombreCanal) {
        if (!ventanasAbiertas.containsKey(nombreCanal)) {
            VentanaCanal nuevaVentana = new VentanaCanal(nombreCanal, conexion, () -> {
                ventanasAbiertas.remove(nombreCanal);
                if (!nombreCanal.startsWith("Priv_")) {
                    conexion.enviarComando("LEAVE_ROOM|" + nombreCanal); 
                }
                String nombreMostrar = nombreCanal.startsWith("Priv_") ? "Privado con " + nombreCanal.substring(5) : nombreCanal;
                areaChat.append(">> Has cerrado el canal: " + nombreMostrar + "\n");
            });
            ventanasAbiertas.put(nombreCanal, nuevaVentana);
            nuevaVentana.setLocationRelativeTo(this);
            nuevaVentana.setVisible(true);
            
            if (!nombreCanal.startsWith("Priv_")) {
                conexion.enviarComando("JOIN_ROOM|" + nombreCanal); 
            }
            String nombreMostrar = nombreCanal.startsWith("Priv_") ? "Privado con " + nombreCanal.substring(5) : nombreCanal;
            areaChat.append(">> Canal abierto: " + nombreMostrar + "\n");
        }
    }

    private void enviarComandoTerminal() {
        String texto = campoMensaje.getText().trim();
        if (!texto.isEmpty()) {
            if (texto.startsWith("MSG_ROOM|")) {
                String[] partes = texto.split("\\|");
                if (partes.length >= 3) {
                    abrirVentana(partes[1]); 
                }
                conexion.enviarComando(texto);
            } else if (texto.startsWith("MSG_PRIV|")) {
                String[] partes = texto.split("\\|");
                if (partes.length >= 3) {
                    String destino = partes[1];
                    String msg = partes[2];
                    abrirVentana("Priv_" + destino);
                    ventanasAbiertas.get("Priv_" + destino).mostrarMensaje("Tú: " + msg);
                }
                conexion.enviarComando(texto);
            } else {
                conexion.enviarComando(texto);
            }
            campoMensaje.setText("");
        }
    }

    private void conectarAlServidor() {
        try {
            conexion = new ConexionServidor();
            conexion.conectar("127.0.0.1", 5000);
            
            conexion.escucharServidor(mensajeCrudo -> {
                String[] partes = mensajeCrudo.split("\\|");
                String comando = partes[0];

                switch (comando) {
                    case "ROOM_BROADCAST":
                        String salon = partes[1];
                        String emisor = partes[2];
                        String fecha = partes[3];
                        String texto = partes[4];
                        if (ventanasAbiertas.containsKey(salon)) {
                            ventanasAbiertas.get(salon).mostrarMensaje("[" + fecha + "] " + emisor + ": " + texto);
                        }
                        break;

                    case "RECV_PRIV":
                        if (partes.length >= 3) {
                            String remitente = partes[1];
                            String mensajeP = partes[2];
                            abrirVentana("Priv_" + remitente);
                            ventanasAbiertas.get("Priv_" + remitente).mostrarMensaje(remitente + ": " + mensajeP);
                        }
                        break;
                        
                    case "NOTIFY_JOIN":
                        String usuarioEntrada = partes[1];
                        areaChat.append(">>> " + usuarioEntrada + " se ha conectado al chat.\n");
                        mostrarNotificacionTemporal(usuarioEntrada + " se ha unido al chat.", 3000);
                        break;

                    case "NOTIFY_LEAVE":
                        areaChat.append("<<< " + partes[1] + " ha abandonado el chat.\n");
                        break;

                    case "MAINTENANCE":
                        if (partes.length >= 2) {
                            if (partes[1].equals("ON")) {
                         
                                JOptionPane.showMessageDialog(this, "🛠️ El servidor acaba de entrar en MANTENIMIENTO.\nPor seguridad, se cerrará tu sesión actual.", "Desconexión Forzosa", JOptionPane.WARNING_MESSAGE);

                                conexion.enviarComando("LOGOUT");
                            
                                System.exit(0); 
                                
                            } else {
                                JOptionPane.showMessageDialog(this, "✅ El mantenimiento ha finalizado.\nYa puedes volver a conectarte.", "Aviso del Sistema", JOptionPane.INFORMATION_MESSAGE);
                                areaChat.append("▶️ [SISTEMA] El mantenimiento ha finalizado.\n");
                            }
                        }
                        break;

                    case "MSG_ERR":
                        if (partes.length >= 2) {
                            
                            JOptionPane.showMessageDialog(this, partes[1], "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                            areaChat.append("❌ ERROR: " + partes[1] + "\n");
                        }
                        break;
                    case "REG_OK":
                        JOptionPane.showMessageDialog(this, "¡Registro completado!\nTu clave única es: " + partes[1], "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
                        areaChat.append(">> Registro exitoso. Clave obtenida.\n");
                        break;

                    case "PRIV_ERR":
                        if (partes.length >= 2) {
                            areaChat.append(" ERROR PRIVADO: " + partes[1] + "\n");
                        }
                        break;
                   
                    case "HIST_DATA":
                        if (partes.length >= 5) {
                            String salaHist = partes[1];
                            String emisorHist = partes[2];
                            String fechaHist = partes[3];
                            String textoHist = partes[4];
                            if (ventanasAbiertas.containsKey(salaHist)) {
                                ventanasAbiertas.get(salaHist).mostrarMensaje("[HISTORIAL] [" + fechaHist + "] " + emisorHist + ": " + textoHist);
                            }
                        }
                        break;

                    default:
                        areaChat.append(">> " + mensajeCrudo + "\n");
                        areaChat.setCaretPosition(areaChat.getDocument().getLength());
                        break;
                }
            });
            
            areaChat.append(">> Conexión establecida con el servidor de la UEMC.\n");
            areaChat.append(">> Comandos: REG|nombre | LOGIN|nombre|clave | MSG_ROOM|salon|mensaje o MSG_PRIV|usuario|mensaje\n");
        } catch (Exception e) {
            areaChat.append("Error al conectar: " + e.getMessage() + "\n");
        }
    }

    private void mostrarNotificacionTemporal(String mensaje, int milisegundos) {
        JDialog dialogo = new JDialog(this, "Notificación", false); 
        dialogo.setLayout(new FlowLayout());
        dialogo.add(new JLabel("  " + mensaje + "  "));
        dialogo.pack();
        Point ubicacion = this.getLocation();
        dialogo.setLocation(ubicacion.x + this.getWidth() - dialogo.getWidth() - 10, ubicacion.y + 40);
        dialogo.setVisible(true);
        new javax.swing.Timer(milisegundos, e -> dialogo.dispose()).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaChat().setVisible(true);
        });
    }
}