/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoAttack;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
//import static unoAttack.Connector.sentMessage;

public class Game extends Thread  {
    
    private String currentPlayer;
    private String[] playersId;
    private String nextPlayer;
    boolean uno;

    private UnoDeck deck;
    private ArrayList<ArrayList<UnoCard>> playerHand;
    private ArrayList<UnoCard> stockPile;

    private UnoCard.Color validColor;
    private UnoCard.Value validValue;
    
    private final SerialPort readPort = SerialPort.getCommPorts()[7];
    private final SerialPort writePort = SerialPort.getCommPorts()[0];
    GameStage gameStage;
    JButton button;
    boolean gameDirection;
    private javax.swing.JButton topCardButton2;

    public Game(GameStage gameStage2, JButton topButton) {
        deck = new UnoDeck();
        deck.shuffle();
        stockPile = new ArrayList<UnoCard>();
        currentPlayer = "A";
        gameDirection = false;
        configurarEventoReceptor();
        this.readPort.openPort();
        System.out.println(readPort.getSystemPortName());
        gameStage=gameStage2;
        button=topButton;
        uno = false;
        playerHand = new ArrayList<ArrayList<UnoCard>>();
        
        ArrayList<UnoCard> hand = new ArrayList<UnoCard>(Arrays.asList(deck.drawCard(7)));
        playerHand.add(hand);
    }

