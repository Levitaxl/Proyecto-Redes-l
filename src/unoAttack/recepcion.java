/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoAttack;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;


public class recepcion extends Thread {

    static SerialPort comPort;
     static SerialPort comPort2;
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
            //System.out.println("In event listener.");
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                return;
            System.out.println("Past event type check.");
            byte[] newData = new byte[comPort.bytesAvailable()];
            int numRead = comPort.readBytes(newData, newData.length);
            if(numRead>0)System.out.println("Read COM 1");
            if(numRead<=0){
                System.out.println("Read COM 1");
                numRead = comPort2.readBytes(newData, newData.length);
            }
            stringBuffer = new String(newData,0,numRead);
            System.out.println("Read " + numRead + " bytes.");
            System.out.println(stringBuffer);
        }
       

    }
    
    /*TRAMA*/
    
    /**
     * Inicio de la trama   ($$)
     * Emisor               (A,B,C,D,S,T)
     * Destinatario         (A,B,C,D,S,T)
     * GANADOR JUEGO        (AC, BD, SG)
     * TURNO                (A,B,C,D)
     * PODER DE ENVIDO      (S,#)
     * RESPUESTA ENVIDO     (S,N,#)
     * PEDIR TRUCO          (S,#)
     * RESPUESTA TRUCO      (S,N,#)
     * CARTA JUGADA         (#1-12 B, C, E, O)
     * FINAL DE TRAMA       (%%)
     * */
    
    
    
    /*public void sendByteImmediately(byte b) throws Exception {
        comPort.writeBytes(new byte[]{b}, 1);
    }*/

    /**
     *
     * @param args
     */
    static public void main(String[] args)  
    {
        comPort = SerialPort.getCommPort("COM1");
        comPort.setFlowControl(0);
        System.out.println(comPort.getDescriptivePortName());
        comPort.openPort();
        System.out.println("COM port open: " + comPort.getSystemPortName());
        DataListener listener = new DataListener();
        comPort.addDataListener(listener);
        System.out.println("Event Listener open. 1");
       
        
        
        comPort2 = SerialPort.getCommPort("COM2");
        comPort2.setFlowControl(0);
        System.out.println(comPort2.getDescriptivePortName());
        comPort2.openPort();
        System.out.println("COM port open: " + comPort2.getSystemPortName());
        DataListener listener2 = new DataListener();
        comPort2.addDataListener(listener2);
        System.out.println("Event Listener open. 2");
       
        
        int i=0;
        //byte[] b = new byte[]{ (byte) 'hola' };
    
        String example = "$$ABCDEFGHIJKLM%%";
        byte[] bytes = example.getBytes(); 
        comPort2.writeBytes(bytes,bytes.length);
        example = "$$AAAAAAAAAA%%";
        bytes = example.getBytes(); 
        comPort2.writeBytes(bytes,bytes.length);
        /*while(i<=1){
            comPort2.writeBytes(bytes,bytes.length);
            i++;
        }*/
        
        i=0;
        /*while(i<=1){
            comPort2.writeBytes(new byte[]{'h'}, 1);
            i++;
        }*/
        
        /*i=0;
        while(i<=1){
            System.out.println("Encuentro en puerto 2");
            comPort.writeBytes(new byte[]{'2'}, 1);
            i++;
        }*/

        
        
      /* i=0;
       
     
        while(i<=10){   
             comPort2.writeBytes(new byte[]{'2'}, 1);
             //comPort2.writeBytes(new byte[]{'b'}, 1);
             i++;
        }
        i=0;
        while(i<=1){
             comPort2.writeBytes(new byte[]{'2'}, 1);
             i++;
        }*/
        
    }
}