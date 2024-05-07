package polimi.ingsw.am21.codex.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import polimi.ingsw.am21.codex.model.Cards.Commons.CardsLoader;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.Cards.DrawingCardSource;
import polimi.ingsw.am21.codex.model.GameBoard.DrawingDeckType;
import polimi.ingsw.am21.codex.model.GameBoard.GameBoard;

class CliPrintableTest {

  @Test
  void printCards() {
    GameBoard gb = new GameBoard(new CardsLoader());

    try {
      System.out.println(
        gb
          .drawCard(DrawingCardSource.Deck, DrawingDeckType.RESOURCE)
          .cardToAscii(null)
      );
      System.out.println(
        gb
          .drawCard(DrawingCardSource.Deck, DrawingDeckType.GOLD)
          .cardToAscii(null)
      );
    } catch (EmptyDeckException e) {
      throw new RuntimeException(e);
    }
  }
}
