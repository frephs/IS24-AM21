package polimi.ingsw.am21.codex.client.localModel;

import java.util.HashMap;
import java.util.Map;

public class LocalMenu {

  private final Map<String, GameEntry> games = new HashMap<>();

  LocalMenu() {}

  public Map<String, GameEntry> getGames() {
    return games;
  }
}
