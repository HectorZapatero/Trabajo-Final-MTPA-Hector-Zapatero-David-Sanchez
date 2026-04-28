package Cliente;

import java.awt.*;
import javax.swing.*;


public class VentanaChat extends JFrame {
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private DefaultListModel<String> modeloSalones;
    private ConexionServidor conexion;

    public VentanaChat() {
        // 1. Configuración básica de la ventana
        setTitle("UEMC Chat - Cliente");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 2. Panel de Salones (Izquierda) - Requisito funcional [cite: 44]
        modeloSalones = new DefaultListModel<>();
        modeloSalones.addElement("IA");
        modeloSalones.addElement("Deportes");
        modeloSalones.addElement("Manga");
        JList<String> listaSalones = new JList<>(modeloSalones);
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
            
            // Escuchar mensajes que vienen del servidor
            conexion.escucharServidor(mensaje -> {
                areaChat.append("Servidor dice: " + mensaje + "\n");
            });
            
            areaChat.append("Conectado al servidor correctamente.\n");
        } catch (Exception e) {
            areaChat.append("Error al conectar: " + e.getMessage() + "\n");
        }
    }

    private void enviarMensaje() {
        String texto = campoMensaje.getText();
        if (!texto.isEmpty()) {
            // Aquí es donde más adelante aplicaremos el PROTOCOLO [cite: 22]
            conexion.enviarComando(texto); 
            campoMensaje.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaChat().setVisible(true);
        });
    }
}