/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoAttack;


import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import static java.lang.Integer.parseInt;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jssc.SerialPortList;


public class Connector {

    static SerialPort comPort;
    static SerialPort[] comPorts;
    static String stringBuffer;
    Thread t;

    private static final class DataListener implements SerialPortDataListener
    {
        @Override
        public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

        @Override
        public void serialEvent(SerialPortEvent event)
        {
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
           // System.out.println("Past event type check.");
            byte[] newData = new byte[comPort.bytesAvailable()];
            int numRead = comPort.readBytes(newData, newData.length);
            stringBuffer = new String(newData,0,numRead);
           receiveData(stringBuffer);
            
        }
    }
    
     private static String message="";
    private static void receiveData(String data)
    {     
        System.out.println("Trama recibida "+ data);
       // sentMessage("AB0113AZ07VE");
        ferificarCarta(data);
        //sentMessage("AB1101VE01");
        /*System.out.println("Jugador Origen:"+data.substring(0,1));
        System.out.println("Jugador Destino:"+data.substring(1,2));
        System.out.println("UNO:"+data.substring(2,3));
        //Condicion para el uno
        System.out.println("Sentido:"+data.substring(3,4));
        //Condicion par el sentido
        System.out.println("Carta Jugada:"+data.substring(4,8));
        //Condicion para la carta jugada anteriormente
        System.out.println("Cant. Cartas jugadas por el jugador anterior:"+data.substring(8,10));*/
        
        //Falta mandar la data.
    }
    
    public static  void filter(String s)
    {
        System.out.println(s);
    }
    
    public static void ferificarCarta(String carta){
        int         nroCarta=parseInt(carta.substring(4,6));
        String      tipoCarta=carta.substring(6,8);
        String      colorCarta = null;
        String      cantidadCartas=carta.substring(8,10);
        String      nuevoColorCarta=carta.substring(10,12);
        
        if(tipoCarta.equals("AZ"))         colorCarta="azul";
        else if(tipoCarta.equals("AM"))    colorCarta="amarillo";
        else if(tipoCarta.equals("VE"))    colorCarta="verde";
        else if(tipoCarta.equals("RO"))    colorCarta="rojo";
        else if(tipoCarta.equals("NE"))    colorCarta="negro";
        
                
        if(nuevoColorCarta.equals("AZ"))         nuevoColorCarta="azul";
        else if(nuevoColorCarta.equals("AM"))    nuevoColorCarta="amarillo";
        else if(nuevoColorCarta.equals("VE"))    nuevoColorCarta="verde";
        else if(nuevoColorCarta.equals("RO"))    nuevoColorCarta="rojo";
        else if(nuevoColorCarta.equals("NE"))    nuevoColorCarta="negro";
        
        //Si el valor de la carta es mayor a 9, es un comodin.
        if(nroCarta>9){
         if(nroCarta==10){
            //System.out.println("Reversa de color " + colorCarta);
            generarTramaReversa(carta);
         }
         
         else if(nroCarta==11){
            //System.out.println("Bloqueo de color " + colorCarta);
            generarTramaBloqueo(carta);
         }
         
         else if (nroCarta==12){
            //System.out.println("+2 de color " + colorCarta);
            generarTramaMas2(carta);
         }
         
         else if (nroCarta==13){
            //System.out.println("Cambia de de color a " + nuevoColorCarta);
            generarTramaCambioDeColor(carta);
         }
         
         else if (nroCarta==14){
           // System.out.println("+4 con nuevo color de carta" + nuevoColorCarta);
            generarTramaMas4(carta);
         }
         
         else if (nroCarta==15){
            //System.out.println("manotazo " + colorCarta);
         }
        }
        
        else  System.out.println("La carta no es un comodin");
    }
    
