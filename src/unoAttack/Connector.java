/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoAttack;


import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
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
            System.out.println("Past event type check.");
            byte[] newData = new byte[comPort.bytesAvailable()];
            int numRead = comPort.readBytes(newData, newData.length);
            stringBuffer = new String(newData,0,numRead);
           receiveData(stringBuffer);
            
        }
    }
    
     private static String message="";
    private static void receiveData(String data)
    {
     
        int end = data.indexOf("%%");
         if(end!=-1)
        {
            message+=data.substring(0, end+2);
            filter(message);
            message="";
            receiveData(data.substring(end+2));
        }
        else
        {
            message+=data;
        } 
    }
    
    public static  void filter(String s)
    {
        System.out.println(s);
    }

    /**
     *
     * @param args
     */
    static public void main(String[] args)  
    {
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
        sentMessage("$$AB1101AZ%%");
    }
    
    /**
    *  Inicio de la trama   ($$)
    *  Jugador Origen       (A,B,C,D)
    *  Jugador Destino      (A,B,C,D)
    *  UNO                  (0,1)
    *  SENTIDO              (0,1)
    *  CARTA JUGADA         (#1-15,AZ/AM/VE/RO/NE)
    *  FINAL DE TRAMA       (%%)
    **/
    
     public static void sentMessage(String message)
    {
        try
        {
            byte[] bytes = message.getBytes(); 
            comPort.writeBytes(bytes,bytes.length);
        }
        catch (Exception e)
        {
            System.err.println("Error al enviar mensaje: "+e.getMessage());
        }
    }
}
