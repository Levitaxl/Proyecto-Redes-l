/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoAttack;

import java.util.Random;
import java.util.ArrayList;
import javax.swing.ImageIcon;


public class UnoDeck
{
  private UnoCard[] cards;
  private int cardsInDeck;

    /**
     *
     */
    public UnoDeck()
  {
    //Inicializa el array en 108 (cantidad total de cartas)
    cards = new UnoCard[108];
    reset();
  }

   
    public void reset() {
    //Se crea un array de los colores 
    UnoCard.Color[] colors = UnoCard.Color.values();
    //Se inicializa el mazo             
    cardsInDeck = 0;
    //Se pasa por los colores menos los wild
    for (int i = 0; i < colors.length-1; i++)
    {
      //El color de la carta sera el actual del indice del array
      UnoCard.Color color = colors[i];

      // Agrega 1 Zero 
      cards[cardsInDeck++] = new UnoCard(color, UnoCard.Value.getValue(0));
      // Agrega dos cartas de del 1-9
      for (int j = 1; j < 10; j++)
      {
        cards[cardsInDeck++] = new UnoCard(color, UnoCard.Value.getValue(j));
        cards[cardsInDeck++] = new UnoCard(color, UnoCard.Value.getValue(j));
      }
      // Agrega DrawTwo, SkipTurn y Reverse al mazo
      UnoCard.Value[] values = new UnoCard.Value[]{UnoCard.Value.DrawTwo, UnoCard.Value.SkipTurn, UnoCard.Value.Reverse};
      for (UnoCard.Value value : values)
      {
        cards[cardsInDeck++] = new UnoCard(color, value);
        cards[cardsInDeck++] = new UnoCard(color, value);
      }
    }

    // Agrega las cartas Wild al mazo
    UnoCard.Value[] values = new UnoCard.Value[]{UnoCard.Value.ChangeColor, UnoCard.Value.DrawFour};
    for (UnoCard.Value value : values)
    {
      for (int i = 0; i < 4; i++)
      {
        cards[cardsInDeck++] = new UnoCard(UnoCard.Color.Wild, value);
      }
    }
  }


    /**
     *
     * @param cards (stockpile)
     * 
     */
    public void replaceDeckWith(ArrayList<UnoCard> cards) {
      this.cards = cards.toArray(new UnoCard[cards.size()]);
      this.cardsInDeck = this.cards.length;
    }

    /**
     *
     * @return true if there are no cards in the deck
     */
    public boolean isEmpty() {
      return cardsInDeck == 0;
    }

    public void shuffle() {
      int n = cards.length;
      Random random = new Random();

      for (int i = 0; i < cards.length; i++) {

        //Baraja el mazo

        int randomValue = i + random.nextInt(n - i);
        UnoCard randomCard = cards[randomValue];
        cards[randomValue] = cards[i];
        cards[i] = randomCard;
      }

    }

    public UnoCard drawCard() throws IllegalArgumentException {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot draw a card since there are no cards in the deck");
      }
      return cards[--cardsInDeck];
    }

    public ImageIcon drawCardImage() throws IllegalArgumentException {
      if(isEmpty()) {
        throw new IllegalArgumentException("Cannot draw a card since the deck is empty");
      }
      return new ImageIcon(cards[--cardsInDeck].toString() + ".png");
    }

    public UnoCard[] drawCard(int n) {
      if (n < 0) {
        throw new IllegalArgumentException("Must draw positiive cards but tried to draw " + n + " cards.");
      }

      if (n > cardsInDeck) {
        throw new IllegalArgumentException("Cannot draw " + n + " cards since there are only " + cardsInDeck + " cards.");
      }

      UnoCard[] ret = new UnoCard[n];

      for (int i = 0; i < n; i++) {
        ret[i] = cards[--cardsInDeck];
      }
      return ret;
    }
}
