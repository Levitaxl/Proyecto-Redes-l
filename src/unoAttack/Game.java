/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoAttack;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Game {
    
    private int currentPlayer;
    private String[] playersId;

    private UnoDeck deck;
    private ArrayList<ArrayList<UnoCard>> playerHand;
    private ArrayList<UnoCard> stockPile;

    private UnoCard.Color validColor;
    private UnoCard.Value validValue;

    boolean gameDirection;

    public Game(String[] pids) {
        deck = new UnoDeck();
        deck.shuffle();
        stockPile = new ArrayList<UnoCard>();

        playersId = pids;
        currentPlayer = 0;
        gameDirection = false;

        playerHand = new ArrayList<ArrayList<UnoCard>>();

        for (int i = 0; i < pids.length; i++) {
            ArrayList<UnoCard> hand = new ArrayList<UnoCard>(Arrays.asList(deck.drawCard(7)));
            playerHand.add(hand);

        }
    }

    public void start (Game game) {
        UnoCard card = deck.drawCard();
        validColor = card.getColor();
        validValue = card.getValue();

        if (card.getValue() == UnoCard.Value.ChangeColor) {
            start(game);
        }

        if (card.getValue() == UnoCard.Value.DrawFour || card.getValue() == UnoCard.Value.DrawTwo) {
            start(game);
        }

        if (card.getValue() == UnoCard.Value.SkipTurn) {
            JLabel message = new JLabel(playersId[currentPlayer] + " ha perdido el turno");
            message.setFont(new Font("Arial", Font.BOLD, 18));
            JOptionPane.showMessageDialog(null, message);

            if (gameDirection == false) {
                currentPlayer = (currentPlayer + 1) % playersId.length;
            }
            else if (gameDirection == true) {
                currentPlayer = (currentPlayer + 1) % playersId.length;
                if (currentPlayer == -1) {
                    currentPlayer = playersId.length - 1;
                }
            }
        }

        if (card.getValue() == UnoCard.Value.Reverse) {
            JLabel message = new JLabel(playersId[currentPlayer] + " ha cambiado la direccion");
            message.setFont(new Font("Arial", Font.BOLD, 18));
            JOptionPane.showMessageDialog(null, message);
            gameDirection ^= true;
            currentPlayer = playersId.length - 1; 
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
        return this.playersId[this.currentPlayer];
    }

    public String getPreviousPlayer(int i) {
        int index = this.currentPlayer - i;
        if (index == 1) {
            index = playersId.length -1;
        }
        return this.playersId[index];
    }

    public String[] getPlayers() {
        return playersId;
    }

    public ArrayList<UnoCard> getPlayerHand(String pid) {
        int index = Arrays.asList(playersId).indexOf(pid);
        return playerHand.get(index);
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
        if(this.playersId[this.currentPlayer] != pid) {
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
            currentPlayer = (currentPlayer + 1) % playersId.length;
        }
        else if (gameDirection == true) {
            currentPlayer = (currentPlayer - 1) % playersId.length;
            if (currentPlayer == -1) {
                currentPlayer = playersId.length -1;
            }
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

            if(hasEmptyHand(this.playersId[currentPlayer])) {
                JLabel message3 = new JLabel(this.playersId[currentPlayer] + " ha ganado el juego");
                message3.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message3);
                System.exit(0);
            }

            validColor = card.getColor();
            validValue = card.getValue();
            stockPile.add(card);

            if (gameDirection == false) {
                currentPlayer = (currentPlayer + 1) % playersId.length;
            }
            else if (gameDirection == true) {
                currentPlayer = (currentPlayer - 1) % playersId.length;
                if (currentPlayer == -1) {
                    currentPlayer = currentPlayer - 1;
                }
            }

            if (card.getColor() == UnoCard.Color.Wild) {
                validColor = declaredColor;
            }

            if (card.getValue() == UnoCard.Value.DrawTwo) {
                pid = playersId[currentPlayer];
                getPlayerHand(pid).add(deck.drawCard());
                getPlayerHand(pid).add(deck.drawCard());
                JLabel message = new JLabel(pid + " ha tomado dos cartas");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
            }

            if (card.getValue() == UnoCard.Value.DrawFour) {
                pid = playersId[currentPlayer];
                getPlayerHand(pid).add(deck.drawCard());
                getPlayerHand(pid).add(deck.drawCard());
                getPlayerHand(pid).add(deck.drawCard());
                getPlayerHand(pid).add(deck.drawCard());
                JLabel message = new JLabel(pid + " ha tomado cuatro cartas");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);
            }

            if (card.getValue() == UnoCard.Value.SkipTurn) {
                JLabel message = new JLabel(playersId[currentPlayer] + " ha perdido el turno");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);

                if(gameDirection == false) {
                    currentPlayer = (currentPlayer + 1) % playersId.length;
                }
                else if (gameDirection == true) {
                    currentPlayer = (currentPlayer -1) % playersId.length;
                    if(currentPlayer == -1) {
                        currentPlayer = playersId.length - 1;
                    }
                }

            }

            if (card.getValue() == UnoCard.Value.Reverse) {
                JLabel message = new JLabel(getPreviousPlayer(currentPlayer) + " ha cambiado la direccion");
                message.setFont(new Font("Arial", Font.BOLD, 18));
                JOptionPane.showMessageDialog(null, message);

                gameDirection ^= true; //si va hacia delante 
                if(gameDirection == true) {
                    currentPlayer = (currentPlayer - 2 ) % playersId.length;
                    if (currentPlayer == -1) {
                        currentPlayer = playersId.length - 1;
                    }

                    if (currentPlayer == -2) {
                        currentPlayer = playersId.length - 2;
                    }
                }
                else if (gameDirection == false) { //si va hacia atras
                    currentPlayer = (currentPlayer + 1) % playersId.length;
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


