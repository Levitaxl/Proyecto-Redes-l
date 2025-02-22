/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unoAttack;

public class UnoCard {
    

    enum Color {

        Red, Blue, Yellow, Green, Wild;

        private static final Color[] colors = Color.values();
        public static Color getColor(int i) {
            return Color.colors[i];
        }

    }

    enum Value {

        Zero, One, Two, Three, Four, Five, Six, Seven, Eight, Nine, DrawTwo, SkipTurn, Reverse, ChangeColor, DrawFour;

        private static final Value[] values = Value.values();

        public static Value getValue (int i) {
            return Value.values[i];
        }

    }

    private final Color color;
    private final Value value;

    public UnoCard(final Color color, final Value value) {
        this.color = color;
        this.value = value;
    }

    public Color getColor() {
        return this.color;
    }

    public Value getValue() {
        return this.value;
    }

    public String toString() {
        return color + "_" + value;
    }

}

