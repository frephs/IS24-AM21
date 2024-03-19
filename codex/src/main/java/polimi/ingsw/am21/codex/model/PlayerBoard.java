package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;

import java.util.*;

public class PlayerBoard {

    private final int MAX_CARDS = 3;
    private List<PlayableCard> cards = new ArrayList<PlayableCard>(MAX_CARDS);
    private ObjectiveCard objectiveCard;

    Map <Position, PlayedCard> playedCards = new HashMap<>();
    
   // Hashmaps to keep track of resources
    private HashMap<ResourceTypes, Integer> resources = new HashMap<>(ResourceTypes.values().lenght);
    private HashMap<ObjectTypes, Integer> objects = new HashMap<>(ObjectTypes.values().lenght);

    // List of all available spots in which a card can be placed
    Set<Position> availableSpots = new HashSet<>();
    HashMap<Position, PlayedCard>  placedCards = new HashMap<>();


    private PlayerBoard(PlayableCard[] cards, StarterCard starterCard) {
        for(PlayableCard card : cards){
            this.cards.add(card);
        }
        this.playedCards.set(new Position(), starterCard);
    }

    public void setObjectiveCard(ObjectiveCard objectiveCard) {
        this.objectiveCard = objectiveCard;
    }

    public ObjectiveCard getObjectiveCard() {
        return objectiveCard;
    }

    void drawCard(PlayableCard card){
        cards.add(card);
    }

    void placeCard(PlayableCard playedCard, CardSidesTypes playedSide, Position position){
        this.cards.remove(playedCard);

        playedCard.setPlayedSide(CardSidesTypes);
        this.playedCards.put(position, playedCard);

        updateAvailableSpots(position);
        updateResourcesAndObjects(playedCard, position);

    }

    //FIXME: maybe order it a little
    void updateResourcesAndObjects(PlayableCard playedCard, Position position) {
        playedCard.getPlayedSide().getCorners().foreach(
                (cornerPosition, corner) ->{
                    updateResourcesAndObjectsMaps(corner, +1);
                    Position linkingCardPosition = cornerPosition.computeLinkingPosition(cornerPosition);

                    if(!availableSpots.contains(linkingCardPosition)){
                        // we need to remove its covered contents
                        CornerEnum linkingCorner = CornerEnum.getOppositeCorner(cornerPosition);
                        PlayableCard linkedCard= this.playedCards.get(linkingCardPosition);
                        Corner linkedCorner = linkedCard.getPlayedSide().getCorner(linkingCorner);

                        updateResourcesAndObjectsMaps(linkedCorner, -1);
                    }
                }
        );
    }

    void updateResourcesAndObjectsMaps(Corner corner, int update){
        if(ResourceTypes.has(Corner.getContent())){
            ResourceTypes resource = Corner.getContent();
            this.resources[resource] += update;
        }else if(ObjectTypes.has(Corner.getContent())) {
            ObjectTypes object = Corner.getContent();
            this.objects[object] += update;
        }
    }

    void updateAvailableSpots(Position position){
        availableSpots.remove(position);
        for (CornerEnum adjacentCorner : CornerEnum.values()) {
            Position adjacentCardPosition = position.computeLinkingPosition((adjacentCorner));
            if(!availableSpots.contains(adjacentCardPosition)){
                if(! playedCards.containsKey(adjacentCardPosition)){
                    availableSpots.add(adjacentCardPosition);
                }else{
                    availableSpots.remove(adjacentCardPosition);
                }
            }
        }
    }

    int evaluate(ObjectiveCard card){
        return card.evaluate(playedCards);
    }

    int evaluate(PlayableCard card){
        return card.evaluate(objects); // set coveredCorners
    }

}




