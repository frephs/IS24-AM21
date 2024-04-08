package polimi.ingsw.am21.codex.model.Cards;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CardBuilder {

  int id;
  CardType type;

  // Resource | Gold
  Optional<Integer> points;

  // Objective
  Optional<ObjectiveType> objectiveType;
  Optional<List<List<ResourceType>>> objectiveGeometry;
  Optional<HashMap<ResourceType, Integer>> objectiveResources;
  Optional<HashMap<ObjectType, Integer>> objectiveObjects;

  // Resource | Starter | Gold
  Optional<List<ResourceType>> backPermanentResources;
  Optional<HashMap<CornerPosition, CornerContentType>> frontCorners;
  Optional<HashMap<CornerPosition, CornerContentType>> backCorners;

  // Gold
  Optional<List<ResourceType>> placementCondition;
  Optional<PointConditionType> pointCondition;
  Optional<ObjectType> pointConditionObject;

  public CardBuilder(int id, CardType type) {
    this.id = id;
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

  private void checkType(CardType... expected) throws WrongCardTypeException {
    List<CardType> list = Arrays.stream(expected).toList();
    if (!list.contains(this.type)) throw new WrongCardTypeException(
      String.join("|", list.stream().map(CardType::toString).toList()),
      this.type.toString()
    );
  }

  public CardBuilder setPoints(int points) throws WrongCardTypeException {
    this.points = Optional.of(points);
    return this;
  }

  public CardBuilder setObjectiveType(ObjectiveType objectiveType)
    throws WrongCardTypeException {
    this.objectiveType = Optional.ofNullable(objectiveType);
    return this;
  }

  public CardBuilder setObjectiveGeometry(
    List<List<ResourceType>> objectiveGeometry
  ) throws WrongCardTypeException, MissingParametersException {
    checkType(CardType.OBJECTIVE);
    if (
      this.objectiveType.map(t -> t != ObjectiveType.GEOMETRIC).orElse(true)
    ) throw new ConflictingParameterException(
      "objectiveType",
      ObjectiveType.GEOMETRIC.toString(),
      this.objectiveType.map(Enum::toString).orElse("empty")
    );

    this.objectiveGeometry = Optional.ofNullable(
      List.copyOf(objectiveGeometry)
    );
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

    this.backPermanentResources = Optional.ofNullable(
      List.copyOf(backPermanentResources)
    );
    return this;
  }

  public CardBuilder setPlacementCondition(
    List<ResourceType> placementCondition
  ) throws WrongCardTypeException {
    checkType(CardType.GOLD);

    this.placementCondition = Optional.ofNullable(
      List.copyOf(placementCondition)
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
    throws WrongCardTypeException {
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

        return getPlayableCard(permanentResources, new StarterCardFrontSide());
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
          new ResourceCardFrontSide(points)
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
          )
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

  private Card getPlayableCard(
    List<ResourceType> permanentResources,
    PlayableFrontSide frontSide
  ) {
    this.frontCorners.orElse(new HashMap<>()).forEach(frontSide::setCorners);

    PlayableBackSide backSide = new PlayableBackSide(permanentResources);
    this.backCorners.orElse(new HashMap<>()).forEach(backSide::setCorners);

    return new PlayableCard(this.id, frontSide, backSide);
  }
}
