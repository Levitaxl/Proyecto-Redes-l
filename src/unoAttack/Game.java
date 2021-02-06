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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static unoAttack.Connector.sentMessage;

public class Game extends Thread  {
    
    private String currentPlayer;
    private String[] playersId;
    private String nextPlayer;

    private UnoDeck deck;
    private ArrayList<ArrayList<UnoCard>> playerHand;
    private ArrayList<UnoCard> stockPile;

    private UnoCard.Color validColor;
    private UnoCard.Value validValue;
    
    private final SerialPort readPort = SerialPort.getCommPorts()[7];
    private final SerialPort writePort = SerialPort.getCommPorts()[0];

    boolean gameDirection;

    public Game() {
        deck = new UnoDeck();
        deck.shuffle();
        stockPile = new ArrayList<UnoCard>();

        currentPlayer = "A";
        gameDirection = false;
        configurarEventoReceptor();
        this.readPort.openPort();
        System.out.println(readPort.getSystemPortName());

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
                            //checkAction(mensaje);
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

            if (gameDirection == false)     currentPlayer = "11";
            else if (gameDirection == true) currentPlayer = "01";
       
        }

        if (card.getValue() == UnoCard.Value.Reverse) {
            JLabel message = new JLabel(currentPlayer+ " ha cambiado la direccion");
            message.setFont(new Font("Arial", Font.BOLD, 18));
            JOptionPane.showMessageDialog(null, message);
            gameDirection = true;
            currentPlayer = "01"; 
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
        return "11";
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

    public void submitDraw(String pid) throws InvalidPlayerTurnException {
        checkPlayerTurn(pid);

        if (deck.isEmpty()) {
            deck.replaceDeckWith(stockPile);
            deck.shuffle();
        }
        
        getPlayerHand(pid).add(deck.drawCard());
        
        if (gameDirection == false) {
            currentPlayer = "01";
        }
        else if (gameDirection == true) {
            currentPlayer = "11";
        }
    }

    public void setCardColor(UnoCard.Color color) {
        validColor = color;
    }

    public void submitPlayerCard (String pid, UnoCard card, UnoCard.Color declaredColor) 
        throws InvalidColorSubmissionException, InvalidPlayerTurnException, InvalidValueSubmissionException {
            checkPlayerTurn(pid);
            
      

            ArrayList<UnoCard> playerHand = getPlayerHand(pid);
            
            
            //Condicion para verificar que la carta se jugó corresponda al color y al numero correcto
            if (!validCardPlay(card)) {

                if (card.getColor() == UnoCard.Color.Wild) {
                    validColor = card.getColor();
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

            if(hasEmptyHand(this.currentPlayer)) {
                JLabel message3 = new JLabel(this.currentPlayer + " ha ganado el juego");
                message3.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message3);
                System.exit(0);
            }
            
            System.err.println(card.getColor());
            System.err.println(card.getValue());
            validColor = card.getColor();
            validValue = card.getValue();
            stockPile.add(card);
            
            
            //System.out.println(validValue);
            
            
            
            //CONDICIONES PARA LA TRAMA
            //Condiciones para ver cual es el siguiente jugador
            //Cambiar dependiendo de el jugador.
            nextPlayer=(gameDirection == false)?"B":"D";
            
            String sentido=(gameDirection == true)?"1":"0";
            String coloJugado=AdaptTheColorsToTheTrama(validColor.toString());
            String colorDeclarado=AdaptTheColorsToTheTrama(declaredColor.toString());
            String value=AdaptTheValueToTheTrama(validValue.toString());
            String uno="0";
            String cantidadCartasEnMano=String.valueOf(getPlayerHandSize(pid));
            String trama="";
            
            
            if(Integer.parseInt(cantidadCartasEnMano)<10)cantidadCartasEnMano="0"+cantidadCartasEnMano;
            
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
            
            //Condiciones para las cartas
            if (card.getColor() == UnoCard.Color.Wild) {
                colorDeclarado=AdaptTheColorsToTheTrama(declaredColor.toString());
                trama=currentPlayer+nextPlayer+uno+sentido+"13"+coloJugado+cantidadCartasEnMano+colorDeclarado+"0";
            }

            if (card.getValue() == UnoCard.Value.DrawTwo) {
                 trama=currentPlayer+nextPlayer+uno+sentido+"12"+coloJugado+cantidadCartasEnMano+coloJugado+"1";
                System.out.println(trama);
                JLabel message = new JLabel(pid + " ha tomado dos cartas");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
            }

            else if (card.getValue() == UnoCard.Value.DrawFour) {
                 trama=currentPlayer+nextPlayer+uno+sentido+"14"+coloJugado+cantidadCartasEnMano+colorDeclarado+"1";
                JLabel message = new JLabel(pid + " ha tomado cuatro cartas");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
            }

            else if (card.getValue() == UnoCard.Value.SkipTurn) {
                 trama=currentPlayer+nextPlayer+uno+sentido+"11"+coloJugado+cantidadCartasEnMano+coloJugado+"1";
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
                   currentPlayer = "01";
                }
                else if (gameDirection == false) { //si va hacia atras
                    currentPlayer = "11";
                }
           }
            else{
                 trama=currentPlayer+nextPlayer+uno+sentido+value+coloJugado+cantidadCartasEnMano+coloJugado+"0";
            }
            
            System.out.println(trama);
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


