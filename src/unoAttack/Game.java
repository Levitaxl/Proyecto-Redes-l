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
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Game extends Thread  {
    
    private String currentPlayer;
    private String[] playersId;

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

        currentPlayer = "00";
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
    
    // public boolean isGameOver() {
    //     for (String player: this.playersId) {
    //         if(playerHand.size() == 0 ) {

    //         }
    //     }
    // }

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

            validColor = card.getColor();
            validValue = card.getValue();
            stockPile.add(card);

            if (gameDirection == false) {
                currentPlayer = "01";
            }
            else if (gameDirection == true) {
                currentPlayer = "11";
            }

            if (card.getColor() == UnoCard.Color.Wild) {
                validColor = declaredColor;
            }

            if (card.getValue() == UnoCard.Value.DrawTwo) {
                pid = currentPlayer;
                getPlayerHand(pid).add(deck.drawCard());
                getPlayerHand(pid).add(deck.drawCard());
                JLabel message = new JLabel(pid + " ha tomado dos cartas");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
            }

            if (card.getValue() == UnoCard.Value.DrawFour) {
                pid = currentPlayer;
                getPlayerHand(pid).add(deck.drawCard());
                getPlayerHand(pid).add(deck.drawCard());
                getPlayerHand(pid).add(deck.drawCard());
                getPlayerHand(pid).add(deck.drawCard());
                JLabel message = new JLabel(pid + " ha tomado cuatro cartas");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
            
            }

              if (card.getValue() == UnoCard.Value.SkipTurn) {
                JLabel message = new JLabel(currentPlayer + " ha perdido el turno");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);

                if(gameDirection == false) {
                    currentPlayer = "01";
                }
                else if (gameDirection == true) {
                    currentPlayer = "11";
                }
            }

             if (card.getValue() == UnoCard.Value.Reverse) {
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


