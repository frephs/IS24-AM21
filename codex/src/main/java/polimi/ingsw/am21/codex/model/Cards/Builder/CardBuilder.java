package polimi.ingsw.am21.codex.model.Cards.Builder;

import polimi.ingsw.am21.codex.model.Cards.*;
import polimi.ingsw.am21.codex.model.Cards.Objectives.*;
import polimi.ingsw.am21.codex.model.Cards.Playable.*;

import java.util.*;

public class CardBuilder {

  private final int id;
  private final CardType type;

  // Resource | Gold | Objective
  private Optional<Integer> points;

  // Objective
  private Optional<ObjectiveType> objectiveType;
  private Optional<Map<AdjacentPosition, ResourceType>> objectiveGeometry;
  private Optional<Map<ResourceType, Integer>> objectiveResources;
  private Optional<Map<ObjectType, Integer>> objectiveObjects;

  // Resource | Starter | Gold
  private Optional<List<ResourceType>> backPermanentResources;
  private Optional<
    Map<CornerPosition, Optional<CornerContentType>>
  > frontCorners;
  private Optional<
    Map<CornerPosition, Optional<CornerContentType>>
  > backCorners;

  // Gold
  private Optional<List<ResourceType>> placementCondition;
  private Optional<PointConditionType> pointCondition;
  private Optional<ObjectType> pointConditionObject;

  public CardBuilder(int id, CardType type) {
    this.id = id;
    if (type == null) throw new NullPointerException();
    this.type = type;

    this.points = Optional.empty();
    this.objectiveType = Optional.empty();
    this.objectiveGeometry = Optional.empty();
    this.objectiveResources = Optional.empty();
    this.objectiveObjects = Optional.empty();
    this.backPermanentResources = Optional.empty();
    this.placementCondition = Optional.empty();
    this.pointCondition = Optional.empty();
    this.pointConditionObject = Optional.empty();
    this.frontCorners = Optional.empty();
    this.backCorners = Optional.empty();
  }

  /**
   * Checks whether the current card type matches any oof the provided types
   */
  private void checkType(CardType... expected) throws WrongCardTypeException {
    List<CardType> list = Arrays.stream(expected).toList();
    if (!list.contains(this.type)) throw new WrongCardTypeException(
      String.join("|", list.stream().map(CardType::toString).toList()),
      this.type.toString()
    );
  }

  public CardBuilder setPoints(int points) throws WrongCardTypeException {
    checkType(CardType.RESOURCE, CardType.GOLD, CardType.OBJECTIVE);
    this.points = Optional.of(points);
    return this;
  }

  public CardBuilder setObjectiveType(ObjectiveType objectiveType)
    throws WrongCardTypeException {
    checkType(CardType.OBJECTIVE);
    this.objectiveType = Optional.ofNullable(objectiveType);
    return this;
  }

  public CardBuilder setObjectiveGeometry(
    Map<AdjacentPosition, ResourceType> objectiveGeometry
  ) throws WrongCardTypeException, ConflictingParameterException {
    checkType(CardType.OBJECTIVE);
    if (
      this.objectiveType.map(t -> t != ObjectiveType.GEOMETRIC).orElse(true)
    ) throw new ConflictingParameterException(
      "objectiveType",
      ObjectiveType.GEOMETRIC.toString(),
      this.objectiveType.map(Enum::toString).orElse("empty")
    );

    this.objectiveGeometry = Optional.ofNullable(
      objectiveGeometry == null ? null : new HashMap<>(objectiveGeometry)
    );

    return this;
  }

  public CardBuilder setObjectiveResources(
    Map<ResourceType, Integer> objectiveResources
  ) throws WrongCardTypeException, ConflictingParameterException {
    checkType(CardType.OBJECTIVE);
    if (
      this.objectiveType.map(t -> t != ObjectiveType.COUNTING).orElse(true)
    ) throw new ConflictingParameterException(
      "objectiveType",
      ObjectiveType.COUNTING.toString(),
      this.objectiveType.map(Enum::toString).orElse("empty")
    );
    if (
      this.objectiveObjects.isPresent()
    ) throw new ConflictingParameterException(
      "objectiveObjects",
      "empty",
      "set"
    );

    this.objectiveResources = Optional.ofNullable(
      objectiveResources == null ? null : new HashMap<>(objectiveResources)
    );
    return this;
  }

  public CardBuilder setObjectiveObjects(
    Map<ObjectType, Integer> objectiveObjects
  ) throws WrongCardTypeException, ConflictingParameterException {
    checkType(CardType.OBJECTIVE);
    if (
      this.objectiveType.map(t -> t != ObjectiveType.COUNTING).orElse(true)
    ) throw new ConflictingParameterException(
      "objectiveType",
      ObjectiveType.COUNTING.toString(),
      this.objectiveType.map(Enum::toString).orElse("empty")
    );
    if (
      this.objectiveResources.isPresent()
    ) throw new ConflictingParameterException(
      "objectiveResources",
      "empty",
      "set"
    );

    this.objectiveObjects = Optional.ofNullable(
      objectiveObjects == null ? null : new HashMap<>(objectiveObjects)
    );
    return this;
  }

  public CardBuilder setBackPermanentResources(
    List<ResourceType> backPermanentResources
  ) throws WrongCardTypeException {
    checkType(CardType.RESOURCE, CardType.STARTER, CardType.GOLD);

    this.backPermanentResources = Optional.ofNullable(
      backPermanentResources == null
        ? null
        : List.copyOf(backPermanentResources)
    );

    return this;
  }

