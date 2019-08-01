package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;

    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile actualContainingPile = card.getContainingPile();
        if (actualContainingPile.getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }

        if (actualContainingPile.getPileType() == Pile.PileType.TABLEAU && card.isFaceDown() &&
                actualContainingPile.getTopCard() == card) {
            card.flip();
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        if (!card.isFaceDown())
            draggedCards.add(card);

        for (Card flippedCard: card.getContainingPile().getCards()) {
            if (activePile.getPileType() == Pile.PileType.TABLEAU &&
                    !flippedCard.isFaceDown() &&
                    (flippedCard.getRank() <= card.getRank())) {
                draggedCards.add(flippedCard);
            }
        }

        for (Card cardInList : draggedCards) {
            cardInList.getDropShadow().setRadius(20);
            cardInList.getDropShadow().setOffsetX(10);
            cardInList.getDropShadow().setOffsetY(10);
            cardInList.toFront();
            cardInList.setTranslateX(offsetX);
            cardInList.setTranslateY(offsetY);
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile tableauPile = getValidIntersectingPile(card, tableauPiles);
        Pile foundationPile = getValidIntersectingPile(card, foundationPiles);


        if (isMoveValid(card, tableauPile)) {
            handleValidMove(card, tableauPile);
        } else if (isMoveValid(card, foundationPile)) {
            handleValidMove(card, foundationPile);
            if (isGameWon(foundationPiles)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Klondike Solitaire");
                alert.setHeaderText(null);
                alert.setContentText("You Won!");
                alert.showAndWait();
            }
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    };

    public boolean isGameWon(List<Pile> list) {
        int count = 1;
        for (Pile field: list) {
            if (!field.isEmpty() && field.getTopCard().getRank() == 13) {
                count++;
            }
        }

        if (count == 4) {
            System.out.println("You won!");
            return true;
        }
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        while (!discardPile.isEmpty()) {
            List<Card> discardedCards = discardPile.getCards();
            Card actualCard = discardedCards.get(0);
            actualCard.flip();
            actualCard.moveToPile(stockPile);
        }
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile != null && destPile.getPileType().equals(Pile.PileType.TABLEAU) && !destPile.isEmpty() &&
                Card.isOppositeColor(card, destPile.getTopCard()) &&
                Card.isNextRank(card, destPile.getTopCard()) ||
                (destPile != null && destPile.isEmpty() && card.getRank() == 13)) {
            return true;
        } else if (destPile != null && destPile.getPileType().equals(Pile.PileType.FOUNDATION) && !destPile.isEmpty() &&
                Card.isSameSuit(card, destPile.getTopCard()) &&
                Card.isNextRankFoundation(card, destPile.getTopCard()) ||
                (destPile != null && destPile.isEmpty() && card.getRank() == 1)) {
            return true;
        }
        return false;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }

        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();

        int counter = 1;

        for (int j = 0; j < tableauPiles.size(); j++) {
            Pile actualPile = tableauPiles.get(j);

            for (int i = 0; i < counter; i++) {
                Card actualCard = deckIterator.next();
                addMouseEventHandlers(actualCard);
                actualPile.addCard(actualCard);
                getChildren().add(actualCard);
            }

            counter++;
            actualPile.getTopCard().flip();

        }

        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
