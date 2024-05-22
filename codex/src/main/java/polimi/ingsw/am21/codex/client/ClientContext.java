package polimi.ingsw.am21.codex.client;

import java.util.HashMap;
import java.util.Map;

public enum ClientContext {
  //TODO move commandHandlers here??

  //TODO decide if this class is still needed
  LOBBY(generateLobbyCommands()),
  GAME(
    new HashMap<String, String>(
      Map.of(
        "get-game-status",
        "Get the current game status",
        "place-card <cardId> <position>",
        "Place the specified card in the specified position",
        "draw-card <drawing-position> <deck-type>",
        "Draw a card from the specified position and deck type"
      )
    )
  );

  public final Map<String, String> availableCommands;

  ClientContext(Map<String, String> availableCommands) {
    this.availableCommands = availableCommands;
  }

  private static Map<String, String> generateLobbyCommands() {
    Map<String, String> res = new HashMap<>();
    res.put("list-games", "Display the available game lobbies");
    res.put("leave-game", "Leave the game");

    res.put("join-game <game-id>", "Join the specified game lobby");
    res.put(
      "create-game <game-id> <number-of-players>",
      "Create a new game lobby with the selected number of players"
    );
    res.put("set-token <color>", "Choose your player token color");
    res.put("set-nickname <nickname>", "Choose your player nickname");
    res.put(
      "get-objective-cards",
      "Get the available secret objective card's id"
    );
    res.put("set-objective-card <first>", "Choose your secret objective card");

    res.put(
      "get-starter-card-sides",
      "Get the id of the startCard to show its' sides"
    );
    res.put(
      "select-starter-side <front|back>",
      "Choose the starter card to place on your playerboard"
    );
    res.put("get-game-status", "Get the current game status");
    return res;
  }
}
