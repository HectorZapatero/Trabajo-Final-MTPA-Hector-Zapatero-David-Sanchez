package Cliente;

import java.awt.*;
import javax.swing.*;

public class VentanaChat extends JFrame {
    // --- VARIABLES DEL CHAT PÚBLICO ---
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private DefaultListModel<String> modeloSalones;
    private JList<String> listaSalones; 
    private ConexionServidor conexion;

    // --- NUEVAS VARIABLES PARA MENSAJES PRIVADOS (LA INTERFAZ) ---
    private JTextArea areaPrivados;
    private JTextField campoDestinatario;
    private JTextField campoMensajePrivado;
    private JButton btnEnviarPrivado;

    public VentanaChat() {
        setTitle("UEMC Chat - Cliente");
        setSize(650, 450); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- CREACIÓN DEL SISTEMA DE PESTAÑAS ---
        JTabbedPane sistemaPestanas = new JTabbedPane();

        
        // PESTAÑA 1: SALONES PÚBLICOS
      
        JPanel panelPublico = new JPanel(new BorderLayout());

        modeloSalones = new DefaultListModel<>();
        modeloSalones.addElement("IA");
        modeloSalones.addElement("Deportes");
        modeloSalones.addElement("Manga");
        modeloSalones.addElement("Therian");
        modeloSalones.addElement("UEMC");
        listaSalones = new JList<>(modeloSalones);
        listaSalones.setSelectedIndex(0); 
        panelPublico.add(new JScrollPane(listaSalones), BorderLayout.WEST);

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        panelPublico.add(new JScrollPane(areaChat), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        JButton botonEnviar = new JButton("Enviar");
        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);
        panelPublico.add(panelInferior, BorderLayout.SOUTH);

        
        // PESTAÑA 2: MENSAJES PRIVADOS
        
        JPanel panelPrivados = crearPanelPrivados();

        // Añadimos las dos pestañas a la ventana
        sistemaPestanas.addTab("Salones Públicos", panelPublico);
        sistemaPestanas.addTab("Mensajes Privados", panelPrivados);
        add(sistemaPestanas, BorderLayout.CENTER);

        // --- LÓGICA DE CONEXIÓN ---
        conectarAlServidor();

        // Acciones al pulsar Enviar en el chat público
        botonEnviar.addActionListener(e -> enviarMensaje());
        campoMensaje.addActionListener(e -> enviarMensaje());
    }

