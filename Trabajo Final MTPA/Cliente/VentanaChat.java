package Cliente;

import java.awt.*;
import javax.swing.*;

public class VentanaChat extends JFrame {
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private DefaultListModel<String> modeloSalones;
    private JList<String> listaSalones; // LO SUBIMOS AQUÍ para poder usarlo al enviar
    private ConexionServidor conexion;

    public VentanaChat() {
        // 1. Configuración básica de la ventana
        setTitle("UEMC Chat - Cliente");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 2. Panel de Salones (Izquierda)
        modeloSalones = new DefaultListModel<>();
        modeloSalones.addElement("IA");
        modeloSalones.addElement("Deportes");
        modeloSalones.addElement("Manga");
        modeloSalones.addElement("Therian");
        modeloSalones.addElement("UEMC");
        listaSalones = new JList<>(modeloSalones); // Inicializamos la variable de clase
        
        // Seleccionamos el primero por defecto para evitar errores
        listaSalones.setSelectedIndex(0); 
        add(new JScrollPane(listaSalones), BorderLayout.WEST);

        // 3. Área de Chat (Centro)
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        // 4. Campo de entrada (Abajo)
        JPanel panelInferior = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        JButton botonEnviar = new JButton("Enviar");
        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        // --- LÓGICA DE CONEXIÓN ---
        conectarAlServidor();

        // Acción al pulsar Enviar
        botonEnviar.addActionListener(e -> enviarMensaje());
        campoMensaje.addActionListener(e -> enviarMensaje());
    }

    private void conectarAlServidor() {
        try {
            conexion = new ConexionServidor();
            conexion.conectar("127.0.0.1", 5000);
            
            // EL "OÍDO" DEL CLIENTE: TRADUCE EL PROTOCOLO
            conexion.escucharServidor(mensajeCrudo -> {
                // Troceamos el comando que nos llega del servidor
                String[] partes = mensajeCrudo.split("\\|");
                String comando = partes[0];

                switch (comando) {
                    case "ROOM_BROADCAST":
                        // Formato: ROOM_BROADCAST|nombre_salon|usuario_emisor|fecha_hora|contenido_mensaje
                        String salon = partes[1];
                        String emisor = partes[2];
                        String fecha = partes[3];
                        String texto = partes[4];
                        
                        // Lo pintamos bonito en el chat: [12:00] Juan (IA): Hola
                        areaChat.append("[" + fecha + "] " + emisor + " (" + salon + "): " + texto + "\n");
                        // Hacemos que el scroll baje automáticamente
                        areaChat.setCaretPosition(areaChat.getDocument().getLength());
                        break;
                        
                    case "REG_OK":
                        // Mostramos un popup (ventana emergente) con la clave generada
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
                        // partes[1] traerá algo como "Pepe, Juan, Ana, "
                        String listaConectados = partes[1];
                        areaChat.append("--- USUARIOS EN LÍNEA: " + listaConectados + "---\n");
                        areaChat.setCaretPosition(areaChat.getDocument().getLength());
                         break;

                    default:
                        // Para PONG u otros mensajes del sistema
                        areaChat.append("Sistema: " + mensajeCrudo + "\n");
                        break;
                }
            });
            
            areaChat.append("Conectado al servidor. Escribe REG|tu_nombre para registrarte, o LOGIN|tu_nombre|tu_clave para entrar.\n");
        } catch (Exception e) {
            areaChat.append("Error al conectar: " + e.getMessage() + "\n");
        }
    }

    // Muestra un popup no bloqueante que se cierra solo tras X milisegundos
    private void mostrarNotificacionTemporal(String mensaje, int milisegundos)
    {
        JDialog dialogo = new JDialog(this, "Notificación", false); // false = no modal
        dialogo.setLayout(new FlowLayout());
        dialogo.add(new JLabel("  " + mensaje + "  "));
        dialogo.pack();
 
        // Centramos el popup respecto a la ventana principal
        Point ubicacion = this.getLocation();
        dialogo.setLocation(ubicacion.x + this.getWidth() - dialogo.getWidth() - 10,
                            ubicacion.y + 40);
 
        dialogo.setVisible(true);
 
        // Cierra el dialogo en el hilo correcto
        new javax.swing.Timer(milisegundos, e -> dialogo.dispose()).start();
    }

    private void enviarMensaje() {
    String texto = campoMensaje.getText().trim();
    if (!texto.isEmpty()) {
        
        // 1. PRIMERO comprobamos si el usuario ha escrito el comando manual
        // Si empieza por MSG_ROOM|, pasamos olimpicamente de la lista de salones
        if (texto.startsWith("MSG_ROOM|") || texto.startsWith("REG|") || 
            texto.startsWith("LOGIN|") || texto.startsWith("LOGOUT")||texto.startsWith("LIST_USERS")) {
            
            conexion.enviarComando(texto);
            
        } else {
            // 2. SOLO si no es un comando manual, miramos la lista
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