package polimi.ingsw.am21.codex.controller.messages;

public enum MessageType {
  CONFIRM,
  NEXT_TURN_ACTION,
  PLACE_CARD,
  CREATE_GAME,

  JOIN_LOBBY,
  LEAVE_LOBBY,
  SELECT_OBJECTIVE,
  SELECT_CARD_SIDE,
  SET_NICKNAME,
  SET_TOKEN_COLOR,

  GET_GAME_STATUS,
  GET_AVAILABLE_GAME_LOBBIES,
  GET_OBJECTIVE_CARDS,
  GET_STARTER_CARD_SIDE,

  GAME_STATUS,
  LOBBY_STATUS,
  AVAILABLE_GAME_LOBBIES,
  OBJECTIVE_CARDS,
  STARTER_CARD_SIDES,

  INVALID_CARD_PLACEMENT,
  GAME_FULL,
  GAME_NOT_FOUND,
  NICKNAME_ALREADY_TAKEN,
  TOKEN_COLOR_ALREADY_TAKEN,
  ACTION_NOT_ALLOWED,
  NOT_A_CLIENT_MESSAGE,
  UNKNOWN_MESSAGE_TYPE,

  CARD_PLACED,
  GAME_OVER,
  NEXT_TURN_UPDATE,
  PLAYER_JOINED_GAME,
  PLAYER_SCORE_UPDATE,
  REMAINING_TURNS,
  WINNING_PLAYER,
  AVAILABLE_TOKEN_COLORS,
  GAME_CREATED,
  GAME_DELETED,
  GAME_STARTED,
  PLAYER_JOINED_LOBBY,
  PLAYER_LEFT_LOBBY,
  PLAYER_SET_NICKNAME,
  PLAYER_SET_TOKEN_COLOR,
}
