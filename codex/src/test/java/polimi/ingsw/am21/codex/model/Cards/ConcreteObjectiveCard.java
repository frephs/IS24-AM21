package polimi.ingsw.am21.codex.model.Cards;

import polimi.ingsw.am21.codex.model.Cards.Objectives.ObjectiveCard;

public class ConcreteObjectiveCard extends ObjectiveCard {
  public ConcreteObjectiveCard() {
    super(12, 2, new ConcreteObjective());
  }
}
