package Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManejadorCliente implements Runnable
{
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    public ManejadorCliente(Socket socket)
    {
        this.socket= socket;
    }

    @Override
    public void run()
    {
        try
        {
            entrada= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida= new PrintWriter(socket.getOutputStream(),true);
            String mensajeCrudo;
            while((mensajeCrudo= entrada.readLine())!= null)
            {
                System.out.println("Recibido del cliente: "+ mensajeCrudo);
                //Respusta de prueba
                salida.println("PONG");
            }
        }catch(IOException e)
            {
                System.out.println("Cliente desconectado.");
            }finally
            {
                cerrarConexion();
            }
            
            
                


    }
    private void cerrarConexion(){
        try{
            if(socket!=null){
                socket.close();
            }
        }catch(IOException e){}
    }

}
