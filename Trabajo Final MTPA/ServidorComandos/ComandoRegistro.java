package ServidorComandos;

import ServidorInterfaces.IClienteConectado;
import ServidorInterfaces.IServicioUsuarios;

public class ComandoRegistro implements Comando {
    private final IServicioUsuarios servicioUsuarios;

    public ComandoRegistro(IServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    @Override
    public void ejecutar(IClienteConectado cliente, String[] partes) {
        if (partes.length < 2) {
            cliente.enviarMensaje("REG_ERR|Formato incorrecto");
            return;
        }
        String nombre = partes[1];
        if (servicioUsuarios.existeUsuario(nombre)) {
            cliente.enviarMensaje("REG_ERR|Usuario ya registrado");
        } else {
            String clave;
            // Bucle de seguridad: genera claves numéricas hasta encontrar una libre
            do {
                clave = String.valueOf((int) (Math.random() * 9000 + 1000));
            } while (servicioUsuarios.existeClave(clave));

            servicioUsuarios.registrarUsuario(nombre, clave);
            cliente.enviarMensaje("REG_OK|" + clave);
        }
    }
}