    // --- FABRICAMOS EL PANEL DE PRIVADOS (LA NUEVA INTERFAZ) ---
    private JPanel crearPanelPrivados() {
        JPanel panel = new JPanel(new BorderLayout());

        areaPrivados = new JTextArea();
        areaPrivados.setEditable(false);
        panel.add(new JScrollPane(areaPrivados), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panelInferior.add(new JLabel("Para (Nombre):"));
        campoDestinatario = new JTextField(8);
        panelInferior.add(campoDestinatario);

        panelInferior.add(new JLabel("Mensaje:"));
        campoMensajePrivado = new JTextField(20);
        panelInferior.add(campoMensajePrivado);

        btnEnviarPrivado = new JButton("Enviar DM");
        panelInferior.add(btnEnviarPrivado);

        btnEnviarPrivado.addActionListener(e -> enviarMensajePrivado());
        campoMensajePrivado.addActionListener(e -> enviarMensajePrivado());

        panel.add(panelInferior, BorderLayout.SOUTH);
        return panel;
    }

    // --- MÉTODO PARA ENVIAR EL PRIVADO ---
    private void enviarMensajePrivado() {
        String destinatario = campoDestinatario.getText().trim();
        String texto = campoMensajePrivado.getText().trim();

        if (destinatario.isEmpty() || texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe a quién va dirigido y el mensaje.");
            return;
        }

        // Enviamos el comando al servidor
        conexion.enviarComando("MSG_PRIV|" + destinatario + "|" + texto);
        campoMensajePrivado.setText(""); 
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
                        areaChat.append("[" + fecha + "] " + emisor + " (" + salon + "): " + texto + "\n");
                        areaChat.setCaretPosition(areaChat.getDocument().getLength());
                        break;
                        
                    case "REG_OK":
                        JOptionPane.showMessageDialog(this, 
                            "¡Registro completado!\nTu clave única es: " + partes[1] + "\nApúntala para iniciar sesión.", 
                            "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
                        break;
                        
                    case "REG_ERR":
                        JOptionPane.showMessageDialog(this, "Error al registrar: " + partes[1], "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                        
                    case "LOGIN_OK":
                        areaChat.append("--- HAS INICIADO SESIÓN CORRECTAMENTE ---\n");
                        break;
                        
                    case "LOGIN_ERR":
                        JOptionPane.showMessageDialog(this, "Error de Login: " + partes[1], "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                        break;
                        
                    case "NOTIFY_JOIN":
                        String usuarioEntrada = partes[1];
                        areaChat.append(">>> " + usuarioEntrada + " se ha conectado al chat.\n");
                        areaChat.setCaretPosition(areaChat.getDocument().getLength());
                        mostrarNotificacionTemporal(usuarioEntrada + " se ha unido al chat.", 3000);
                        break;

                    case "NOTIFY_LEAVE":
                        String usuarioSalida = partes[1];
                        areaChat.append("<<< " + usuarioSalida + " ha abandonado el chat.\n");
                        areaChat.setCaretPosition(areaChat.getDocument().getLength());
                        break;
                        
                    case "USERS_LIST":
                        String listaConectados = partes[1];
                        areaChat.append("--- USUARIOS EN LÍNEA: " + listaConectados + "---\n");
                        areaChat.setCaretPosition(areaChat.getDocument().getLength());
                         break;

                    // --- RECIBIR MENSAJES PRIVADOS ---
                    case "RECV_PRIV":
                        if (partes.length >= 5) {
                            String remitente = partes[1];
                            String contextoSalon = partes[2]; // Aquí vendrá "Privado" o "Privado para Ana"
                            String fechaP = partes[3];
                            String mensajeP = partes[4];
                            
                            // Lo imprimimos en la pestaña de DMs con el formato exacto que pediste
                            areaPrivados.append("[" + fechaP + "] " + remitente + " (" + contextoSalon + "): " + mensajeP + "\n");
                            areaPrivados.setCaretPosition(areaPrivados.getDocument().getLength());
                            
                        if (!remitente.equals("Tú")) {
                            mostrarNotificacionTemporal("🔔 ¡Nuevo mensaje privado de " + remitente + "!", 3000);
                        }
                        }
                        break;

                    case "MSG_ERR":
                        if (partes.length >= 2) {
                            areaPrivados.append("❌ ERROR: " + partes[1] + "\n");
                            areaPrivados.setCaretPosition(areaPrivados.getDocument().getLength());
                        }
                        break;

                    default:
                        areaChat.append("Sistema: " + mensajeCrudo + "\n");
                        break;
                        
                }
            });
            
            areaChat.append("Conectado al servidor. Escribe REG|tu_nombre para registrarte, o LOGIN|tu_nombre|tu_clave para entrar.\n");
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
        dialogo.setLocation(ubicacion.x + this.getWidth() - dialogo.getWidth() - 10,
                            ubicacion.y + 40);
 
        dialogo.setVisible(true);
 
        new javax.swing.Timer(milisegundos, e -> dialogo.dispose()).start();
    }

    private void enviarMensaje() {
        String texto = campoMensaje.getText().trim();
        if (!texto.isEmpty()) {
            
            if (texto.startsWith("MSG_ROOM|") || texto.startsWith("REG|") || 
                texto.startsWith("LOGIN|") || texto.startsWith("LOGOUT") || texto.startsWith("LIST_USERS") 
                || texto.startsWith("MSG_PRIV|")) {
                
                conexion.enviarComando(texto);
                
            } else {
                String salonSeleccionado = listaSalones.getSelectedValue();
                
                if (salonSeleccionado == null) {
                    JOptionPane.showMessageDialog(this, "Selecciona un salón o escribe el comando completo.");
                    return;
                }
                
                String comandoProtocolo = "MSG_ROOM|" + salonSeleccionado + "|" + texto;
                conexion.enviarComando(comandoProtocolo);
            }
            
            campoMensaje.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaChat().setVisible(true);
        });
    }
}