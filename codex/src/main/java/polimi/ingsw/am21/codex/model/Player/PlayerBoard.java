package polimi.ingsw.am21.codex.model.Player;

import java.util.*;
import java.util.stream.Collectors;
import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableSide;

public class PlayerBoard {

  //private final int MAX_CARDS = 3;
  private final List<PlayableCard> hand;
  private final ObjectiveCard objectiveCard;

  Map<Position, PlayableCard> playedCards = new HashMap<>();

  // Hashmaps to keep track of resources
  private final Map<ResourceType, Integer> resources = new HashMap<>(
    ResourceType.values().length
  );
  private final Map<ObjectType, Integer> objects = new HashMap<>(
    ObjectType.values().length
  );
  private final MapUpdater mapUpdater = new MapUpdater();

  // List of all available spots in which a card can be placed
  Set<Position> availableSpots = new HashSet<>();
  Set<Position> forbiddenSpots = new HashSet<>();

  /**
   * @param hand the player's cards drawn from the GameBoard (2 resources and 1 GoldCard)
   * @param starterCard drawn from the PlayerBoard
   * @param objectiveCard chosen by the client controller (physical player)
   */
  public PlayerBoard(
    List<PlayableCard> hand,
    PlayableCard starterCard,
    ObjectiveCard objectiveCard
  ) throws IllegalCardSideChoiceException, IllegalPlacingPositionException {
    this.hand = new LinkedList<>(hand);
    this.objectiveCard = objectiveCard;

    // initialize the maps with resources to 0
    Arrays.stream(ResourceType.values()).forEach(
      resourceType -> resources.put(resourceType, 0)
    );
    Arrays.stream(ObjectType.values()).forEach(
      objectType -> objects.put(objectType, 0)
    );

    // add the starterCard to the playerBoard
    this.hand.add(starterCard); // the starter card cannot skip the placeability checks
    availableSpots.add(new Position());
    placeCard(
      starterCard,
      starterCard.getPlayedSideType().orElseThrow(),
      new Position()
    );
  }

  /**
   * @return the player's secret objective
   */
  public ObjectiveCard getObjectiveCard() {
    return objectiveCard;
  }

  /**
   * @return the player's hand
   */
  public List<PlayableCard> getHand() {
    return this.hand;
  }

  /**
   * @return a list of sides that are both playable and placeable
   */
  public List<PlayableSide> getPlaceableCardSides() {
    return getHand()
      .stream()
      .flatMap(card -> card.getSides().stream())
      .filter(side -> side.getPlaceabilityChecker().apply(this))
      .collect(Collectors.toList());
  }

  /**
   * @param card which is added to the player's hand
   */
  void drawCard(PlayableCard card) {
    hand.add(card);
  }

  /**
   * Places a card on the playerBoard,
   * @param playedCard chosen from the player's hand, the card will be evaluated after placement
   * @param playedSideType of the card chosen to be placed on the PlayerBoard
   * @param position of the PlayerBoard in which the card will be placed by the PlayerBoard
   * @throws IllegalPlacingPositionException if the provided position is either not reachable, forbidden or occupied
   * @throws IllegalCardSideChoiceException if the side chosen is not placeable because of card placing conditions.
   */
  public void placeCard(
    PlayableCard playedCard,
    CardSideType playedSideType,
    Position position
  ) throws IllegalPlacingPositionException, IllegalCardSideChoiceException {
    playedCard.setPlayedSideType(playedSideType);
    if (playedCards.containsKey(position)) {
      throw new IllegalPlacingPositionException(
        "You tried placing a card in a spot which is already occupied"
      );
    }

    if (forbiddenSpots.contains(position)) {
      throw new IllegalPlacingPositionException(
        "You tried placing a card in a spot which is forbidden"
      );
    }

    if (!availableSpots.contains(position)) {
      throw new IllegalPlacingPositionException(
        "You tried placing a card in a spot which is unreachable"
      );
    }

    if (
      !getPlaceableCardSides()
        .contains(
          playedCard
            .getPlayedSide()
            .orElseThrow(
              () -> new NoSuchElementException("Side is not present")
            )
        )
    ) {
      throw new IllegalCardSideChoiceException();
    }

    this.hand.remove(playedCard);

    PlayableSide playedSide = playedCard
      .getPlayedSide()
      .orElseThrow(() -> new NoSuchElementException("Side not present"));

    this.playedCards.put(position, playedCard);

    updateAvailableSpots(playedSide, position);
    updateResourcesAndObjects(playedCard, position);
  }

