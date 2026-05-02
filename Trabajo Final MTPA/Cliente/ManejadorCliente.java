package Cliente;

import Servidor.ServidorChat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManejadorCliente implements Runnable
{
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    private String nombreUsuario="Anonimo";
    private boolean autenticado=false;


    public ManejadorCliente(Socket socket)
    {
        this.socket= socket;
    }


    public void enviarMensaje(String mensaje){
        if(salida!=null){
            salida.println(mensaje);
        }
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
                mensajeCrudo=mensajeCrudo.trim();
                if(mensajeCrudo.isEmpty()){
                    continue;
                }
                System.out.println("Recibido del cliente: "+ mensajeCrudo);
                String[] partes= mensajeCrudo.split("\\|");
                String comando= partes[0]; //sera "LOGIN" o "MSG"

                switch(comando)
                {
                    case "REG":
                        if(partes.length>=2){
                            String nuevoUsuario=partes[1];
                            if(ServidorChat.usuariosRegistrados.containsKey(nuevoUsuario)){
                                salida.println("REG_ERR|Usuario ya registrado");
                            }else{
                                // Generamos la clave autonumérica y la guardamos de la mano con el nombre
                                int numeroAleatorio= (int)(Math.random()*9000+1000);
                                String claveAutonumerica=String.valueOf(numeroAleatorio);
                                ServidorChat.usuariosRegistrados.put(nuevoUsuario,claveAutonumerica);
                                salida.println("REG_OK|"+ claveAutonumerica);
                                System.out.println("Registro exitoso ---> Usuario "+nuevoUsuario+"|Clave: "+claveAutonumerica);
                            }
                        }else{
                            salida.println("REG_ERR|Formato incorrecto");
                        }
                        break;
                        case "LOGIN":
                            if(partes.length>=3){
                                String usuario=partes[1];
                                String clave=partes[2];
                                //validamos que existe el usuario y contraseña
                                if(ServidorChat.usuariosRegistrados.containsKey(usuario)&& ServidorChat.usuariosRegistrados.get(usuario).equals(clave)){
                                    this.nombreUsuario=usuario;
                                    this.autenticado=true;
                                    salida.println("LOGIN_OK|IA,DEPORTES,THERIAN,MANGA,UEMC");
                                    System.out.println(usuario+" ha iniciado sesion ");
                                }else{
                                    salida.println("LOGIN_ERR|Credenciales_incorrectas");
                                }
                                
                        }
                        break;
                        case "MSG_ROOM":
                            if(partes.length>=3 && autenticado){
                                String salon= partes[1];
                                String contenido=partes[2];

                                if(contenido.length()>190){
                                    contenido=contenido.substring(0,190);
                                }
                                String fechaHora=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                String difusion= "ROOM_BROADCAST|"+ salon + "|"+this.nombreUsuario+"|"+fechaHora+"|"+contenido;
                                //BROADCAST:repartir el mensaje a todos lños clientes conectados
                                for(ManejadorCliente cliente: ServidorChat.clientesConectados){
                                    cliente.enviarMensaje(difusion);
                                 }
                            }
                            break;
                        case "LOGOUT":
                            this.autenticado=false;
                            cerrarConexion();
                            break;
                }
            }
        } catch(IOException e)
            {
                System.out.println("Cliente desconectado.");
            }finally
            {
                cerrarConexion();
            }
            
            
                


    }
    private void cerrarConexion(){
        try{
            ServidorChat.clientesConectados.remove(this);
            if(socket!=null && !socket.isClosed()){
                socket.close();
                System.out.println("Sesion cerrada para "+ nombreUsuario);
            }
        }catch(IOException e){}
    }

}
