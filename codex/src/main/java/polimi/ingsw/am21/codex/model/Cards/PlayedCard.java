package polimi.ingsw.am21.codex.model.Cards;

public class PlayedCard {

    SidedCard playedCard;
    CardSide playedSide;

    RelativePosition relativePosition;
    List<PlayedCard> adjacentCards;


    PlayedCard(SidedCard playedCard, CardSides playedSide, PlayedCard linkingCard, CornerEnum cornerNumber){

        relativePosition = linkingCard.relativePosition.computeLinkingPosition(cornerNumber);
    }


}
