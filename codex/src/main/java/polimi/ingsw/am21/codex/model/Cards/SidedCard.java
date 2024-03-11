package polimi.ingsw.am21.codex.model.Cards;

public class SidedCard extends Card{
    final int sides = 2;
    CardSide cardSide[sides];

    SidedCard(){
        this.sides[0] = new CardSide();
        this.sides[1] = new CardBackSIde ()
    }

}
