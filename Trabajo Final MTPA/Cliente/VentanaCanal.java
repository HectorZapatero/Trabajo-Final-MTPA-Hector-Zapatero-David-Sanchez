package Cliente;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class VentanaCanal extends JFrame {
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private String nombreCanal; 
    private ConexionServidor conexion;
    private int mensajesHistorialCargados = 0; 

    public VentanaCanal(String nombreCanal, ConexionServidor conexion, Runnable alCerrar) {
        this.nombreCanal = nombreCanal;
        this.conexion = conexion;

        setSize(450, 400);
        setLayout(new BorderLayout());
        
        // Al cerrar la ventana, el JFrame se destruye por completo limpiando la RAM
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        if (nombreCanal.startsWith("Priv_")) {
            setTitle("Chat privado con " + nombreCanal.substring(5));
            // EN PRIVADOS NO SE CREA EL BOTÓN NI SE SOLICITA HISTORIAL
        } else {
            setTitle("Salón: " + nombreCanal);
            
            // EL BOTÓN SOLO EXISTE Y SE MUESTRA EN LOS SALONES PÚBLICOS
            JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnHistorial = new JButton("Cargar mensajes");
            
            btnHistorial.addActionListener(e -> {
                if (mensajesHistorialCargados >= 150) {
                    JOptionPane.showMessageDialog(this, "Has alcanzado el límite máximo de 150 mensajes históricos.", "Límite Alcanzado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                conexion.enviarComando("GET_HIST|" + nombreCanal + "|" + mensajesHistorialCargados);
                mensajesHistorialCargados += 50;
            });
            panelSuperior.add(btnHistorial);
            add(panelSuperior, BorderLayout.NORTH);
        }
        
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        campoMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");

        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        java.awt.event.ActionListener accionEnviar = e -> enviarMensaje();
        btnEnviar.addActionListener(accionEnviar);
        campoMensaje.addActionListener(accionEnviar);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                alCerrar.run(); // Notifica a la ventana principal para sacarlo de ventanasAbiertas
            }
        });
    }

    private void enviarMensaje() {
        String txt = campoMensaje.getText().trim();
        if (!txt.isEmpty()) {
            if (nombreCanal.startsWith("Priv_")) {
                String destino = nombreCanal.substring(5);
                conexion.enviarComando("MSG_PRIV|" + destino + "|" + txt);
                mostrarMensaje("Tú: " + txt);
            } else {
                conexion.enviarComando("MSG_ROOM|" + nombreCanal + "|" + txt);
            }
            campoMensaje.setText("");
        }
    }

    public void mostrarMensaje(String mensaje) {
        areaChat.append(mensaje + "\n");
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }
}