  public CardBuilder setPlacementCondition(
    List<ResourceType> placementCondition
  ) throws WrongCardTypeException {
    checkType(CardType.GOLD);

    this.placementCondition = Optional.ofNullable(
      placementCondition == null ? null : List.copyOf(placementCondition)
    );

    return this;
  }

  public CardBuilder setPointCondition(PointConditionType pointConditionType)
    throws WrongCardTypeException {
    checkType(CardType.GOLD);

    this.pointCondition = Optional.ofNullable(pointConditionType);
    return this;
  }

  public CardBuilder setPointConditionObject(ObjectType pointConditionObject)
    throws WrongCardTypeException, ConflictingParameterException {
    checkType(CardType.GOLD);
    if (
      this.pointCondition.map(t -> t != PointConditionType.OBJECTS).orElse(true)
    ) throw new ConflictingParameterException(
      "pointCondition",
      PointConditionType.OBJECTS.toString(),
      this.pointCondition.map(Enum::toString).orElse("empty")
    );

    this.pointConditionObject = Optional.ofNullable(pointConditionObject);
    return this;
  }

  public CardBuilder setCorners(
    CardSideType side,
    Map<CornerPosition, Optional<CornerContentType>> cornerMap
  ) throws WrongCardTypeException {
    checkType(CardType.STARTER, CardType.RESOURCE, CardType.GOLD);

    if (side == CardSideType.FRONT) frontCorners = Optional.ofNullable(
      cornerMap == null ? null : new HashMap<>(cornerMap)
    );
    else if (side == CardSideType.BACK) backCorners = Optional.ofNullable(
      cornerMap == null ? null : new HashMap<>(cornerMap)
    );
    else throw new NullPointerException();

    return this;
  }

  public ObjectiveCard buildObjectiveCard() throws MissingParametersException {
    Integer points =
      this.points.orElseThrow(() -> new MissingParametersException("points"));
    Objective objective =
      this.objectiveType.map(type -> {
          if (type == ObjectiveType.GEOMETRIC) {
            return this.objectiveGeometry.map(
                GeometricObjective::new
              ).orElseThrow(
                () -> new MissingParametersException("objectiveGeometry")
              );
          } else {
            Map<ResourceType, Integer> res =
              this.objectiveResources.orElse(new HashMap<>());
            Map<ObjectType, Integer> obj =
              this.objectiveObjects.orElse(new HashMap<>());
            if (
              res.isEmpty() && obj.isEmpty()
            ) throw new MissingParametersException(
              "objectiveResources or " + "objectiveObjects"
            );

            return new CountingObjective(res, obj);
          }
        }).orElseThrow(() -> new MissingParametersException("objectiveType"));

    return new ObjectiveCard(this.id, points, objective);
  }

  public PlayableCard buildPlayableCard() throws MissingParametersException {
    switch (this.type) {
      case STARTER -> {
        List<ResourceType> permanentResources =
          this.backPermanentResources.orElseThrow(
              () -> new MissingParametersException("backPermanentResources")
            );

        return getPlayableCard(
          permanentResources,
          new StarterCardFrontSide(),
          CardType.STARTER
        );
      }
      case RESOURCE -> {
        List<ResourceType> permanentResources =
          this.backPermanentResources.orElseThrow(
              () -> new MissingParametersException("backPermanentResources")
            );
        Integer points = this.points.orElse(0);

        return getPlayableCard(
          permanentResources,
          new ResourceCardFrontSide(points),
          CardType.RESOURCE
        );
      }
      case GOLD -> {
        List<ResourceType> permanentResources =
          this.backPermanentResources.orElseThrow(
              () -> new MissingParametersException("backPermanentResources")
            );
        Integer points =
          this.points.orElseThrow(
              () -> new MissingParametersException("points")
            );
        List<ResourceType> placementCondition =
          this.placementCondition.orElseThrow(
              () -> new MissingParametersException("placementCondition")
            );

        return getPlayableCard(
          permanentResources,
          new GoldCardFrontSide(
            points,
            placementCondition,
            this.pointCondition.orElse(null),
            this.pointConditionObject.orElse(null)
          ),
          CardType.GOLD
        );
      }
      default -> throw new ConflictingParameterException(
        "type",
        String.join(
          "|",
          Arrays.stream(CardType.values()).map(CardType::toString).toList()
        ),
        this.type.toString()
      );
    }
  }

  public Card build() throws MissingParametersException {
    if (this.type == CardType.OBJECTIVE) return this.buildObjectiveCard();
    return this.buildPlayableCard();
  }

  /**
   * Creates a playable card, handling corners and back side creation
   *
   * @param permanentResources The permanent resources on the back side
   * @param frontSide          The front side of the card
   * @return The resulting PlayableCard
   */
  private PlayableCard getPlayableCard(
    List<ResourceType> permanentResources,
    PlayableFrontSide frontSide,
    CardType cardType
  ) {
    this.frontCorners.orElse(new HashMap<>()).forEach(frontSide::setCorner);

    PlayableBackSide backSide = new PlayableBackSide(permanentResources);
    this.backCorners.orElse(new HashMap<>()).forEach(backSide::setCorner);

    if (cardType == CardType.STARTER) {
      return new PlayableCard(this.id, frontSide, backSide);
    } else {
      return new PlayableCard(
        this.id,
        frontSide,
        backSide,
        permanentResources.getFirst()
      );
    }
  }
}
