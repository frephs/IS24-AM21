package polimi.ingsw.am21.codex.model.Cards.Objectives;

import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;

public abstract class Objective {

  public abstract BiFunction<PlayerBoard, Integer, Integer> getEvaluator();
}