    public static void generarTramaReversa(String trama){
        int         nroCarta=parseInt(trama.substring(4,6));
        String      tipoCarta=trama.substring(6,8);
        int         sentido=parseInt(trama.substring(3,4));
        String      jugadorActual=trama.substring(1,2);
        String      jugadorSiguiente=null;
        String      colorNuevo=trama.substring(6,8);
        
        //Estas son cosas que despues se deben de volver funciones
        String uno="0";
        String cartaJugada=nroCarta+tipoCarta;
        String cantidadDeCartas="20";
        int bloqueado=0;
     
        
        
        if(sentido==1)      sentido=0;
        else if(sentido==0) sentido=1;
        
       
        
        if(sentido==1){
            if      (jugadorActual.equals("A")) jugadorSiguiente="B";
            else if (jugadorActual.equals("B")) jugadorSiguiente="C";
            else if (jugadorActual.equals("C")) jugadorSiguiente="D";
            else if (jugadorActual.equals("D")) jugadorSiguiente="A";
        }
        else if(sentido==0){
            if      (jugadorActual.equals('A')) jugadorSiguiente="D";
            else if (jugadorActual.equals("D")) jugadorSiguiente="C";
            else if (jugadorActual.equals("C")) jugadorSiguiente="B";
            else if (jugadorActual.equals("B")) jugadorSiguiente="A"; 
        }
        //Condicion para el uno.
        //generar carta jugada
        //contar cartas en mano
  
        String tramaNueva=jugadorActual+jugadorSiguiente+uno+sentido+cartaJugada+cantidadDeCartas+colorNuevo+bloqueado;
        
        sentMessage(tramaNueva); 
    }
    
    public static void generarTramaBloqueo(String trama){
        int         nroCarta=parseInt(trama.substring(4,6));
        String      tipoCarta=trama.substring(6,8);
        int     sentido=parseInt(trama.substring(3,4));
        String  jugadorActual=trama.substring(1,2);
        String  jugadorSiguiente=null;
        String  colorNuevo=trama.substring(6,8);
        int     bloqueado=parseInt(trama.substring(8,9));
        
        //Estas son cosas que despues se deben de volver funciones
        String uno="0";
        String cartaJugada=nroCarta+tipoCarta;
        String cantidadDeCartas="20";
        
        if(bloqueado==1){
            if(sentido==1){
                if      (jugadorActual.equals("A")) jugadorSiguiente="C";
                else if (jugadorActual.equals("B")) jugadorSiguiente="D";
                else if (jugadorActual.equals("C")) jugadorSiguiente="A";
                else if (jugadorActual.equals("D")) jugadorSiguiente="B";
            }
            else if(sentido==0){
                if      (jugadorActual.equals('A')) jugadorSiguiente="C";
                else if (jugadorActual.equals("D")) jugadorSiguiente="B";
                else if (jugadorActual.equals("C")) jugadorSiguiente="A";
                else if (jugadorActual.equals("B")) jugadorSiguiente="D"; 
            }
            bloqueado=0;
        }
        
        else if(bloqueado==0){
            if(sentido==1){
                if      (jugadorActual.equals("A")) jugadorSiguiente="B";
                else if (jugadorActual.equals("B")) jugadorSiguiente="C";
                else if (jugadorActual.equals("C")) jugadorSiguiente="D";
                else if (jugadorActual.equals("D")) jugadorSiguiente="A";
            }
            else if(sentido==0){
                if      (jugadorActual.equals('A')) jugadorSiguiente="D";
                else if (jugadorActual.equals("D")) jugadorSiguiente="C";
                else if (jugadorActual.equals("C")) jugadorSiguiente="B";
                else if (jugadorActual.equals("B")) jugadorSiguiente="A"; 
            }
        }
        //Condicion para el uno.
        //generar carta jugada
        //contar cartas en mano
        String tramaNueva=jugadorActual+jugadorSiguiente+uno+sentido+cartaJugada+cantidadDeCartas+colorNuevo+bloqueado;
        
        sentMessage(tramaNueva); 
    }
    public static void generarTramaMas2(String trama){
        int         nroCarta=parseInt(trama.substring(4,6));
        String      tipoCarta=trama.substring(6,8);
        int         sentido=parseInt(trama.substring(3,4));
        String      jugadorActual=trama.substring(1,2);
        String      jugadorSiguiente=null;
        String      colorNuevo=trama.substring(6,8);
        int         bloqueado=parseInt(trama.substring(8,9));
        
        //Estas son cosas que despues se deben de volver funciones
        String uno="0";
        String cartaJugada=nroCarta+tipoCarta;
        String cantidadDeCartas="20";
     
        
        
        if(sentido==1)      sentido=0;
        else if(sentido==0) sentido=1;
        

        
        if(bloqueado==1){
            if(sentido==1){
                if      (jugadorActual.equals("A")) jugadorSiguiente="C";
                else if (jugadorActual.equals("B")) jugadorSiguiente="D";
                else if (jugadorActual.equals("C")) jugadorSiguiente="A";
                else if (jugadorActual.equals("D")) jugadorSiguiente="B";
            }
            else if(sentido==0){
                if      (jugadorActual.equals('A')) jugadorSiguiente="C";
                else if (jugadorActual.equals("D")) jugadorSiguiente="B";
                else if (jugadorActual.equals("C")) jugadorSiguiente="A";
                else if (jugadorActual.equals("B")) jugadorSiguiente="D"; 
            }
            bloqueado=0;
        }
        
        else if(bloqueado==0){
            if(sentido==1){
                if      (jugadorActual.equals("A")) jugadorSiguiente="B";
                else if (jugadorActual.equals("B")) jugadorSiguiente="C";
                else if (jugadorActual.equals("C")) jugadorSiguiente="D";
                else if (jugadorActual.equals("D")) jugadorSiguiente="A";
            }
            else if(sentido==0){
                if      (jugadorActual.equals('A')) jugadorSiguiente="D";
                else if (jugadorActual.equals("D")) jugadorSiguiente="C";
                else if (jugadorActual.equals("C")) jugadorSiguiente="B";
                else if (jugadorActual.equals("B")) jugadorSiguiente="A"; 
            }
        }
        //Condicion para el uno.
        //generar carta jugada
        //contar cartas en mano +2
        //añadir el doble manotazo

        String tramaNueva=jugadorActual+jugadorSiguiente+uno+sentido+cartaJugada+cantidadDeCartas+colorNuevo+bloqueado;
        sentMessage(tramaNueva); 
   
    }
    
