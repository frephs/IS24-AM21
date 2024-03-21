package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;
import java.util.*;

public class PlayerBoard {

    private final int MAX_CARDS = 3;
    private List<PlayableCard> hand = new ArrayList<PlayableCard>(MAX_CARDS);
    private ObjectiveCard objectiveCard;

    Map <Position, PlayableCard> playedCards = new HashMap<>();
    
   // Hashmaps to keep track of resources
    private HashMap<ResourceType, Integer> resources = new HashMap<>(ResourceType.values().lenght);
    private HashMap<ObjectType, Integer> objects = new HashMap<>(ObjectType.values().lenght);

    // List of all available spots in which a card can be placed
    Set<Position> availableSpots = new HashSet<>();
    HashMap<Position, PlayableCard>  placedCards = new HashMap<Position, PlayableCard>();

    /**
     * @param hand the player's cards drawn from the GameBoard (2 resources and 1 goldcard)
     * @param starterCard drawn from the playerboard
     * @param objectiveCard chosen by the client controller (physical player)
     */
    PlayerBoard(List<PlayableCard> hand, PlayableCard starterCard, ObjectiveCard objectiveCard) {
        this.hand = hand;
        this.playedCards.set(new Position(), starterCard);
        this.objectiveCard = objectiveCard;
    }

    /**
     * @return the player's secret objective
     * */
    public ObjectiveCard getObjectiveCard() {
        return objectiveCard;
    }

    /**
     * return the player's hand
     * */
    public List<PlayableCard> getHand(){
        return this.hand;
    }

    /**
     * @param card which is added to the player's hand
     * */
    void drawCard(PlayableCard card){
        hand.add(card);
    }

    /**
     * @param playedCard chosen from the player's hand, will be evaluated after placement
     * @param playedSide of the card chosen to be placed on the PlayerBoard
     * @param position of the PlayerBoard in which the card will be placed by the PlayerBoard
     */
    void placeCard(PlayableCard playedCard, CardSidesType playedSide, Position position){
        this.hand.remove(playedCard);

        playedCard.setPlayedSide(CardSidesType);
        this.playedCards.put(position, playedCard);

        updateAvailableSpots(position);
        updateResourcesAndObjects(playedCard, position);

    }
    
    //FIXME: maybe order it a little, maybe iterate over the corners once int the placecard method
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
        if(ResourceType.has(Corner.getContent())){
            ResourceType resource = Corner.getContent();
            int prevVal = this.resources.get(resource);
            this.resources.put(resource, prevVal+update);
        }else if(ObjectType.has(Corner.getContent())) {
            ObjectType object = Corner.getContent();
            int prevVal = this.objects.get(object);
            this.objects.put(object, prevVal+update);
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
}




