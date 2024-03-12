package polimi.ingsw.am21.codex.model.Cards;

import java.util.ArrayList;
import java.util.List;

public class PlayedCard {

    SidedCard playedCard;
    CardSide playedSide;

    Position relativePosition;
    List<PlayedCard> adjacentCards = new ArrayList<>(2);


    PlayedCard(SidedCard playedCard, CardSides playedSide, PlayedCard linkingCard, CornerEnum cornerNumber){
        relativePosition = linkingCard.relativePosition.computeLinkingPosition(cornerNumber);
    }




}
