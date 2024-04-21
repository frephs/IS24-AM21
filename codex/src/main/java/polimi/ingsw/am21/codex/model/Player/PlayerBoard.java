package polimi.ingsw.am21.codex.model.Player;

import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableSide;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerBoard {

    //private final int MAX_CARDS = 3;
    private final List<PlayableCard> hand;
    private final ObjectiveCard objectiveCard;

    Map<Position, PlayableCard> playedCards = new HashMap<>();

   // Hashmaps to keep track of resources
    private final Map<ResourceType, Integer> resources = new HashMap<>(ResourceType.values().length);
    private final Map<ObjectType, Integer> objects = new HashMap<>(ObjectType.values().length);
    private final MapUpdater mapUpdater = new MapUpdater();

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
        this.objectiveCard = objectiveCard;

        // let's initialize the maps with resources to 0
        Arrays.stream(ResourceType.values()).forEach(
            (resourceType) -> resources.put(resourceType, 0)
        );
        Arrays.stream(ObjectType.values()).forEach(
            (objectType) -> objects.put(objectType, 0)
        );

        // let's add the starterCard to the playerBoard
        this.hand.add(starterCard);
        placeCard(3, starterCard.getPlayedSideType().orElseThrow(), new Position());

    }

    /**
     * @return the player's secret objective
     * */
    public ObjectiveCard getObjectiveCard() {
        return objectiveCard;
    }

    /**
     * @return the player's hand
     * */
    public List<PlayableCard> getHand(){
        return this.hand;
    }

    /**
     * @return a list of sides that are both playable and placeable
     * */
    public List<PlayableSide> getPlaceableCardSides(){
      return getHand().stream().flatMap(
        card -> card.getSides().stream()
      ).filter(
        side -> side.getPlaceabilityChecker().apply(this)
      ).collect(Collectors.toList());
    }

    /**
     * @param card which is added to the player's hand
     * */
    void drawCard(PlayableCard card){
        hand.add(card);
    }

    /**
     * Places a card on the playerBoard,
     * @param playedCardIndex of the card to be played chosen from the player's hand, the card will be evaluated after placement
     * @param playedSideType of the card chosen to be placed on the PlayerBoard
     * @param position of the PlayerBoard in which the card will be placed by the PlayerBoard
     * @throws IllegalPlacingPositionException if the position is either not reachable or forbidden
     * @throws IndexOutOfBoundsException if the card index exceeds the hand size
     * @throws IllegalCardSideChoiceException if the side chosen is not placeable because of card placing conditions.
     */
    void placeCard(int playedCardIndex, CardSideType playedSideType, Position position) throws
      IllegalPlacingPositionException, IndexOutOfBoundsException, IllegalCardSideChoiceException {

        if(! availableSpots.contains(position)){
          throw new IllegalPlacingPositionException();
        }

        PlayableCard playedCard;

        try{
          playedCard= hand.get(playedCardIndex);
        }catch (IndexOutOfBoundsException e){
          throw new IndexOutOfBoundsException("You tried to place a played card which either doesn't exist or is not in your hand");
        }

        if(! getPlaceableCardSides().contains(playedCard.getSide(playedSideType))){
          throw new IllegalCardSideChoiceException();
        }

        this.hand.remove(playedCard);
        playedCard.setPlayedSideType(playedSideType);
        PlayableSide playedSide = playedCard.getPlayedSide().orElseThrow();

        this.playedCards.put(position, playedCard);

        updateAvailableSpots(playedSide, position);
        updateResourcesAndObjects(playedSide, position);

    }

      /**
       * Helper method called by PlayerBoard.placeCard() to update the player's available resources and objects
       * */
      private void updateResourcesAndObjects(PlayableSide playedSide, Position position) {
        Map<CornerPosition, Corner> enabledCorners = playedSide.getCorners();

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
                        PlayableSide oppositeCard = playedCards.get(adjacentCardPosition).getPlayedSide().orElseThrow();
                        Map<CornerPosition, Corner> enabledOppositeCorners = oppositeCard.getCorners();
                        Corner oppositeCorner = enabledOppositeCorners.get(oppositeCornerPosition);
                        oppositeCorner.cover();
                        updateResourcesAndObjectsMaps(oppositeCorner, -1);
                    }
                }
        );
    }


  /* Old Implementation
  private <T extends CornerContentType> void updateResourcesAndObjectsMaps(Corner<T> corner, int update) {
      corner.getContent().ifPresent(
         content -> {
           if (ResourceType.has(content)) {
             this.objects.computeIfPresent((ResourceType) content, (key, val) -> val + update);
           } else if (ObjectType.has(content)) {
             this.objects.computeIfPresent((ObjectType) content, (key, val) -> val + update);
           }
         }
         );
    }*/

  /**
   * Helper method called by PlayerBoard.updateResourcesAndObjects() to
   * update the stored data structures of player's resources and objects
   * */
  private <T extends CornerContentType> void updateResourcesAndObjectsMaps(Corner<T> corner, int update){
    corner.getContent().ifPresent(
      content ->
        content.acceptVisitor(mapUpdater, update)
    );
  }

  private void updateMap(ObjectType object, int update){
    mapUpdater.visit(object,update);
  }

  private void updateMap(ResourceType resource, int update){
    mapUpdater.visit(resource,update);

  }

  private class MapUpdater implements CornerContentVisitor{
    MapUpdater(){}
    @Override
    public void visit(ObjectType object, int arg) {
      objects.computeIfPresent(object, (key, value) -> value + arg);
    }

    @Override
    public void visit(ResourceType resource, int arg) {
      resources.computeIfPresent(resource, (key, value) -> value + arg);
    }
  }

    private void updateAvailableSpots(PlayableSide playedSide, Position position){
        this.availableSpots.remove(position);
        Map<CornerPosition,Corner> enabledCorners = playedSide.getCorners();

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

    public Map<ObjectType, Integer> getObjects() {
        return this.objects;
    }

    public Map<ResourceType, Integer> getResources() {
        return this.resources;
    }

    public Map<Position, PlayableCard> getPlayedCards() {
      return playedCards;
    }

}




