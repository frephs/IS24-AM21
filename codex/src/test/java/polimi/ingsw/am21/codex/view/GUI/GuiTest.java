package polimi.ingsw.am21.codex.view.GUI;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.client.localModel.GameEntry;
import polimi.ingsw.am21.codex.client.localModel.LocalPlayer;
import polimi.ingsw.am21.codex.model.Cards.Card;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardPair;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Player.TokenColor;
import polimi.ingsw.am21.codex.view.NotificationType;

class GuiTest {

  static Gui gui;

  @BeforeAll
  static void setUp() {
    gui = new Gui();
    Application.launch(gui.getClass());
  }

  @Test
  public void testNotifications() {
    gui.postNotification(NotificationType.CONFIRM, "Attento");
    gui.postNotification(NotificationType.WARNING, "Attento");
    gui.postNotification(NotificationType.ERROR, "Attento");
    gui.postNotification(NotificationType.UPDATE, "Attento");
    gui.postNotification(NotificationType.RESPONSE, "Attento");
  }

  @Test
  public void testException() {
    //    displayException(new RuntimeException("Test exception"));

  }

  @Test
  public void testMenu() {
    gui.drawAvailableGames(
      new ArrayList<>(
        Collections.nCopies(
          52,
          new GameEntry(UUID.randomUUID().toString().substring(0, 3), 2, 4)
        )
      )
    );
  }

  @Test
  public void testLobbyAndTokens() {
    LocalPlayer p1 = new LocalPlayer(UUID.randomUUID());
    LocalPlayer p2 = new LocalPlayer(UUID.randomUUID());
    LocalPlayer p3 = new LocalPlayer(UUID.randomUUID());
    LocalPlayer p4 = new LocalPlayer(UUID.randomUUID());

    p1.setNickname("Player 1");
    p2.setNickname("Player 2");
    p3.setNickname("Player 3");

    p1.setToken(TokenColor.RED);
    p2.setToken(TokenColor.BLUE);
    p4.setToken(TokenColor.YELLOW);

    gui.drawAvailableTokenColors(
      Arrays.stream(TokenColor.values()).collect(Collectors.toSet())
    );
    gui.drawLobby(
      Map.of(
        UUID.randomUUID(),
        p1,
        UUID.randomUUID(),
        p2,
        UUID.randomUUID(),
        p3,
        UUID.randomUUID(),
        p4
      )
    );
  }

  @Test
  public void testObjectiveCardChoice() {
    CardsLoader cards = new CardsLoader();

    Card cardFromId = cards.getCardFromId(96);
    gui.drawObjectiveCardChoice(
      new CardPair<>(cardFromId, cards.getCardFromId(101))
    );
  }

  @Test
  public void testStarterCardChoice() {
    CardsLoader cards = new CardsLoader();
    gui.drawStarterCardSides(cards.getCardFromId(32));
  }
}
