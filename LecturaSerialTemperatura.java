/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rxtx;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
  public class LecturaSerialTemperatura  {
	  
	  //inicializamos y decalramos variables
	CommPortIdentifier portId;
	Enumeration puertos;
	SerialPort serialport;
	static InputStream entrada = null;
	Thread t;
	static int temp=10;
	
	//creamos un constructor para realizar la conexion del puerto
   	public LecturaSerialTemperatura() {
        super();
        puertos=CommPortIdentifier.getPortIdentifiers();
        t = new Thread(new LeerSerial());
        while (puertos.hasMoreElements()) { //para recorrer el numero de los puertos, y especificar con cual quiero trabajar 
        			//hasmorelements mientras tenga mas elementos
        	portId = (CommPortIdentifier) puertos.nextElement(); //next elemento recorre uno por uno
        	System.out.println(portId.getName()); //puertos disponbibles
  			if (portId.getName().equalsIgnoreCase("COM7")) {
  					try {
  						serialport= (SerialPort)portId.open("LecturaSerialTemperatura", 500);//tiempo en ms
  						entrada = serialport.getInputStream(); //esta variable del tipo InputStream obtiene el dato serial
    					t.start(); // inciamos el hilo para realizar nuestra accion de imprimir el dato serial
     		 
  						} catch (Exception e) {
 			} } }
  }
    public int data() {       
		return temp;
	}
    //con este metodo del tipo thread relaizamos 
  	public static class LeerSerial implements Runnable {
	   int aux;
       public void run () {
    	   while(true){
    		  try {
				aux = entrada.read();// aqui estamos obteniendo nuestro dato serial
				Thread.sleep(100);
 				if (aux>0) {
 					temp=aux;
					System.out.println((char)aux);//imprimimos el dato serial
				}				
			} catch (Exception e) {
 			} } }
       
       
  } }