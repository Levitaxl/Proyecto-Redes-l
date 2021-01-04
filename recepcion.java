/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rxtx;

import com.fazecast.jSerialComm.SerialPort;
import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DISABLED;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import static rxtx.mandar.comPort;
import static rxtx.mandar.stringBuffer;


public class recepcion extends Thread {

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
            //System.out.println("In event listener.");
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                return;
            System.out.println("Past event type check.");
            byte[] newData = new byte[comPort.bytesAvailable()];
            int numRead = comPort.readBytes(newData, newData.length);
            stringBuffer = new String(newData,0,numRead);
            //System.out.println("Read " + numRead + " bytes.");
            System.out.println(stringBuffer);


        }
    }
    
    
    public void sendByteImmediately(byte b) throws Exception {
        comPort.writeBytes(new byte[]{b}, 1);
    }

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
        System.out.println("Event Listener open.");
        t = new Thread(new recepcion());

        
        
       
       
        int i=0;
        while(i<=10){
             comPort.writeBytes(new byte[]{'b'}, 1);
             i++;
            
        }
        
    }
}