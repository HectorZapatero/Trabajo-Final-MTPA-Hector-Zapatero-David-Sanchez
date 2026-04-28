package Servidor;

import Cliente.ManejadorCliente;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServidorChat 
{
    private static final int PUERTO= 5000;
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
