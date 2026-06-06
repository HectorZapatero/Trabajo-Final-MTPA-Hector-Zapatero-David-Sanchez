package Pruebas;

import Servidor.GestorMensajesCSV;
import org.junit.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.Assert.*;

public class GestorMensajesCSVTest {

    @Test
    public void testGuardarYObtenerMensajes() {
        GestorMensajesCSV gestor = new GestorMensajesCSV();
        String salonTest = "IA";
        String usuarioTest = "RobotTest";
        String fechaHoraTest = LocalDate.now().toString() + " 12:00";
        String contenidoTest = "Mensaje de prueba para cobertura";

        gestor.guardarMensaje(salonTest, usuarioTest, fechaHoraTest, contenidoTest);
        List<String> mensajesHoy = gestor.obtenerMensajesDeHoy(salonTest);
        assertFalse("Debería haber al menos un mensaje hoy en el salón IA", mensajesHoy.isEmpty());
        List<String> historial = gestor.obtenerHistorialAnterior(salonTest, 0, 10);
        assertNotNull("La lista del historial nunca debería ser nula", historial);
    }
}