package polimi.ingsw.am21.codex.view.TUI.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Playable.CardSideType;
import polimi.ingsw.am21.codex.model.Cards.Playable.PlayableCard;
import polimi.ingsw.am21.codex.model.Cards.Position;
import polimi.ingsw.am21.codex.view.TUI.utils.commons.Color;

class CliPlayerBoardTest {

  CardsLoader cardsLoader = new CardsLoader();

  @Test
  void testDrawPlayerBoard() {
    new Cli.Options(true);

    List<Integer> cards = List.of(1, 37, 46, 1, 37, 46, 1, 37, 46);
    List<Position> positions = List.of(
      new Position(0, 0),
      new Position(-1, 0),
      new Position(0, -1),
      new Position(0, 2),
      new Position(1, 1),
      new Position(2, 1),
      new Position(3, 2),
      new Position(4, 1),
      new Position(-2, 1)
    );

    List<Pair<Position, Pair<PlayableCard, CardSideType>>> playedCards =
      new ArrayList<>();
    for (int i = 0; i < positions.size(); i++) {
      playedCards.add(
        new Pair<>(
          positions.get(i),
          new Pair<>(
            (PlayableCard) cardsLoader.getCardFromId(cards.get(i)),
            CardSideType.FRONT
          )
        )
      );
    }

    CliPlayerBoard.drawPlayerBoard(
      playedCards,
      Set.of(new Position(1, 1)),
      Set.of(new Position(-1, -1)),
      0,
      0
    );
  }

  @Test
  void testAddBoxCharacters() {
    CliPlayerBoard.BOX_CHARACTERS.forEach(
      a ->
        CliPlayerBoard.BOX_CHARACTERS.forEach(
          b ->
            assertEquals(
              CliPlayerBoard.addBoxCharacters(a, b),
              CliPlayerBoard.addBoxCharacters(b, a)
            )
        )
    );
  }
}
