package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.CornerContentTypes;
import polimi.ingsw.am21.codex.model.Cards.PlayedCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.SidedCard;

import java.util.*;

public class PlayerBoard {

    private final int MAX_CARDS = 3;
    private List<SidedCard> cards = new ArrayList<SidedCard>(MAX_CARDS);
    private ObectiveCard objectiveCard;

    Map <Position, PlayedCard> playedCards = new HashMap<>();
    
   // Hashmaps to keep track of resources
    private HashMap<ResourceTytoggle, Integer> resources = new HashMap<>(ResourceTypes.values().lenght);
    private HashMap<ObjectTypes, Integer> objects = new HashMap<>(ObjectTypes.values().lenght);

    // List of all available corners
    List<Corner> availableCorners = new LinkedList<Corner>();

    //Occupied Relative Position
    Set<Position> = new Set<>();

    PlayedCard placedCardsGraph;

    private PlayerBoard(StarterCard starterCard) {
        this.playedCardsGraph = starterCard;
    }

    void drawCard(SidedCard card){
        cards.add(card);
    }

    void placeCard(SidedCard playedCard, CardSide playedSide, PlayedCard linkingCard, CornerEnum cornerNumber){

        playedSide = new PlayedCard(
                playedCard,
                playedSide,

        );

        linkingCorner = playedCard.getPlayedSide().getCorner(cornerNumber);

        // linking the card and removing the linking corner from the available ones.
        linkingCorner.link(playedCard);
        availableCorners.remove(linkingCorner);

        //try looking for adjacent card:
        playedCard.relativePosition

        // removing the content of the covered content from resources and objects
        updateResourcesAndObjects(linkingCorner, -1);

        // updating the resources for every corner and putting them (besides the connected one) in the list of available ones
        for(Corner corner : playedCard.corners) {
            updateResourcesAndObjects(corner, +1);
            if(linkingCorner.getLinkedCorner() != corner){
                availableCorners.add(corner);
            }
        }
    }

    void updateResourcesAndObjects(Corner corner, int update){
        if(corner.content.isPresent()){
            switch (corner.contentType){
                case CornerContentTypes.OBJECT: objects[corner.content] += update; break;
                case CornerContentTypes.RESOURCE: resources[corner.content] += update; break;
            }
        }
    }

    void updateLink
}


