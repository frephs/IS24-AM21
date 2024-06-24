package polimi.ingsw.am21.codex.client.localModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents the local menu. It is used to store the information about the games available in the game menu.
 * It is stored in the LocalModelContainer class.
 * It includes the game entries, which are the games available in the game menu.
 * @see LocalModelContainer
 * @see GameEntry
 * */
public class LocalMenu {

  private final Map<String, GameEntry> games = new HashMap<>();

  LocalMenu() {}

  public Map<String, GameEntry> getGames() {
    return games;
  }
}
