package Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConexionServidor 
{
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;

    public void conectar(String ip,int puerto)throws IOException
    {
        this.socket= new Socket(ip,puerto);
        this.salida= new PrintWriter(socket.getOutputStream(),true);
        this.entrada= new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void enviarComando(String comando)
    {
        if(salida!=null)
        {
            salida.println(comando);
        }
    }

    public void escucharServidor(java.util.function.Consumer<String> alRecibiMensaje)
    {
        new Thread(()->{
            try {
                String linea;
                while((linea=entrada.readLine())!=null){
                    alRecibiMensaje.accept(linea);
                }
            } catch (IOException e) {
                System.out.println("Conexion perdida con el servidor.");
            }

    }).start();
    }



}



