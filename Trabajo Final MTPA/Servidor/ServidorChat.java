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
    private static final String ARCHIVO_USUARIOS="usuarios.csv";

    private void cargarUsuariosDesdeCSV(){
        java.io.File archivo= new java.io.File(ARCHIVO_USUARIOS);
        if(!archivo.exists()) return;
        try (java.io.BufferedReader br= new java.io.BufferedReader(new java.io.FileReader(archivo))) {
            String linea;
            while((linea=br.readLine())!=null){
                String[] partes= linea.split(",");
                if(partes.length==2){
                    usuariosRegistrados.put(partes[0],partes[1]);
                }
            }
            System.out.println("Memoria sincronizada con el archivo CSV");
        }catch(IOException e){
            System.out.println("Error al cargar usuarios desde el archivo CSV: "+ e.getMessage());
        } 
    }
    
    public void iniciar()
    {
        cargarUsuariosDesdeCSV();
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
