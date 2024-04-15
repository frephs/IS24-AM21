package polimi.ingsw.am21.codex.model.Cards;

import java.util.*;

public class CardBuilder {

  private final int id;
  private final CardType type;

  // Resource | Gold | Objective
  private Optional<Integer> points;

  // Objective
  private Optional<ObjectiveType> objectiveType;
  private Optional<Map<AdjacentPosition, ResourceType>> objectiveGeometry;
  private Optional<HashMap<ResourceType, Integer>> objectiveResources;
  private Optional<HashMap<ObjectType, Integer>> objectiveObjects;

  // Resource | Starter | Gold
  private Optional<List<ResourceType>> backPermanentResources;
  private Optional<HashMap<CornerPosition, CornerContentType>> frontCorners;
  private Optional<HashMap<CornerPosition, CornerContentType>> backCorners;

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

    if (objectiveGeometry != null) this.objectiveGeometry = Optional.of(
      Map.copyOf(objectiveGeometry)
    );
    else this.objectiveGeometry = Optional.empty();

    return this;
  }

  public CardBuilder setObjectiveResources(
    HashMap<ResourceType, Integer> objectiveResources
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

    this.objectiveResources = Optional.of(new HashMap<>(objectiveResources));
    return this;
  }

  public CardBuilder setObjectiveObjects(
    HashMap<ObjectType, Integer> objectiveObjects
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

    this.objectiveObjects = Optional.of(new HashMap<>(objectiveObjects));
    return this;
  }

  public CardBuilder setBackPermanentResources(
    List<ResourceType> backPermanentResources
  ) throws WrongCardTypeException {
    checkType(CardType.RESOURCE, CardType.STARTER, CardType.GOLD);

    if (backPermanentResources != null) this.backPermanentResources =
      Optional.of(List.copyOf(backPermanentResources));
    else this.backPermanentResources = Optional.empty();

    return this;
  }

  public CardBuilder setPlacementCondition(
    List<ResourceType> placementCondition
  ) throws WrongCardTypeException {
    checkType(CardType.GOLD);

    if (placementCondition != null) this.placementCondition = Optional.of(
      List.copyOf(placementCondition)
    );
    else this.placementCondition = Optional.empty();
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
    HashMap<CornerPosition, CornerContentType> cornerMap
  ) throws WrongCardTypeException {
    checkType(CardType.STARTER, CardType.RESOURCE, CardType.GOLD);

    if (side == CardSideType.FRONT) frontCorners = Optional.of(
      new HashMap<>(cornerMap)
    );
    else if (side == CardSideType.BACK) backCorners = Optional.of(
      new HashMap<>(cornerMap)
    );
    else throw new NullPointerException();

    return this;
  }

  public Card build() throws MissingParametersException {
    switch (this.type) {
      case OBJECTIVE -> {
        Integer points =
          this.points.orElseThrow(
              () -> new MissingParametersException("points")
            );
        Objective objective =
          this.objectiveType.map(type -> {
              if (type == ObjectiveType.GEOMETRIC) {
                return this.objectiveGeometry.map(
                    GeometricObjective::new
                  ).orElseThrow(
                    () -> new MissingParametersException("objectiveGeometry")
                  );
              } else {
                HashMap<ResourceType, Integer> res =
                  this.objectiveResources.orElseThrow(
                      () -> new MissingParametersException("objectiveResources")
                    );
                HashMap<ObjectType, Integer> obj =
                  this.objectiveObjects.orElseThrow(
                      () -> new MissingParametersException("objectiveObjects")
                    );
                return new CountingObjective(res, obj);
              }
            }).orElseThrow(
              () -> new MissingParametersException("objectiveType")
            );

        return new ObjectiveCard(this.id, points, objective);
      }
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
        Integer points =
          this.points.orElseThrow(
              () -> new MissingParametersException("points")
            );

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

  /**
   * Creates a playable card, handling corners and back side creation
   * @param permanentResources The permanent resources on the back side
   * @param frontSide The front side of the card
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