    public static void generarTramaCambioDeColor(String trama){
        int     sentido=parseInt(trama.substring(3,4));
        String  jugadorActual=trama.substring(1,2);
        String  jugadorSiguiente=null;
        String  colorNuevo=trama.substring(6,8);
        int     bloqueado=parseInt(trama.substring(8,9));
        
        //Estas son cosas que despues se deben de volver funciones
        String uno="0";
        String cartaJugada="13NE";
        String cantidadDeCartas="20";
        
        if(sentido==1){
                if      (jugadorActual.equals("A")) jugadorSiguiente="B";
                else if (jugadorActual.equals("B")) jugadorSiguiente="C";
                else if (jugadorActual.equals("C")) jugadorSiguiente="D";
                else if (jugadorActual.equals("D")) jugadorSiguiente="A";
            }
        else if(sentido==0){
             if      (jugadorActual.equals('A')) jugadorSiguiente="D";
             else if (jugadorActual.equals("D")) jugadorSiguiente="C";
             else if (jugadorActual.equals("C")) jugadorSiguiente="B";
             else if (jugadorActual.equals("B")) jugadorSiguiente="A"; 
        }
        
        String tramaNueva=jugadorActual+jugadorSiguiente+uno+sentido+cartaJugada+cantidadDeCartas+colorNuevo+bloqueado;
        sentMessage(tramaNueva); 
        
    }
    
    
    public static void generarTramaMas4(String trama){
        int     sentido=parseInt(trama.substring(3,4));
        String  jugadorActual=trama.substring(1,2);
        String  jugadorSiguiente=null;
        String  colorNuevo=trama.substring(6,8);
        int     bloqueado=parseInt(trama.substring(8,9));
        
        //Estas son cosas que despues se deben de volver funciones
        String uno="0";
        String cartaJugada="14NE";
        String cantidadDeCartas="20";
        
        if(bloqueado==1){
            if(sentido==1){
                if      (jugadorActual.equals("A")) jugadorSiguiente="C";
                else if (jugadorActual.equals("B")) jugadorSiguiente="D";
                else if (jugadorActual.equals("C")) jugadorSiguiente="A";
                else if (jugadorActual.equals("D")) jugadorSiguiente="B";
                bloqueado=0;
            }
            else if(sentido==0){
                if      (jugadorActual.equals('A')) jugadorSiguiente="C";
                else if (jugadorActual.equals("D")) jugadorSiguiente="B";
                else if (jugadorActual.equals("C")) jugadorSiguiente="A";
                else if (jugadorActual.equals("B")) jugadorSiguiente="D"; 
            }
        }
        
        else if(bloqueado==0){
            if(sentido==1){
                if      (jugadorActual.equals("A")) jugadorSiguiente="B";
                else if (jugadorActual.equals("B")) jugadorSiguiente="C";
                else if (jugadorActual.equals("C")) jugadorSiguiente="D";
                else if (jugadorActual.equals("D")) jugadorSiguiente="A";
                bloqueado=0;
            }
            else if(sentido==0){
                if      (jugadorActual.equals('A')) jugadorSiguiente="D";
                else if (jugadorActual.equals("D")) jugadorSiguiente="C";
                else if (jugadorActual.equals("C")) jugadorSiguiente="B";
                else if (jugadorActual.equals("B")) jugadorSiguiente="A"; 
            }
        }
        
        //Función para aumentar
        String tramaNueva=jugadorActual+jugadorSiguiente+uno+sentido+cartaJugada+cantidadDeCartas+colorNuevo+bloqueado;
        sentMessage(tramaNueva); 
        
    }

