package Pruebas;

import Servidor.GestorUsuariosCSV;
import org.junit.Test;
import static org.junit.Assert.*;

public class GestorUsuariosCSVTest {

    @Test
    public void testRegistroYValidacionUsuario() {
        GestorUsuariosCSV gestor = new GestorUsuariosCSV();
        String usuarioTest = "UserPrueba_" + System.currentTimeMillis();
        String claveTest = "9999";

        assertFalse("El usuario no debería existir antes de registrarse", gestor.existeUsuario(usuarioTest));
        gestor.registrarUsuario(usuarioTest, claveTest);
        assertTrue("El usuario debería existir tras el registro", gestor.existeUsuario(usuarioTest));
        assertTrue("La clave debería estar registrada en el sistema", gestor.existeClave(claveTest));
        assertTrue("Las credenciales deberían ser válidas", gestor.validarCredenciales(usuarioTest, claveTest));
        assertFalse("Debería rechazar una clave incorrecta", gestor.validarCredenciales(usuarioTest, "0000"));
    }
}