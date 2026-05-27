package ServidorComandos;

import ServidorInterfaces.IClienteConectado;
import ServidorInterfaces.IServicioUsuarios;
import Servidor.ServidorChat;

public class ComandoLogin implements Comando {
    private final IServicioUsuarios servicioUsuarios;

    public ComandoLogin(IServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    @Override
    public void ejecutar(IClienteConectado cliente, String[] partes) {
        if (partes.length < 3) {
            cliente.enviarMensaje("LOGIN_ERR|Credenciales_invalidas");
            return;
        }
        String usuario = partes[1];
        String clave = partes[2];
        
        if (servicioUsuarios.validarCredenciales(usuario, clave)) {
            cliente.setNombreUsuario(usuario);
            cliente.setAutenticado(true);
            cliente.enviarMensaje("LOGIN_OK|IA,Deportes,Therian,Manga,UEMC");
            ServidorChat.difundirNotificacion("NOTIFY_JOIN|" + usuario, cliente);
        } else {
            cliente.enviarMensaje("LOGIN_ERR|Credenciales_invalidas");
        }
    }
}