     public void enviarMensaje(String mensaje) {
        System.out.println("\n Enviando mensaje...");
        try {
            this.writePort.openPort();
            try (OutputStream out = this.writePort.getOutputStream()) {
                out.flush();
                System.out.println("String: " + mensaje); 
                //System.out.print("Bytes: ");
                System.out.println(Arrays.toString(mensaje.getBytes("ASCII")));
                out.write(mensaje.getBytes());
                System.out.println("Mensaje enviado! \n");
            }
            catch (Exception e) {
                System.err.println("Mensaje fallido! \n");
            }
            finally {
                writePort.closePort();
            }
        }
        catch(Exception e) {
            System.err.println("No se pudo abrir el puerto de escritura. \n");
        }
    }
     
     
      public void configurarEventoReceptor() {
        try {
            System.out.println("Configurando receptor...");
            readPort.openPort();
            readPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            readPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent e) {
                    String mensaje = "";
                    if (e.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
                    else {
                        System.out.println("Leyendo mensaje...");
                        try {
                            InputStream in = readPort.getInputStream();
                            System.out.print("Mensaje Recibido: \n String: ");
                            do {
                                mensaje += (char)in.read();
                            } while(readPort.bytesAvailable() > 0);
                            in.close();
                            System.out.println(mensaje + "\n");
                            filter(mensaje);
                        }
                        catch(IOException exception) {
                            System.err.println("Error leyendo mensaje!");
                        }
                        catch(Exception exception) {
                            System.err.println("Error leyendo mensaje!");
                        }
                    }
                }
            
            });     
            System.out.println("Receptor Configurado!");
        }
        catch (Exception e) {
            System.err.println("Error al configurar el evento para el puerto receptor!");
        }
    }
      /*test 2*/
    public void start (Game game) {
        UnoCard card = deck.drawCard();
        validColor = card.getColor();
        validValue = card.getValue();

        if (card.getValue() == UnoCard.Value.ChangeColor) start(game);
        

        if (card.getValue() == UnoCard.Value.DrawFour || card.getValue() == UnoCard.Value.DrawTwo) start(game);
        

        if (card.getValue() == UnoCard.Value.SkipTurn) {
            JLabel message = new JLabel(currentPlayer + " ha perdido el turno");
            message.setFont(new Font("Arial", Font.BOLD, 18));
            JOptionPane.showMessageDialog(null, message);

            if (gameDirection == false)     currentPlayer = "B";
            else if (gameDirection == true) currentPlayer = "D";
       
        }

        if (card.getValue() == UnoCard.Value.Reverse) {
            JLabel message = new JLabel(currentPlayer+ " ha cambiado la direccion");
            message.setFont(new Font("Arial", Font.BOLD, 18));
            JOptionPane.showMessageDialog(null, message);
            gameDirection = true;
            currentPlayer = "A"; 
        }

       stockPile.add(card);
    }
    
    public UnoCard getTopCard() {
        return new UnoCard(validColor, validValue); 
    }

    public ImageIcon getTopCardImage() {
        return new ImageIcon(validColor + "_" + validValue + ".png");
    }
    
    public String getCurrentPlayer() {
        return this.currentPlayer;
    }
    
    public String getPreviousPlayer() {
        return (gameDirection == false)?"B":"D";
    }

    public String[] getPlayers() {
        return playersId;
    }
    
    public ArrayList<UnoCard> getPlayerHand(String pid) {
        //System.out.println(playerHand);
        return playerHand.get(0);
    }

    public int getPlayerHandSize(String pid) {
        return getPlayerHand(pid).size();
    }

    public UnoCard getPlayerCard(String pid, int choice) {
        ArrayList<UnoCard> hand = getPlayerHand(pid);
        return hand.get(choice);
    }

    public boolean hasEmptyHand(String pid) {
        return getPlayerHand(pid).isEmpty();
    }

    public boolean validCardPlay (UnoCard card) {
        return card.getColor() == validColor || card.getValue() == validValue;
    }

    public void checkPlayerTurn (String pid) throws InvalidPlayerTurnException {
        if(this.currentPlayer != pid) {
            throw new InvalidPlayerTurnException("No es tu turno", pid);
        }
    }

    public boolean sayUno (String pid) {
        if (getPlayerHandSize(pid) == 1) {
            return true;
        }   
        return false;
    }
    

    public void submitDraw(String pid) throws InvalidPlayerTurnException {
        checkPlayerTurn(pid);

        if (deck.isEmpty()) {
            deck.replaceDeckWith(stockPile);
            deck.shuffle();
        }
        
        getPlayerHand(pid).add(deck.drawCard());
        
        /*if (gameDirection == false) {
            currentPlayer = "B";
        }
        else if (gameDirection == true) {
            currentPlayer = "D";
        }*/
    }

    public void setCardColor(UnoCard.Color color) {
        validColor = color;
    }

    public void submitPlayerCard (String pid, UnoCard card, UnoCard.Color declaredColor,UnoCard.Color newColor) 
        throws InvalidColorSubmissionException, InvalidPlayerTurnException, InvalidValueSubmissionException {
            checkPlayerTurn(pid);
              ArrayList<UnoCard> playerHand = getPlayerHand(pid);

            //Condicion para verificar que la carta se jugó corresponda al color y al numero correcto
            if (!validCardPlay(card)) {

                if (card.getColor() == UnoCard.Color.Wild) {
                    validColor = declaredColor;
                    validValue = card.getValue();
                }
                if (card.getColor() != validColor) {
                    JLabel message = new JLabel("Movimiento Invalido, color esperado " + validColor + " estas colocando" + card.getColor());
                    message.setFont(new Font("Arial", Font.BOLD, 18));
                    JOptionPane.showMessageDialog(null, message);
                    throw new InvalidColorSubmissionException("Movimiento Invalido, color esperado " + validColor + " estas colocando" + card.getColor(), card.getColor(), validColor);
                }
                else if (card.getValue() != validValue) {
                    JLabel message2 = new JLabel("Movimiento Invalido, numero esperado " + validValue + " estas colocando" + card.getValue());
                    message2.setFont(new Font("Arial", Font.BOLD, 18));
                    JOptionPane.showMessageDialog(null, message2);
                    throw new InvalidValueSubmissionException("Movimiento Invalido, numero esperado " + validValue + " estas colocando" + card.getValue(), card.getValue(), validValue);
                }
            }

            playerHand.remove(card);


            
            validColor = declaredColor;
            validValue = card.getValue();
            stockPile.add(card);
  
            
            
            //CONDICIONES PARA LA TRAMA
            //Condiciones para ver cual es el siguiente jugador
            //Cambiar dependiendo de el jugador.
            nextPlayer=(gameDirection == false)?"D":"B";
            
            String sentido=(gameDirection == true)?"1":"0";
            String coloJugado=AdaptTheColorsToTheTrama(validColor.toString());
            String colorDeclarado=AdaptTheColorsToTheTrama(declaredColor.toString());
            String value=AdaptTheValueToTheTrama(validValue.toString());
            String uno="0";
            String cantidadCartasEnMano=String.valueOf(getPlayerHandSize(pid));
            String trama="";
            
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
            */
            
            //AB0140NE11AZ1
            //BB010AM07AM0
            
            if(coloJugado.equals("NE") && colorDeclarado.equals("NE")) System.out.println("2ble negro");
            else{
            if(Integer.parseInt(cantidadCartasEnMano)<10)cantidadCartasEnMano="0"+cantidadCartasEnMano;
            //Condiciones para las cartas
            if (card.getColor() == UnoCard.Color.Wild) {
                colorDeclarado=AdaptTheColorsToTheTrama(validColor.toString());
            }

            if (card.getValue() == UnoCard.Value.DrawTwo) {
                 trama=currentPlayer+nextPlayer+uno+"12"+sentido+coloJugado+cantidadCartasEnMano+coloJugado+"1";
               // getPlayerHand(pid).add(deck.drawCard());
                //getPlayerHand(pid).add(deck.drawCard());
                JLabel message = new JLabel(pid + " ha tomado dos cartas");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
            }

            else if (card.getValue() == UnoCard.Value.DrawFour) {
                trama=currentPlayer+nextPlayer+uno+"14"+sentido+"NE"+cantidadCartasEnMano+colorDeclarado+"1";
                JLabel message = new JLabel(nextPlayer + " ha tomado cuatro cartas");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
            }
            
            else if(card.getValue() == UnoCard.Value.ChangeColor){
                
                trama=currentPlayer+nextPlayer+uno+"13"+sentido+"NE"+cantidadCartasEnMano+colorDeclarado+"0";
                JLabel message = new JLabel(nextPlayer + " ha cambiado de color");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
                
            }

            else if (card.getValue() == UnoCard.Value.SkipTurn) {
                 trama=currentPlayer+nextPlayer+uno+"11"+sentido+coloJugado+cantidadCartasEnMano+coloJugado+"1";
                JLabel message = new JLabel(nextPlayer + " ha perdido el turno");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
            }

            else  if (card.getValue() == UnoCard.Value.Reverse) {
                //Este lo voy a dejar para el final
                JLabel message = new JLabel(getPreviousPlayer() + " ha cambiado la direccion");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);

                gameDirection ^= true; //si va hacia delante 
                if(gameDirection == true) {
                   currentPlayer = "B";
                }
                else if (gameDirection == false) { //si va hacia atras
                    currentPlayer = "D";
                }
           }
            else{
                 trama=currentPlayer+nextPlayer+uno+value+sentido+coloJugado+cantidadCartasEnMano+coloJugado+"0";
            }
            enviarMensaje(trama);
            }
            
             if(hasEmptyHand(this.currentPlayer)) {
                JLabel message3 = new JLabel(this.currentPlayer + " ha ganado el juego");
                message3.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message3);
                System.exit(0);
            }
            
    }
   public String AdaptTheColorsToTheTrama(String color){
        if(color.equals("Red"))             return "RO";
        else if(color.equals("Blue"))       return "AZ";
        else if(color.equals("Yellow"))     return "AM";
        else if(color.equals("Green"))      return "VE";
        else if(color.equals("Wild"))       return "NE";
        return "1";
    }
   
   public String AdaptTheValueToTheTrama(String value){
        if(value.equals("Zero"))        return "00";
        else if(value.equals("One"))    return "01";
        else if(value.equals("Two"))    return "02";
        else if(value.equals("Three"))  return "03";
        else if(value.equals("Four"))   return "04";
        else if(value.equals("Five"))   return "05";
        else if(value.equals("Six"))    return "06";
        else if(value.equals("Seven"))  return "07";
        else if(value.equals("Eight"))  return "08";
        else if(value.equals("Nine"))   return "09";
        return "1";
    }
        
   //BA0060AM07AM0
   public void filter(String trama){
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
        int    nroCarta=parseInt(trama.substring(3,5));
        String tipoCarta=trama.substring(6,8);
        String colorCarta = null;
 
        String nuevoColorCarta=trama.substring(10,12);
        JLabel message = new JLabel();
        String nroCartaString=trama.substring(3,5);
        String jugador=trama.substring(1,2);
        String direccion=trama.substring(5,6);
        String jugadorOrigen=trama.substring(0,1);
        
        gameDirection=(direccion.equals("1"))?true:false;
        nextPlayer=(gameDirection == false)?"D":"B";
        
        UnoCard.Color color = AdaptTheTramaToTheColor(tipoCarta);
        UnoCard.Value value = AdaptTheTramaToTheValue(nroCartaString);
        
        UnoCard card= new UnoCard(color,value);
        validColor = card.getColor();
        validValue = card.getValue();
        System.out.println(currentPlayer+"-"+jugador);
        stockPile.add(card);
        String cantidadCartas=trama.substring(8,10);
        System.out.println("La cantidad de cartas que hay son de"+ cantidadCartas);
        if(cantidadCartas.equals("01")){
            message = new JLabel("El jugador "+jugadorOrigen+ "Tiene UNO");
            message.setFont(new Font("Arial", Font.BOLD, 18));
            
            JOptionPane.showMessageDialog(null, message);
        }
        
        if(cantidadCartas.equals("00")) {
                JLabel message3 = new JLabel("El jugador "+jugadorOrigen + " ha ganado el juego");
                message3.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message3);
                System.exit(0);
        }

        
        if(jugador.equals(currentPlayer)){
            if(nroCarta>9){
                switch (nroCarta) {
                    case 10:
                        System.out.println("Reversa de color " + colorCarta);
                        break;
                    case 11:
                        System.out.println("Bloqueo de color " + colorCarta);
                        message = new JLabel("Su turno ha sido bloqueado, no puede jugar");
                        message.setFont(new Font("Arial", Font.BOLD, 18));
                        JOptionPane.showMessageDialog(null, message);
                        break;
                    case 12:
                        System.out.println("+2 de color " + colorCarta);
                        getPlayerHand(currentPlayer).add(deck.drawCard());
                        getPlayerHand(currentPlayer).add(deck.drawCard());
                        message = new JLabel(currentPlayer + " ha tomado dos cartas");
                        message.setFont(new Font("Arial", Font.BOLD, 18));
                        JOptionPane.showMessageDialog(null, message);
                        break;
                    case 13:
                        System.out.println("Cambia de de color a " + nuevoColorCarta);

                        break;
                    case 14:
                         System.out.println("+4 con nuevo color de carta" + nuevoColorCarta);
                         getPlayerHand(currentPlayer).add(deck.drawCard());
                         getPlayerHand(currentPlayer).add(deck.drawCard());
                         getPlayerHand(currentPlayer).add(deck.drawCard());
                         getPlayerHand(currentPlayer).add(deck.drawCard());
                         message = new JLabel(currentPlayer + " ha tomado cuatro cartas");
                         message.setFont(new Font("Arial", Font.BOLD, 18));
                         JOptionPane.showMessageDialog(null, message);
                        break;
                    default:
                        break;
                }
            }
            else{
                System.out.println("Número básico");
            }
        }
        else enviarMensaje(trama);
        button.setIcon(new javax.swing.ImageIcon("C:\\Users\\usuario\\Documents\\NetBeansProjects\\j2\\src\\unoAttack\\images\\PNGs\\small\\" + getTopCardImage()));
        System.out.println(getTopCardImage());
        validColor = AdaptTheTramaToTheColor(nuevoColorCarta);
        gameStage.setButtonIcons();
   }
   
   public UnoCard.Color AdaptTheTramaToTheColor(String colorString){
        UnoCard.Color color = null;
        if(colorString.equals("AZ"))  return color.Blue;
        else if(colorString.equals("AM"))  return color.Yellow;
        else if(colorString.equals("VE"))  return color.Green;
        else if(colorString.equals("RO"))  return color.Red;
        else if(colorString.equals("NE"))  return color.Wild;
        return color.Wild;
   }
   
   public UnoCard.Value AdaptTheTramaToTheValue(String valueString){
        UnoCard.Value value = null;
        
        if(valueString.equals("00"))        return value.Zero;
        else if(valueString.equals("01"))   return value.One;
        else if(valueString.equals("02"))   return value.Two;
        else if(valueString.equals("03"))   return value.Three;
        else if(valueString.equals("04"))   return value.Four;
        else if(valueString.equals("05"))   return value.Five;
        else if(valueString.equals("06"))   return value.Six;
        else if(valueString.equals("07"))   return value.Seven;
        else if(valueString.equals("08"))   return value.Eight;
        else if(valueString.equals("09"))   return value.Nine;
        else if(valueString.equals("10"))   return value.Reverse;
        else if(valueString.equals("11"))   return value.SkipTurn;
        else if(valueString.equals("12"))   return value.DrawTwo;
        else if(valueString.equals("13"))   return value.ChangeColor;
        else if(valueString.equals("14"))   return value.DrawFour;
        
        return value;
   }

   
}


class InvalidPlayerTurnException extends Exception {
    String playerId;

    public InvalidPlayerTurnException(String message, String pid) {
        super(message);
        playerId = pid;
    }

    public String getPid() {
        return playerId;
    }
}

class InvalidColorSubmissionException extends Exception {
    private UnoCard.Color expected;
    private UnoCard.Color actual;

    public InvalidColorSubmissionException(String message, UnoCard.Color actual, UnoCard.Color expected) {
        this.actual = actual;
        this.expected = expected;
    }

    public UnoCard.Color getExpected() {
        return this.expected;
    }

    public UnoCard.Color getActual() {
        return this.actual;
    }
}

class InvalidValueSubmissionException extends Exception {
    private UnoCard.Value expected;
    private UnoCard.Value actual;

    public InvalidValueSubmissionException(String message, UnoCard.Value actual, UnoCard.Value expected) {
        this.actual = actual;
        this.expected = expected;
    }

    public UnoCard.Value getExpected() {
        return this.expected;
    }

    public UnoCard.Value getActual() {
        return this.actual;
    }
}