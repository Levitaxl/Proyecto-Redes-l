package rxtx;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
  public class LecturaSerial  {
	  //inicializamos y decalramos variables
	CommPortIdentifier portId;
	Enumeration puertos;
	SerialPort serialport;
	static InputStream entrada = null;
	Thread t;
	//creamos un constructor para realizar la conexion del puerto
    public LecturaSerial() {
        super();
        puertos=CommPortIdentifier.getPortIdentifiers();
        t = new Thread(new LeerSerial());
        while (puertos.hasMoreElements()) { //para recorrer el numero de los puertos, y especificar con cual quiero trabajar 
        //hasmorelements mientras tenga mas eleementos
            portId = (CommPortIdentifier) puertos.nextElement(); //next elemento recorre uno por uno
            System.out.println(portId.getName()); //puertos disponbibles
            if (portId.getName().equalsIgnoreCase("COM14")) {
                try {
                    serialport= (SerialPort)portId.open("LecturaSerial", 500);//tiempo en ms
                    InputStream inStream = serialport.getInputStream();
                    entrada = serialport.getInputStream();//esta variable del tipo InputStream obtiene el dato serial
                    t.start(); // inciamos el hilo para realizar nuestra accion de imprimir el dato serial
                } catch (Exception e) {} 
            }
         }
    }
    //con este metodo del tipo thread relaizamos 

    public static class LeerSerial implements Runnable {
	int aux;
        public void run () {
    	   while(true){
                    try {
			aux = entrada.read(); // aqui estamos obteniendo nuestro dato serial
			Thread.sleep(100);
 			if (aux>0) System.out.println((char)aux);//imprimimos el dato serial				
                    }
                    catch (Exception e) {} 
           } 
        }
    }
    public static void main(String[] args) {
	new LecturaSerial();
    }
  
}