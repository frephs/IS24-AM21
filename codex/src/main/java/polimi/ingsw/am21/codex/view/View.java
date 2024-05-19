package polimi.ingsw.am21.codex.view;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import polimi.ingsw.am21.codex.client.localModel.LocalGameBoard;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Colorable;

public interface View {
  void postNotification(NotificationType notificationType, String message);

  void postNotification(Notification notification);

  void postNotification(
    NotificationType notificationType,
    String[] messages,
    Colorable colorable,
    int colorableIndex
  );

  // lobby
  void drawAvailableGames(
    Set<String> gameIds,
    Map<String, Integer> currentPlayers,
    Map<String, Integer> maxPlayers
  );

  void drawAvailableTokenColors(Set<TokenColor> tokenColors);

  void drawLobby(
    Map<UUID, TokenColor> availableTokens,
    Map<UUID, String> playerNicknames
  );

  // game

  void drawLeaderBoard(List<LocalPlayer> players);

  void drawPlayerBoards(List<LocalPlayer> players);

  void drawPlayerBoard(LocalPlayer player);

  void drawCardDraw(DrawingDeckType deck, Card card);
  void drawCardDraw(DrawingDeckType deck);

  void drawLeaderBoard();

  void drawCardPlacement(Card card, CardSideType side, Position position);

  void drawGame(List<LocalPlayer> players);

  void drawCard(Card card);
}
