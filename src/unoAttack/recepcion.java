/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoAttack;


import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jssc.SerialPort;
import jssc.SerialPortList;


public class recepcion {

    private static SerialPort serialPort;
        public static void start()
        {
            String[] portNames = SerialPortList.getPortNames();
            String s=(String) JOptionPane.showInputDialog(new JFrame("NADA"),"Introduce el puerto",
                    "Configuración de puerto", JOptionPane.QUESTION_MESSAGE,null,
                    portNames,portNames[0]);
            /**Cree en VSPE un par con puertos COM4 y COM5*/
            serialPort=new SerialPort(s);
            try
            {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_1200,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                serialPort.addEventListener(new PortReader(serialPort));
            }
            catch (Exception e)
            {
                System.err.println("Error al abrir puerto");
                JOptionPane.showMessageDialog(new JFrame("NADA"),
                        "Error al abrir puerto, el puerto que ha seleccionado no se encuantra disponible",
                        "Error al abrir puerto",JOptionPane.ERROR_MESSAGE);
                start();
            }
        }
}