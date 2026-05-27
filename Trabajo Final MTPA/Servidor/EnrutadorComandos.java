package Servidor;

import ServidorComandos.*;
import ServidorInterfaces.IClienteConectado;
import ServidorInterfaces.IServicioMensajes;
import ServidorInterfaces.IServicioSalones;
import ServidorInterfaces.IServicioUsuarios;
import java.util.HashMap;
import java.util.Map;

public class EnrutadorComandos {
    private final Map<String, Comando> mapaComandos = new HashMap<>();

    public EnrutadorComandos(IServicioUsuarios usuarios, IServicioSalones salones, IServicioMensajes mensajes) {
        mapaComandos.put("REG", new ComandoRegistro(usuarios));
        mapaComandos.put("LOGIN", new ComandoLogin(usuarios));
        mapaComandos.put("JOIN_ROOM", new ComandoUnirSala(mensajes)); 
        mapaComandos.put("LEAVE_ROOM", new ComandoSalirSala());
        mapaComandos.put("MSG_ROOM", new ComandoMensajeSala(salones, mensajes));
        mapaComandos.put("LIST_USERS", new ComandoListaUsuarios());
        mapaComandos.put("MSG_PRIV", new ComandoMensajePrivado());
        mapaComandos.put("LOGOUT", new ComandoLogout());
        mapaComandos.put("PING", new ComandoPing());
        mapaComandos.put("GET_HIST", new ComandoHistorial(mensajes));
    }

    public void procesar(String mensajeCrudo, IClienteConectado cliente) {
        String[] partes = mensajeCrudo.split("\\|");
        String palabraClave = partes[0];
        

        if (Servidor.ServidorChat.mantenimiento && !palabraClave.equals("LOGOUT")) {
            cliente.enviarMensaje("MSG_ERR|El servidor está en mantenimiento. Servicios suspendidos temporalmente.");
            return;
        }
        
        Comando comando = mapaComandos.get(palabraClave);
        if (comando != null) {
            comando.ejecutar(cliente, partes);
        } else {
            cliente.enviarMensaje("MSG_ERR|Protocolo no reconocido");
        }
    }
}