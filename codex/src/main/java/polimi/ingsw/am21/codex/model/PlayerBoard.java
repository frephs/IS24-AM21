package polimi.ingsw.am21.codex.model;

import polimi.ingsw.am21.codex.model.Cards.*;

import java.util.*;

public class PlayerBoard {

    private final int MAX_CARDS = 3;
    private List<PlayableCard> hand = new ArrayList<PlayableCard>(MAX_CARDS);
    private ObjectiveCard objectiveCard;

    Map<Position, PlayableCard> playedCards = new HashMap<>();

   // Hashmaps to keep track of resources
    private HashMap<ResourceType, Integer> resources = new HashMap<>(ResourceType.values().lenght);
    private HashMap<ObjectType, Integer> objects = new HashMap<>(ObjectType.values().lenght);

    // List of all available spots in which a card can be placed
    Set<Position> availableSpots = new HashSet<>();
    Set<Position> forbiddenSpots = new HashSet<>();

    HashMap<Position, PlayableCard>  placedCards = new HashMap<Position, PlayableCard>();

    /**
     * @param hand the player's cards drawn from the GameBoard (2 resources and 1 GoldCard)
     * @param starterCard drawn from the PlayerBoard
     * @param objectiveCard chosen by the client controller (physical player)
     */

    public PlayerBoard(List<PlayableCard> hand, PlayableCard starterCard, ObjectiveCard objectiveCard) {
        this.hand = hand;
        this.playedCards.put(new Position(), starterCard);
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

    public HashMap<ObjectType, Integer> getObjects() {
        return this.objects;
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
        playedCard.setPlayedSide(playedSideType);
        PlayableSide playedSide = playedCard.getPlayedSide().get();

        this.playedCards.put(position, playedCard);

        updateAvailableSpots(playedSide, position);
        updateResourcesAndObjects(playedSide, position);

    }

      private void updateResourcesAndObjects(PlayableSide playedSide, Position position) {
        HashMap<CornerPosition, Corner> enabledCorners = playedSide.getCorners();

        //let's add the resources of the card just placed
        enabledCorners.forEach(
                (cornerPosition, corner) -> {
                    updateResourcesAndObjectsMaps(corner, +1);
                }
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

    private void updateResourcesAndObjectsMaps(Corner corner, int update){
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
}




