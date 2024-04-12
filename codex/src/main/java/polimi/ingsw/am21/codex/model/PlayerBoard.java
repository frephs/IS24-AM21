package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;
import java.util.*;

public class PlayerBoard {

    //private final int MAX_CARDS = 3;
    private final List<PlayableCard> hand;
    private final ObjectiveCard objectiveCard;

    Map<Position, PlayableCard> playedCards = new HashMap<>();

   // Hashmaps to keep track of resources
    private final HashMap<ResourceType, Integer> resources = new HashMap<>(ResourceType.values().length);
    private final HashMap<ObjectType, Integer> objects = new HashMap<>(ObjectType.values().length);

    // List of all available spots in which a card can be placed
    Set<Position> availableSpots = new HashSet<>();
    Set<Position> forbiddenSpots = new HashSet<>();

    /**
     * @param hand the player's cards drawn from the GameBoard (2 resources and 1 GoldCard)
     * @param starterCard drawn from the PlayerBoard
     * @param objectiveCard chosen by the client controller (physical player)
     */
    public PlayerBoard(List<PlayableCard> hand, PlayableCard starterCard, ObjectiveCard objectiveCard) {
        this.hand = hand;
        this.playedCards.put(new Position(), starterCard);
        this.objectiveCard = objectiveCard;
        
        // let's initialize the maps with resources to 0
        Arrays.stream(ResourceType.values()).forEach(
            (resourceType) -> resources.put(resourceType, 0)
        );
        Arrays.stream(ObjectType.values()).forEach(
            (objectType) -> objects.put(objectType, 0)
        );
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
     * @param playedSideType of the card chosen to be placed on the PlayerBoard
     * @param position of the PlayerBoard in which the card will be placed by the PlayerBoard
     */
    void placeCard(PlayableCard playedCard, CardSideType playedSideType, Position position){

        this.hand.remove(playedCard);
        playedCard.setPlayedSideType(playedSideType);
        PlayableSide playedSide = playedCard.getPlayedSide().get();

        this.playedCards.put(position, playedCard);

        updateAvailableSpots(playedSide, position);
        updateResourcesAndObjects(playedSide, position);

    }

      /**
       * Helper method called by PlayerBoard.placeCard() to update the player's available resources and objects
       * */
      private void updateResourcesAndObjects(PlayableSide playedSide, Position position) {
        HashMap<CornerPosition, Corner> enabledCorners = playedSide.getCorners();

        //let's add the resources of the card just placed
        enabledCorners.forEach(
                (cornerPosition, corner) -> updateResourcesAndObjectsMaps(corner, +1)
        );

        //let's remove the resources of the cards that are covered.
        Arrays.stream(CornerPosition.values()).forEach(
                cornerPosition -> {
                    Position adjacentCardPosition = position.computeAdjacentPosition(cornerPosition);
                    if (this.playedCards.containsKey((adjacentCardPosition))) {
                        CornerPosition oppositeCornerPosition = cornerPosition.getOppositeCornerPosition();
                        PlayableSide oppositeCard = playedCards.get(adjacentCardPosition).getPlayedSide().get();
                        HashMap<CornerPosition, Corner> enabledOppositeCorners = oppositeCard.getCorners();
                        Corner oppositeCorner = enabledOppositeCorners.get(oppositeCornerPosition);
                        updateResourcesAndObjectsMaps(oppositeCorner, -1);
                    }
                }
        );
    }

    /*
    * Helper method called by by PlayerBoard.updateResourcesAndObjects() to
    * update the stored data structures of player's resources and obbjects
    * */
    private void updateResourcesAndObjectsMaps(Corner corner, int update){
        Optional content = corner.getContent();
        if(content.isPresent()){
          if(ResourceType.has(content.get())){
              ResourceType resource = (ResourceType) content.get();
              this.resources.computeIfPresent(resource, (k, val) -> val + update);
          }else if(ObjectType.has(content.get())) {
            ObjectType object = (ObjectType) content.get();
            this.objects.computeIfPresent(object, (k, val) -> val + update);
          }
        }
    }

    private void updateAvailableSpots(PlayableSide playedSide, Position position){
        this.availableSpots.remove(position);
        HashMap<CornerPosition,Corner> enabledCorners = playedSide.getCorners();

        for (CornerPosition cornerPosition : CornerPosition.values()) {
            Position adjacentCardPosition = position.computeAdjacentPosition(cornerPosition);
            //if the spot is nor forbidden nor occupied, we add it to the available ones
            //if the corner is disabled, aka it's not in the HashMap, we add it to the forbidden ones.
            if(enabledCorners.containsKey(cornerPosition)){
                if(!forbiddenSpots.contains(adjacentCardPosition)){
                    if(!playedCards.containsKey(adjacentCardPosition)){
                        availableSpots.add(adjacentCardPosition);
                    }
                }
            }else{
                forbiddenSpots.add(adjacentCardPosition);
            }
        }
    }

    public HashMap<ObjectType, Integer> getObjects() {
        return this.objects;
    }

    public HashMap<ResourceType, Integer> getResources() {
        return this.resources;
    }

    public Map<Position, PlayableCard> getPlayedCards() {
      return playedCards;
    }

}