  /**
   * Helper method called by PlayerBoard.placeCard() to update the player's available resources and objects
   */
  private void updateResourcesAndObjects(
    PlayableCard playedCard,
    Position position
  ) {
    PlayableSide playedSide = playedCard
      .getPlayedSide()
      .orElseThrow(() -> new NoSuchElementException("Side not present"));
    Map<CornerPosition, Corner<CornerContentType>> enabledCorners =
      playedSide.getCorners();

    // add the resources of the card just placed
    enabledCorners.forEach(
      (cornerPosition, corner) -> updateResourcesAndObjectsMaps(corner, +1)
    );

    // if the played side is CardSideType.back, add the backPermanentResources too
    if (playedCard.getPlayedSideType().orElseThrow() == CardSideType.BACK) {
      playedCard
        .getBackPermanentResources()
        .forEach(
          resource ->
            updateResourcesAndObjectsMaps(
              new Corner<ResourceType>(resource),
              +1
            )
        );
    }

    // remove the resources of the cards that are covered.
    Arrays.stream(CornerPosition.values()).forEach(cornerPosition -> {
      Position adjacentCardPosition = position.computeAdjacentPosition(
        cornerPosition
      );
      if (this.playedCards.containsKey((adjacentCardPosition))) {
        CornerPosition oppositeCornerPosition =
          cornerPosition.getOppositeCornerPosition();
        PlayableSide oppositeCard = playedCards
          .get(adjacentCardPosition)
          .getPlayedSide()
          .orElseThrow(() -> new NoSuchElementException("Side not present"));
        Map<CornerPosition, Corner<CornerContentType>> enabledOppositeCorners =
          oppositeCard.getCorners();
        Corner<CornerContentType> oppositeCorner = enabledOppositeCorners.get(
          oppositeCornerPosition
        );
        oppositeCorner.cover();
        playedCard.setCoveredCorners(playedCard.getCoveredCorners() + 1);
        updateResourcesAndObjectsMaps(oppositeCorner, -1);
      }
    });
  }

  /**
   * Helper method called by PlayerBoard.updateResourcesAndObjects() to
   * update the stored data structures of player's resources and objects
   * @param corner the corner whose content will be added to the playerBoard resources or objects
   * @param delta the count difference that will be applied to the resource or object map (usually either 1 or -1)
   */
  private <T extends CornerContentType> void updateResourcesAndObjectsMaps(
    Corner<T> corner,
    int delta
  ) {
    corner
      .getContent()
      .ifPresent(content -> content.acceptVisitor(mapUpdater, delta));
  }

  // Don't delete, it's exposed for testing purposes
  private void updateMap(ObjectType object, int delta) {
    mapUpdater.visit(object, delta);
  }

  // Don't delete, it's exposed for testing purposes
  private void updateMap(ResourceType resource, int delta) {
    mapUpdater.visit(resource, delta);
  }

  private class MapUpdater implements CornerContentVisitor {

    MapUpdater() {}

    @Override
    public void visit(ObjectType object, int delta) {
      objects.computeIfPresent(object, (key, value) -> value + delta);
    }

    @Override
    public void visit(ResourceType resource, int delta) {
      resources.computeIfPresent(resource, (key, value) -> value + delta);
    }
  }

  private void updateAvailableSpots(
    PlayableSide playedSide,
    Position position
  ) {
    this.availableSpots.remove(position);
    Map<CornerPosition, Corner<CornerContentType>> enabledCorners =
      playedSide.getCorners();

    for (CornerPosition cornerPosition : CornerPosition.values()) {
      Position adjacentCardPosition = position.computeAdjacentPosition(
        cornerPosition
      );
      //if the spot is nor forbidden nor occupied, we add it to the available ones
      //if the corner is disabled, aka it's not in the HashMap, we add it to the forbidden ones.
      if (enabledCorners.containsKey(cornerPosition)) {
        if (!forbiddenSpots.contains(adjacentCardPosition)) {
          if (!playedCards.containsKey(adjacentCardPosition)) {
            availableSpots.add(adjacentCardPosition);
          }
        }
      } else if (!playedCards.containsKey(adjacentCardPosition)) {
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

  public Set<Position> getAvailableSpots() {
    return availableSpots;
  }

  public Set<Position> getForbiddenSpots() {
    return forbiddenSpots;
  }
}
