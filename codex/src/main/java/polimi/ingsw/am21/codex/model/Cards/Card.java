package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.PlayerBoard;

public abstract class Card {
    final int id;

    public Card(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    abstract public int evaluate(PlayerBoard playerBoard);
}
