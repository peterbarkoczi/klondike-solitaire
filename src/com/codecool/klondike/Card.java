package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private String suit;
    private int rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(String suit, int rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public String getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return suit + Integer.toString(rank);
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank" + rank + " of " + "Suit" + suit;
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        //TODO
        return true;
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> allCards = new ArrayList<>();
        for (Suits suit : Suits.values()) {
            String suitName = suit.toString();
            for (int rank = 1; rank < 14; rank++) {
                allCards.add(new Card(suitName, rank, true));
            }
        }

        List<Card> shuffledDeck = new ArrayList<Card>();

        while (allCards.size() > 0) {
            Random random = new Random();
            int randomNumber = random.nextInt(allCards.size());
            shuffledDeck.add(allCards.get(randomNumber));
            allCards.remove(randomNumber);
        }

        return shuffledDeck;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        for (Suits suit : Suits.values()) {
            String suitName = suit.toString();

            for (Ranks rank : Ranks.values()) {
                String cardName = suitName.toLowerCase() + rank.Value;
                String cardId = suit + Integer.toString(rank.Value);
                String imageFileName = "card_images/" + cardName + ".png";
                System.out.println(cardName);
                System.out.println(cardId);
                System.out.println(imageFileName);
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }
    }

    public enum Suits {
        HEARTS,
        DIAMONDS,
        SPADES,
        CLUBS
    }

    public enum Ranks {
        ACE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(11),
        QUEEN(12),
        KING(13);

        public final int Value;

        Ranks(int value) {
            Value = value;
        }
    }

}
