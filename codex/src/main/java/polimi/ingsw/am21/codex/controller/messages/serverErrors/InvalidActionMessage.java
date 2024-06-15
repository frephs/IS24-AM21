package polimi.ingsw.am21.codex.controller.messages.serverErrors;

import java.util.List;
import java.util.Optional;
import polimi.ingsw.am21.codex.controller.exceptions.*;
import polimi.ingsw.am21.codex.controller.exceptions.InvalidActionException.InvalidActionCode;
import polimi.ingsw.am21.codex.controller.messages.ErrorMessage;
import polimi.ingsw.am21.codex.controller.messages.MessageType;
import polimi.ingsw.am21.codex.model.Cards.Commons.EmptyDeckException;
import polimi.ingsw.am21.codex.model.GameBoard.exceptions.TokenAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.LobbyFullException;
import polimi.ingsw.am21.codex.model.Lobby.exceptions.NicknameAlreadyTakenException;
import polimi.ingsw.am21.codex.model.Player.IllegalCardSideChoiceException;
import polimi.ingsw.am21.codex.model.Player.IllegalPlacingPositionException;
import polimi.ingsw.am21.codex.model.exceptions.GameNotReadyException;
import polimi.ingsw.am21.codex.model.exceptions.GameOverException;
import polimi.ingsw.am21.codex.model.exceptions.InvalidNextTurnCallException;
import polimi.ingsw.am21.codex.model.exceptions.PlayerNotFoundGameException;

public class InvalidActionMessage extends ErrorMessage {

  private final InvalidActionCode code;
  private final List<String> notes;

  public InvalidActionMessage(InvalidActionCode code, List<String> notes) {
    super(MessageType.INVALID_ACTION);
    this.code = code;
    this.notes = notes;
  }

  public InvalidActionCode getCode() {
    return code;
  }

  public Optional<List<String>> getNotes() {
    return Optional.ofNullable(notes);
  }

  @Override
  public String toString() {
    return getType().toString();
  }

  public static InvalidActionMessage fromException(InvalidActionException e) {
    return new InvalidActionMessage(e.getCode(), e.getNotes());
  }

  public InvalidActionException toException() {
    return switch (code) {
      case PLAYER_NOT_ACTIVE -> new PlayerNotActive();
      case NOT_IN_GAME -> new NotInGameException();
      case GAME_ALREADY_STARTED -> new GameAlreadyStartedException();
      case INVALID_NEXT_TURN_CALL -> new InvalidNextTurnCallException();
      case GAME_NOT_READY -> new GameNotReadyException();
      case INVALID_GET_OBJECTIVE_CARDS_CALL -> new InvalidGetObjectiveCardsCallException();
      case GAME_NOT_FOUND -> GameNotFoundException.fromExceptionNotes(notes);
      case PLAYER_NOT_FOUND -> PlayerNotFoundException.fromExceptionNotes(
        notes
      );
      case INCOMPLETE_LOBBY_PLAYER -> IncompleteLobbyPlayerException.fromExceptionNotes(
        notes
      );
      case EMPTY_DECK -> new EmptyDeckException();
      case ALREADY_PLACED_CARD -> new AlreadyPlacedCardException();
      case ILLEGAL_PLACING_POSITION -> IllegalPlacingPositionException.fromExceptionNotes(
        notes
      );
      case ILLEGAL_CARD_SIDE_CHOICE -> new IllegalCardSideChoiceException();
      case LOBBY_FULL -> LobbyFullException.fromExceptionNotes(notes);
      case NICKNAME_ALREADY_TAKEN -> NicknameAlreadyTakenException.fromExceptionNotes(
        notes
      );
      case INVALID_TOKEN_COLOR -> new InvalidTokenColorException();
      case TOKEN_ALREADY_TAKEN -> TokenAlreadyTakenException.fromExceptionNotes(
        notes
      );
      case GAME_OVER -> new GameOverException();
    };
  }
}