    /**
     *
     * @param args
     */
    static public void main(String[] args)  
    {
         /**
            *  Jugador Origen                   (A,B,C,D)
            *  Jugador Destino                  (A,B,C,D)
            *  UNO                              (0,1) 0=tiene uno pero no lo dijo o no tiene uno, 1= tiene uno y lo dijo
            *  SENTIDO                          (0,1) 0=izquierda, 1=derecha
            *  CARTA JUGADA                     (#1-16,AZ/AM/VE/RO/NE)
            *  CANT.MANO DEL JUGADOR ANTERIOR   (#1-99)
            *  COLOR NUEVO                      (AZ/AM/VE/RO/NE)
            * BLOQUEADO                         (0,1)0=no esta bloqueado, 1= esta bloqueado
        **/
        
        /*EL COLOR NUEVO ES PARA CUANDO SE TENGA UNA CARTA NEGRA COMO +4 o +2*/
        
        /*
            NROS DE CARTAS
            #0-9/ Cualquier Color
            #10=reversa
            #11=bloqueo
            #12=+2
            #13=cambia color
            #14=+4
            #15=manotazo?
        */
    
        String[] portNames = SerialPortList.getPortNames();
        String s=(String) JOptionPane.showInputDialog(new JFrame("NADA"),"Introduce el puerto",
                "Configuración de puerto", JOptionPane.QUESTION_MESSAGE,null,
                portNames,portNames[0]);
        
        System.out.println(s);
        comPort= SerialPort.getCommPort(s);
        
        comPort.setFlowControl(0);
        System.out.println(comPort.getDescriptivePortName());
        comPort.openPort();
        System.out.println("COM port open: " + comPort.getSystemPortName());
        DataListener listener = new DataListener();
        comPort.addDataListener(listener);
        System.out.println("Event Listener open.");
        
           
        String data="AB0110AZ07AZ";
        sentMessage(data);
        //data=null;
        //data="BC0111NE08VE";
        //sentMessage("AB0113AZ07VE");
    }
      public static void sentMessage(String message)
    {
        try
        {
            System.out.println("Trama enviada: "+message);
            byte[] bytes = message.getBytes(); 
            comPort.writeBytes(bytes,bytes.length);
        }
        catch (Exception e)
        {
            System.err.println("Error al enviar mensaje: "+e.getMessage());
        }
    }
}
