package Servidor;

import Cliente.ManejadorCliente;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ServidorChat 
{

    private static final int PUERTO= 5000;
    public static ConcurrentHashMap <String, String> usuariosRegistrados = new ConcurrentHashMap<>();
    public static List<ManejadorCliente> clientesConectados=Collections.synchronizedList(new ArrayList<>());
    public void iniciar()
    {
        try(ServerSocket serverSocket= new ServerSocket(PUERTO))
        {
            System.out.println("Servidor de chat iniciao en el puerto" + PUERTO);
            //Este bucle de ahora simplemente es un bucle infinito que esta todo el rato escuchando que clientes quieeren acceder((al menos 10 concurrentes))
            while(true)
            {
                Socket socketCliente= serverSocket.accept();
                System.out.println("Nueva conexion: " + socketCliente.getInetAddress());

                //Multiprocesamiento
                ManejadorCliente manejador= new ManejadorCliente(socketCliente);
                clientesConectados.add(manejador);
                new Thread(manejador).start();

            }
            
        }catch(IOException e)
            {
                System.out.println("Error al iniciar el servidor"+ e.getMessage());
            }


    }
    public static void main(String[] args)
    {
        new ServidorChat().iniciar();
        
    } 
   




}
