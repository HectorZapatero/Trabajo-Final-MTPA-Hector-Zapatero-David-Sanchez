package Pruebas;

import Servidor.GestorSalonesMemoria;
import org.junit.Test;
import static org.junit.Assert.*;

public class GestorSalonesMemoriaTest {

    @Test
    public void testRegistrarYObtenerMetricas() {
        GestorSalonesMemoria gestor = new GestorSalonesMemoria();
        gestor.registrarMensaje("IA");
        gestor.registrarMensaje("IA");
        gestor.registrarMensaje("Deportes");

        assertEquals("El salón IA debería tener 2 mensajes", Integer.valueOf(2), gestor.obtenerMetricas().get("IA"));
        assertEquals("El salón Deportes debería tener 1 mensaje", Integer.valueOf(1), gestor.obtenerMetricas().get("Deportes"));
        assertNull("El salón Manga no debería tener mensajes", gestor.obtenerMetricas().get("Manga"));
    }
}