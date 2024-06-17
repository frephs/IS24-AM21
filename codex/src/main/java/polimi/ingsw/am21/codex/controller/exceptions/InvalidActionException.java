package polimi.ingsw.am21.codex.controller.exceptions;

import java.util.List;

public class InvalidActionException extends Exception {

  List<String> notes;

  public enum InvalidActionCode {
    PLAYER_NOT_ACTIVE,
    NOT_IN_GAME,
    GAME_ALREADY_STARTED,
    INVALID_NEXT_TURN_CALL,
    GAME_NOT_READY,
    INVALID_GET_OBJECTIVE_CARDS_CALL,
    GAME_ALREADY_EXISTS,
    INVALID_GAME_NAME,
    GAME_NOT_FOUND,
    PLAYER_NOT_FOUND,
    INCOMPLETE_LOBBY_PLAYER,
    EMPTY_DECK,
    ALREADY_PLACED_CARD,
    ILLEGAL_PLACING_POSITION,
    ILLEGAL_CARD_SIDE_CHOICE,
    LOBBY_FULL,
    NICKNAME_ALREADY_TAKEN,
    INVALID_TOKEN_COLOR,
    TOKEN_ALREADY_TAKEN,
    CARD_NOT_PLACED,
    GAME_OVER;

    public Integer getErrorCode() {
      return this.ordinal();
    }

    public InvalidActionCode getErrorCode(Integer code) {
      return InvalidActionCode.values()[code];
    }

    public String getErrorMessage() {
      return switch (this) {
        case PLAYER_NOT_ACTIVE -> "Player not currently in turn";
        case NOT_IN_GAME -> "Player not in game";
        case GAME_ALREADY_STARTED -> "Game already started";
        case INVALID_NEXT_TURN_CALL -> "Invalid next turn call";
        case INVALID_GET_OBJECTIVE_CARDS_CALL -> "Invalid get objective card call";
        case GAME_NOT_READY -> "Game not ready";
        case GAME_ALREADY_EXISTS -> "Game already exists";
        case INVALID_GAME_NAME -> "Invalid game name";
        case GAME_NOT_FOUND -> "Game not found";
        case PLAYER_NOT_FOUND -> "Player not found";
        case INCOMPLETE_LOBBY_PLAYER -> "Incomplete lobby player";
        case EMPTY_DECK -> "Empty deck";
        case ALREADY_PLACED_CARD -> "You already placed a card";
        case ILLEGAL_PLACING_POSITION -> "You tried placing a card in a position which is either forbidden, occupied or not reachable";
        case ILLEGAL_CARD_SIDE_CHOICE -> "Illegal card side choice";
        case LOBBY_FULL -> "The lobby is full";
        case NICKNAME_ALREADY_TAKEN -> "Nickname already taken";
        case INVALID_TOKEN_COLOR -> "Invalid token color";
        case TOKEN_ALREADY_TAKEN -> "Token already taken";
        case GAME_OVER -> "Game over";
        case CARD_NOT_PLACED -> "Card not placed yet, place a card before drawing";
      };
    }
  }

  private final InvalidActionCode code;

  public InvalidActionException(InvalidActionCode code) {
    super(code.getErrorMessage());
    this.code = code;
    this.notes = null;
  }

  public InvalidActionException(InvalidActionCode code, List<String> notes) {
    super(code.getErrorMessage());
    this.code = code;
    this.notes = notes;
  }

  public InvalidActionCode getCode() {
    return this.code;
  }

  public List<String> getNotes() {
    if (this.notes == null) {
      return List.of();
    }
    return this.notes;
  }
}
