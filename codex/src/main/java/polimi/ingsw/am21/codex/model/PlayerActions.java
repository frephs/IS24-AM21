package polimi.ingsw.am21.codex.model;

public interface PlayerActions {
    // will be implemented by the controller
    ObjectiveCard chooseObjectiveCard(ObjectiveCard cards, int choice);
    DrawingSource chooseDrawingSource(int source);
    DeckType chooseDrawingDeck(int deck);

    SidedCard choosePlayingCard(int choice);
    CardSidesTypes choosePlayedCardSides(int choice);

}
