package polimi.ingsw.am21.codex.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ClientContext {
  LOBBY(
    new HashMap<String, String>(
      Map.of(
        "get-lobbies",
        "Get the available game lobbies",
        "join-game-lobby <lobbyId>",
        "Join the specified lobby",
        "create-game ",
        "Create a new came lobby ",
        "set-nickname <nickname>",
        "Choose your player nickname",
        "get-token-colors",
        "Get the available token colors",
        "set-token-color <color>",
        "Choose your player token color",
        "get-objective-cards",
        "Get the available secret objective card's id",
        "set-objective-card <first>",
        "Choose your secret objective card",
        "get-starter-card-sides",
        "Get the id of the startCard to show its' sides",
        "set-starter-card-sides <first>",
        "Choose the starter card to place on your playerboard ",
        "get-game-status",
        "Get the current game status"
      )
    )
  ),
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
}
