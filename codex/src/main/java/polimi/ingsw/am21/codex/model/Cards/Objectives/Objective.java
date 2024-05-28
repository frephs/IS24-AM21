package polimi.ingsw.am21.codex.model.Cards.Objectives;

import java.util.function.BiFunction;
import polimi.ingsw.am21.codex.model.Player.PlayerBoard;
import polimi.ingsw.am21.codex.view.TUI.utils.CliCard;

public abstract class Objective implements CliCard {

  public abstract BiFunction<PlayerBoard, Integer, Integer> getEvaluator();
}
