package polimi.ingsw.am21.codex.controller.listeners;

import java.util.*;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.ObjectType;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.Cards.ResourceType;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.Player;
import polimi.ingsw.am21.codex.model.Player.TokenColor;

public interface GameEventListener {
  void gameCreated(String gameId, int players);

  void gameDeleted(String gameId);

  void playerJoinedLobby(String gameId, UUID socketID);

  void playerLeftLobby(String gameId, UUID socketID);

  void playerSetToken(String gameId, UUID socketID, TokenColor token);

  void playerSetNickname(String gameId, UUID socketID, String nickname);

  void playerChoseObjectiveCard(String gameId, UUID socketID, Boolean isFirst);

  void playerJoinedGame(
    String gameId,
    UUID socketID,
    String nickname,
    TokenColor color,
    List<Integer> handIDs
  );

  void gameStarted(String gameId, List<String> players);

  void changeTurn(
    String gameId,
    Integer nextPlayer,
    DrawingCardSource source,
    DrawingDeckType deck,
    Integer cardId,
    Boolean isLastRound
  );

  void changeTurn(String gameId, Integer nextPlayer, Boolean isLastRound);

  /* current player placed a card */
  void cardPlaced(
    String gameId,
    Integer playerHandCardNumber,
    Integer cardId,
    CardSideType side,
    Position position,
    int newPlayerScore,
    Map<ResourceType, Integer> updatedResources,
    Map<ObjectType, Integer> updatedObjects
  );
}
