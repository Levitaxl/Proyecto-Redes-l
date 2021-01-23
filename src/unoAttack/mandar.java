/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoAttack;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;


public class mandar {

    static SerialPort comPort;
    static String stringBuffer;

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
            //System.out.println("Past event type check.");
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

    static public void main(String[] args) throws InterruptedException
    {
        SerialPort comPort = SerialPort.getCommPort("COM14");
        /*comPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);*/
        comPort.openPort();

        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    return;
                }
                byte[] newData = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(newData, newData.length);
                System.out.println("Read " + numRead + " bytes. " + newData[0]);
            }
            
            
        });

        

        byte[] sendData = new byte[]{(byte) 0x53};
        comPort.writeBytes(new byte[]{'a'}, 1);
        //comPort.writeBytes(sendData, 1);
    }
}