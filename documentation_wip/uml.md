# UML progetto di ingegneria del software

A rough view of the UML of Model View Controller:

- **Model**: will be connected to the server side controller
  - _Game_: enforces the games rhythm, general rules about the game status, calls for turns and rounds, creates the game boards (common and personal), instantiates the players (passing their boards as parameters) and the decks of cards, keeps track of the game state and the game over condition.
  - _Player_: can play a turn (try to play a card on the player board given as parameter to their constructor and receive drawn cards), has points,
    - _Player Board_: enforces rules for card placement and keeps tracks of resources, objets the geometry of the cards placed onto it
  - _Cards hierarchy_: The uml is pretty self explanatory
  - _GameBoard_: composed of composition class to keep things tidy (is it necessary tho?)
    - _Score Board_: hashmap with buckets, of (lists of) player tokens (optional utility to useful for the GUI development eventually)
    - _Common Board_: common cards for everyone to use, methods to get and draw cards from here (the latter also draws a new card from the deck) (card retrieval methods comprehend common goals retrival for point calculation).
    - _Card's Decks_ : methods to shuffle and retrieve cards from the decks.
    -
- **View**:
  - Cli: text based implementation of the game components for the client
  - Gui: javaFX implementation of the game components for the client
- **Controller**:
  - Client: will handle input received from the respective views and will submit it to the server.
  - Server: will instantiate the game and handle the communications with all the clients.

## Model
As a team, we made the choice to implement part of the game logic in our model because we wanted the controller layer in the server, to be as light as possible. This way the role of the controller layer is to parse the inputs coming from the client controller (communications), calling the model methods to update the game, player, gameboard and playerboard statuses with the parsed data and finally to signal the views to update. 

#### Note on the uml
For development and accessability purposes we split the model class diagram in two parts: the card hierarchy and the rest of the model, so that the most meaningful connections would be easily visibile in the diagram. For the same purpose we deliberately omitted some of the connections between some classes(notably enums) to avoid the graph getting super busy.

#### Documenting choices 
Some design choices we took that we think are worth documenting into detail are:
1. The hybrid approach to evaluating objectives and card placement points: to reflect actual game dynamics we decided to make the cards return a Function which will populated with the playerBoard attributes in the playerBoard context. This way we avoid sending around the playerBoard instances, which we considered a bad practice, and avoid duplicating it just to have the cards evaluate the points with their specificity, which we obtain nonetheless with this approach.      

2. The use of the Builder pattern for the Player class: since the player attributes are all final once chosen by the client, we decided to store PlayerBuilder instances in a hashmap in the Lobby class as the controller layer handles the parsing of the client inputs. This way a player is added to the Game's Player list only when finalized (once they have chosen their Secret Objective).

3. Some classes can be considered redundant as they could be easily implemented as part of the class they are a composition to. We decided to keep them separate to keep the code more readable and to make the implementation of the GUI easier as these entities are effectively functional on their own and can be considered "Drawable". 

### Cards

```mermaid
classDiagram

class ResourceType {
    <<Enumeration>>
    PLANT
    ANIMAL
    FUNGI
    INSECT

    +fromString(String resourceTypeStr) String
    +has(Object value) boolean
    +isResourceType(String value) boolean
    +acceptVisitor(COrnerCOntentVisitor visitor) void
    +acceptVisitor(CornerContentVisitor visitor, int arg) void
    +getColor() Color
    +getImagePath() String
}

class ObjectType {
    <<Enumeration>>
    QUILL
    INKWELL
    MANUSCRIPT

    +fromString(String str) ObjectType
    +has(Object value) boolean
    +isObjectType(String value) boolean
    +acceptVisitor(CornerContentVisitor visitor) void
    +aceeptVisitor(CornerContentVisitor visitor, int arg) void
    +getColor() Color
    +getImagePath() String
}

class CardSideType {
    <<Enumeration>>
    FRONT
    BACK
}

class AdjacentPosition {
    <<Interface>>
}

AdjacentPosition <|.. CornerPosition : Realization
AdjacentPosition <|.. EdgePosition : Realization

class CornerContentType {
    <<Interface>>
    acceptVisitor(CornerContentVisitor visitor) void
    acceptVisitor(CornerContentVisitor visitor, int arg) void
    getColor() Color
}
CornerContentType <|.. ObjectType : Realization
CornerContentType <|.. ResourceType : Realization

class CornerContentVisitor {
    <<Interface>>
    visit(ObjectType object, int diff) void
    visit(ResourceType resource, int diff) void
    visit(ObjectType object, int diff)
    visit(ResourceType resource, int diff)
}

CornerContentVisitor <|.. MapUpdater: Realization




class CornerPosition {
    <<Enumeration>>
    TOP_LEFT
    BOTTOM_LEFT
    TOP_RIGHT
    BOTTOM_RIGHT

    -index: int
    CornerPosition(int index)
    getOppositeCornerPosition() CornerPosition
    +getIndex() int
    +getOppositeCornerPosition() CornerPosition
}

class EdgePosition{
    TOP
    CENTER
    BOTTOM
}

class Card {
    <<Abstract>>
    -id: int

    Card(int id)

    getId() int
    getEvaluator() Function~PlayerBoard; Integer points~ *
}

class Corner~T~ {
    %%set in the constructor
    -content: Optional~T~
    -isCovered: boolean

    Corner()
    Corner(T content)

    isEmpty() bool
    getContent() Optional~T~
    cover() void
    isCovered() bool
}
PlayableSide "1" *-- "1..4" Corner: composition

class PointConditionType {
    <<Enumeration>>
    OBJECTS
    CORNERS

    fromString(String str) PointConditionType
}

class ObjectiveCard {
    -points: int
    -objective: Objective

    ObjectiveCard(int id, int points, Objective objective)

    %% return points * objective.evaluate()
    getEvaluator() Function~PlayerBoard pb; Integer points~ 
    %% implements the abstract method in Card

    cardToString() String
    cardToAscii(HashMap~Integer, String~ cardStringMap) String
}
Card <|.. ObjectiveCard : realization

class Objective {
    <<Abstract>>
    getEvaluator() Function~PlayerBoard pb; Integer points, Integer~ *
}
ObjectiveCard "1" *-- "1" Objective: composition
Card <|.. ObjectiveCard: realization

class GeometricObjective {
    -geometry: HashMap~AdjacentPosition, ResourceType~

    GeometricObjective(HashMap ~AdjacentPostion, ResourceType~ geometry)

    getEvaluator() BiFunction~PlayerBoard pb; Integer points; Integer~

    cardToString() String

    cardToAscii(HashMap~Integer, String~ cardStringMap) String
}
Objective <|.. GeometricObjective : realization
%% ResourceType "3..n" <-- "n" GeometricObjective: dependency


class CountingObjective {
    -resources: HashMap~ResourceType; Integer~
    -objects: HashMap~ObjectType; Integer~

    CountingObjective(HashMap~ResourceType; int~ resources, HashMap~ObjectType; int~ objects)
   
    getEvaluator() BiFunction~PlayerBoard pb; Integer points, Integer~

    cardToString() String
    cardToAscii() String

}
Objective <|.. CountingObjective : realization
%% ResourceType "0..4" <-- "n" CountingObjective: dependency
%% ObjectType "0..3" <-- "n" CountingObjective: dependency

class PlayableCard {
    -frontSide: PlayableFrontSide
    -backSide: PlayableBackSide
    -playableSideType: Optional~CardSideType~
    -coveredCorners: int
    -kingdom: Optional~ResourceType~

    PlayableCard(int id, PlayableSide front, PlayableSide back)
    PlayableCard(int id, PlayableSide front, PlayableSide back, ResourceType kingdom)

    getKingdom() Optional~ResourceType~
    getPlayedSideType() Optional~CardSideType~
    getSides() List~PlayableSide~
    getPlayedSide() Optional~PlayableSide~
    getPermanentResources() List~ResourceType~
    setPlayedSideType(CardSideType playedSideType) void
    clearPlayedSide() void
    getCoveredCorners() int
    SetCoveredCorners(int coveredCorners) void
    
    getEvaluator() Function~PlayerBoard pb; Integer points~

    cardToAscii(HashMap~Integer, String~ cardStringMap) String
    cardToString() String
}
Card <|.. PlayableCard: realization
%% CardSideType "0..1" <-- "n" PlayableCard: dependency

class PlayableSide {
    <<Abstract>>
    -corners: HashMap~CornerPosition, Corner~CornerContentType~~

    getCorners() HashMap~CornerPosition, Corner~CornerContentType~~
    setCorner(CornerPosition position, Optional ~CornerContentType~ content)
    getEvaluator() BiFunction~PlayerBoard pb; Integer coveredCorners; Integer points~ *
    getPlaceabilityChecker() Function~Playerboard pb, boolean isPlaceable~

    cardtoAscii(HashMap~Integer, String~ cardStringMap) String
    cardTostring() String
}
%% CornerPosition "1..4" <-- "n" PlayableSide: dependency
%% ResourceType "0..4" <-- "n" PlayableSide: dependency
%% ObjectType "0..4" <-- "n" PlayableSide: dependency

class PlayableBackSide {
    -permanentResources: List~ResourceType~

    PlayableBackSide(List~ResourceType~ permanentResources)

    getPermanentResources() List~ResourceType~
    getEvaluator() BiFunction~PlayerBoard pb, Integer, Integer~

    cardToAscii(HashMap~Integer, String~ cardStringMap)

    cardToString()
}
PlayableSide <|.. PlayableBackSide: realization
PlayableCard "1" *-- "1"  PlayableBackSide: composition
%% ResourceType "1..3" <-- "n" PlayableBackSide: dependency

class PlayableFrontSide {
    <<Abstract>>
   PlayableFrontSide()

}
PlayableSide <-- PlayableFrontSide: inheritance
PlayableCard "1" *-- "1" PlayableFrontSide: composition

class StarterCardFrontSide {
    StarterCardFrontSide()

    getEvaluator() BiFunction~PlayerBoard pb; Integer coveredCorners; Integer points~

    cardToString() String

    cardToAsci(HashMap~Integer, String~ cardStringMap) String
}
PlayableFrontSide <|.. StarterCardFrontSide: realization

class ResourceCardFrontSide {
    -points: int

    ResourceCardFrontSide(int points)

    getEvaluator() BiFunction~PlayerBoard pb; Integer coveredCorners; Integer points~

    cardToAscii(HashMap~Integer, String~) String

    cardToString() String

}
PlayableFrontSide <|.. ResourceCardFrontSide: realization

class DrawingCardSource {
        <<Enumeration>>
    CardPairFirstCard
    CardPairSecondCard
    Deck
    
    toString() String
}


class GoldCardFrontSide {
    -placementCondition: List~ResourceType~
    -pointCondition: Optional~PointConditionType
    -pointCOnditionObject: Optional~ObjectType~

    GoldCardFrontSide(int points, List~ResourceType~ placementCondition, PointConditionType pointCondition, ObjectType pointConditionObject)

    getEvaluator() BiFunction~PlayerBoard pb; Integer coveredCorners; Integer points~
    %%implements the abstract method in PlayableSide

    getPlaceabilityChecker() Function~Playerboard pb, boolean isPlaceable~
    %%overrides the method in PlayableSide

    cardToAscii(HashMap~Integer, String~ cardStringMap) String
    cardToString() String
}
ResourceCardFrontSide <|-- GoldCardFrontSide: inheritance
%% ResourceType "1..5" <-- "n" GoldCardFrontSide: dependency
%% PointConditionType "0..1" <-- "n" GoldCardFrontSide: dependency
%% ObjectType "0..1" <-- "n" GoldCardFrontSide: dependency

class CardPair {
    -first: T
    -second: T

    CardPair(T firstCard, T secondCard) 

    getFirst() T
    getSecond() T

    replaceFirst(T firstCard) T
    replaceSecond(T secondCard) T

    swap() void
}
CardPair <|.. Card: realization

class CardBuilder {
    -id: int
    %% Resource | Starter | Gold | Objective
    -type: CardType 
    -points: Optional~Integer~
    -objectiveType: Optional~TùObjectiveType~
    -objectiveGeometry: Optional~Map<AdjacentPostion, ResourceType>~
    -objectiveResources: Optional~Map<ResourceType, Integer>~
    -objectiveObjects: Optional~Map<ObjectiveType, Integer>~
    %%Resource | STarter | Gold
    -backPermanentResources: Optional~List~ResourceType~~
    -frontCorner: Optional~Map~CornerPosition, Optional~CornerContentType~~~
    -backCorner: Optional~Map~COrnerPostion, Optional~CornerCOntentType~~~

    %%Gold
    -placementCondition: Optional~List~ResourceType~~
    -pointCOndition: Optional~PointConditionType~
    -pointCOnditionObject: Optional~ObjectType~

    CardBuilder(int id, CardType type)

    +checkType(CardType... expected) ~~throws~~ WrongCardTypeExcpetion

    +setPoints(int points) CardBuilder ~~throws~~ WrongCardTypeException

    +setObjectiveType(ObjectiveType objectiveType) CardBuilder ~~throws~~ WrongCardTypeException
    +setObjectiveGeometry(Map~AdjacentPostion, ResourceType~ objectiveGeometry) CardBuilder ~~throws~~ WrongCardTypeException, ConflictingParameterException
    +setObjectiveResources(Map~ResourceType, Integer~ objectiveResources) CardBuilder ~~throws~~ WrongCardTypeException, ConflictingParameterException
    +setObjectiveObjects(Map~ObjectType, Integer~ objectiveObjects) CardBuilder ~~throws~~ WrongCardTypeException, ConflictingParameterException

    +setBackPermanentResources(List~ResourceType~ backPermanentResources) CardBuilder ~~throws~~ WrongCardTypeException

    +setPlacementCondition(List~ResourceType~ placementCondition) CardBuilder ~~throws~~ WrongCardTypeException
    +setPointCondition(PointConditionType pointCondition) CardBuilder ~~throws~~ WrongCardTypeException
    +setPointConditionObject(ObjectType pointConditionObject) CardBuilder ~~throws~~ WrongCardTypeException, ConflictingPrameterException

    +setCorners(CardSideType side, Map~CornerPostion, Optional~CornerContentType~ cornerMap~) CardBuilder ~~throws~~ WrongCardTypeException

    +buildObjectiveCard() ObjectiveCard ~~throws~~ MissingParameterExcpetion 

    +buildPlayableCard() PlayableCard ~~throws~~ MissingParameterExcpetion

    build() Card ~~throws~~ MissingParametersException

    getPlayableCard(List~ResourceType~ permanentResources, PlayableFrontSide frontSide, CardType cardType) PlayableCard
}
Card "1" *-- "1" CardBuilder: composition
ObjectiveCard "0..1" <-- "1" CardBuilder: dependency
GeometricObjective "0..1" <-- "1" CardBuilder: dependency
CountingObjective "0..1" <-- "1" CardBuilder: dependency
PlayableCard "0..1" <-- "1" CardBuilder: dependency
StarterCardFrontSide "0..1" <-- "1" CardBuilder: dependency
ResourceCardFrontSide "0..1" <-- "1" CardBuilder: dependency
GoldCardFrontSide "0..1" <-- "1" CardBuilder: dependency
PlayableBackSide "0..1" <-- "1" CardBuilder: dependency
CardBuilder --> WrongCardTypeException : throws
CardBuilder --> MissingParametersException : throws
CardBuilder --> ConflictingParameterException : throws

%% CardType "1" *-- "1" CardBuilder: composition
%% ObjectType "0..1" <-- "n" CardBuilder: dependency
%% ResourceType "0..1" <-- "n" CardBuilder: dependency
%% PointConditionType "0..1" <-- "n" CardBuilder: dependency


class CardType {
    <<Enumeration>>
    RESOURCE
    STARTER
    GOLD
    OBJECTIVE

    fromString(String str) CardType
}

class ObjectiveType {
    <<Enumeration>>
    GEOMETRIC
    COUNTING

    fromString(String key) ObjectiveType
}

class WrongCardTypeException {
    WrontCardTypeException(String expected, String actual)
}
IllegalStateException <|-- WrongCardTypeException: inheritance

class MissingParametersException {
    MissingParametersException(String missing)
}
IllegalStateException <|-- MissingParametersException: inheritance

class ConflictingParameterException {
    ConflictingParameterException(String paramName, String expectedValue, String currValue)
}
IllegalStateException <|-- ConflictingParameterException: inheritance

```

### Game model

```mermaid

---
title: Rest of the model
---
classDiagram

class TokenColor{
    <<Enumeration>>
    RED
    BLUE
    GREEN
    YELLOW
    BLACK

    +getColor() Color
    +fromString(String color) TokenColor
    +getImagePath() String
}


class Lobby{
    %% the lobby players are stored in a hashmap with the socket id as key and the player builder as value, while the players are being constructed the player builder is updated with the player's attributes
    lobbyPlayers: HashMap~SocketId; PlayerBuilder~

    %% We store the extracted objective cards in a HashMap along with the socket id, ensuring they can be restored to the deck if the player disconnects.
    extractedCards: HashMap~SocketId;CardPair~ObjectiveCard~~
    remainingPlayers: int 
    %% counts how many players you can still add to the game

    %% arraylist of available tokens

    Lobby(int maxplayers)
    Lobby()

    getRemainingPlayerSlots() int
    getPlayersCount() int

    addPlayer(UUID socketId, CardPair~ObjectiveCard~ objectiveCards, PlayableCard starterCard) void

    removePlayer(UUID socketId) Pair~CardPair~ObjectiveCard~, PlayableCard~ ~~throws~~ PlayerNotFoundExcpetion

    setNickname(UUID socketId, String nickname) void ~~throws~~ PlayerNotFoundExcpetion, NicknameAlreadyTakenException

    setToken(UUID socketId, TokenColor tokenColor) void ~~throws~~ PlayerNotFOundExcpetion, TokenAlreadyTakenExcpetion

    setObjectiveCard(UUID socketId, Boolean firstObjectiveCard) void ~~PlayerNotFoundException~~

    finalizePlayer(UUID socketId, CardSideType cardSide, List~PlayableCard~ hand) Player ~~throws~~ PlayerNotFoundException, IncompletePlayerBuilderException, IllegalCardSideChoiceException, IllegalPlacingPositionException

    getPlayerObjectuveCards(UUID socketId) Optional~CardPair~ObjectiveCard~~

    containsSocketID(UUID socketId) boolean
    getPlayerNickname(UUID socketId) Optional~String~ ~~throws~~ PlayerNotFoundException
    getPlayerTokenColor(UUID socketId) Optional~TokenColor~ ~~throws~~ PlayerNotFoundException

    getAvailableColors() Set~TokenColor~
    getPlayerInfo() Map~UUID socketId, Pair~String, TokenColor~~

    getStarterCard(UUID socketId) PlayableCard ~~throws~~ PlayerNotFoundException
}

PlayerNotFoundExcpetion <-- Lobby: throws
NicknameAlreadyTakenException <-- Lobby: throws
TokenAlreadyTakenException <-- Lobby: throws
IncompletePlayerBuilderException <-- Lobby: throws
IllegalCardSideChoiceException <-- Lobby: throws
IllegalPlacingPositionExcpetion <-- Lobby: throws



class LobbyFullExcpetion {
    +LobbyFullException
}

class IllegalCardSideChoiceException {
    +IllegalCardSideChoiceException()
}

class IllegalPlacingPositionException {
    +IllegalPlacingPositionException()
    +IllegalPlacingPositionExcpetion(String message)
}


class NicknameAlreadyTakenException {
    +NicknameALreadyTakenException(String nickname)

    +getNickname() String
}

class Game {
    -WINNING_POINTS: ~final~ int
    -players: List~Player~
    -gameBoard: GameBoard
    -lobby: Lobby
    -state: GameState
    -RemainingRounds: Integer
    -currentPlayer: Integer
    -maxPlayer: Integer
    -chat: Chat

    Game(int players)
    Game(int players, JSONArray cards)

    getLobby() Lobby
    start() void

    drawObjectiveCardPair() CardPair~ObjectiveCard~
    getState() GameState
    getPlayerState(String nickname) PlayerState
    getScoreBoard() Map~String, Integer~
    getCurrentPlayer() Player
    getPlayers() List~Player~
    getPlayerIds() List~String~
    getCurrentPlayerIndex() Integer
    addPlayer(Player player) void
    isGameOver() boole
    setGameOver() void
    getRemainingRounds() Optional~Integer~
    isResourceDeckEmpty() boole
    isGoldDeckEmpty() boole
    areDecksEmpty() boole

    nextTurn(DrawingCardSource drawingSource, DrawingDeckType, deckType, BiConsumer~Integer, Integer~ drawCardsCallback) void ~~throws~~ GameOverException, EmptyDeckExcpetion, InvalidNextTurnCallExcpetion
    nextTurn(DrawingCardSource drawingSource, DrawingDeckType deckType) void ~~throws~~ GameOverExcpetion, EmptyDeckExcpetion, InvalidNextTurnCallExcpetion
    nextTurn() void ~~throws~~ GameOverExcpetion, InvalidNextTurnCallExcpetion

    getPlayerOrder() List~String~
    drawStarterCard() PlayableCard ~~throws~~ EmptyDeckExcpetion
    insertObjectiveCard(ObjectiveCard objectiveCard)
    insertStarterCard(PlayableCard starterCard)
    isLastRound() boole
    drawHand() List~PlayableCard~ ~~throws~~ EmptyDeckExcpetion

    getPlayersCount() Integer
    getMaxPlayer() Integer
    getPlayersSpotsLeft() Integer
    getChat() Chat
    getGameBoard() GameBoard
}

GameOverExcpetion <-- Game: throws
EmptyDeckExcpetion <-- Game: throws
InvalidNextTurnCallExcpetion <-- Game: throws


class GameManger {
    -games: Map~String, Game~

    GameManger()

    getGames() Set~String~
    getCurrentSlots() Map~String, Integer~
    getMaxSlots() Map~String, Integer~
    getGame(String gameName) Optional~Game~
    createGame(String gameName, Integer players) Game
    deleteGame(String gameName) void 
}


class GameOverException {
    +GameOverException()
}

Game --|> Lobby : composition
Game --|> GameOverException : composition
Game --|> EmptyDeckException : composition

class GameState {
    <<Enumeration>>
    GAME_INIT
    PLAYING
    GAME_OVER
}

class PlayerState{
    <<Enumeration>>
    WAITING
    PLAYING
}


class Deck~T~ {
    -cards: List~T~
    %% Una pila forse
    Deck(List~T~ cards)
    shuffle() void
    draw() T ~~throws~~ EmptyDeckException
    draw(int N) List~T~ ~~throws~~ EmptyDeckException
    getCardsLeft() int
    %% insert card back after card drawing. (ie when you don't choose an objective)
    insert(T card) void
}

class Player {
    -nickname: String
    -points: int
    -token: TokenColor
    -board: PlayerBoard
    -socketId: UUID
    -cardPlacedThisTurn: boole

    %% the player constructor takes the bulder, that is used to get necessary information to build the player
    %% and the board that is initialized inside the build() function drawing and setting the necessary cards in hand
    Player(PlayerBuilder builder, UUID socketId)

    getSocketId() UUID
    +getNickname() String
    +getBoard() PlayerBoard
    +getToken() TokenColor
    +getPoints() int
    +hasPlacedCardThisTurn() boole
    +toggleCardPlacedThisTurn() void
    +drawCard(PlayableCard card) void
    +placeCard(int cardIndex, CardSideType side, Position position) PlayableCard
    +evaluate(ObjectiveCard objectiveCard) void
    +evaluateSecretObjective() void
}

Player *-- PlayerBuilder : composition

class PlayerBuilder {
    -nickname: String
    -token: TokenColor
    -objectiveCard: ObjectiveCard
    -starterCard: PlayableCard
    -cards: List~PlayableCard~

    PlayerBuilder(PlayableCard card)

    setNickname(String nickname) PlayerBuilder
    getNickname() Optional~String~
    setTokenColor(TokenColor token) PlayerBuilder
    getTokenColor() Optional~TokenColor~
    getHand() Optional~List~PlayableCard~~
    setHand(List~PlayableCard~ cards) PlayerBuilder
    setStarterCard(PlayableCard starteCard) PlayerBuilder
    setStarterCardSide(CardSideType side) void
    setObjectiveCard(ObjectiveCard objectiveCard) PlayerBuilder
    build(UUID socketId) Player
    getStarterCard() PlayableCard
}

IllegalCardSideChoiceExcpetion <-- PlayerBoard: throws
IllegalPlacingPositionExcpetion <-- PlayerBoard: throws

class PlayerBoard {
    -hand: List~PlayableCard~
    -objectiveCard: ObjectiveCard

    -playedCards: HashMap~Position, PlayableCard~
    %% the geometry is an hashmap of positions and played cards

    -availableSpots: Set~Position~
    -forbiddenSpots: Set~Position~
    %% the available spots for the player to place a card on the board

    -resources: HashMap~ResourceType, Integer~
    -objects: HashMap~Objects, Integer~

    mapUpdater: MapUpdater

    availableSpots: Set~Position~
    forbiddenSpots: Set~Position~
    
    PlayerBoard(List~PlayableCard~ hand, PlayableCard starterCard, ObjectiveCard objectiveCard) ~~throws~~ IllegalCardSideChoiceException, IllegalPlacingPositionException

    getObjectiveCard() ObjectiveCard
    getHand() List~PlayableCard~
    getPlaceableCardSides() List~PlayableSide~

    drawCard(PlayableCard card)
    placeCard(PlayableCard playedCard, CardSdieType playedSideType, Position position)

    updateResourcesAndObjects(PlayableCard playedCard, Position position)
    updateResourcesAndObjectsMaps(Corner~T~ corner, int delta)
    updateAvailableSpots(PlayableSide playableSide, Position position)
    getObjects() Map~ObjectType, Integer~
    getResources() Map~ResourceType, Integer~
    getPlayedCards() Map~Position, PlayableCard~
    getAvailableSpots() Set~Position~
    getForbiddenSpots() Set~Position~
}

class MapUpdater {
    MapUpdater()
    visit(ObjectType object, int delta) void
    visit(ResourceType resource, int delta) void
}
CornerContentVisitor <.. MapUpdater : realization
PlayerBoard *-- MapUpdater : contains

%%class PlayerActions {
%%    <<Interface>>
%%    chooseTokenColor(int choice) TokenColor
%%    chooseObjectiveCard(int choice) ObjectiveCard
%%
%%    chooseDrawingSource(int choice) DrawingSourceType
%%    chooseDrawingDeck(int choice) DeckType
%%
%%    choosePlayingCard(int choice) PlayableCard
%%    choosePlayingCardSide(int choice) CardSidesType
%%    choosePlayingCardPosition(int choice) Position
%%}

class DeckType {
    <<Enumeration>>
    GOLD_DECK
    RESOURCE_DECK
}

class DrawingDeckType {
    <<Enumeration>>
    RESOURCE
    GOLD

    toString() String
}

class DrawingSourceType {
    <<Enumeration>>
    DECK
    COMMON_BOARD
}

class Position{
    -x: int
    -y: int

    Position(int x, int y)
    Position()

    %% Overriding default hashmap key methods
    equals(Object o) boolean
    computeAdjacentPosition(AdjacentPosition adjacentPosition) Position
    hashCode() int
}

class GameBoard {
    -goldDeck : Deck~PlayableCard~
    -goldCards: CardPair~PlayableCard~
    -starterDeck: Deck~PlayableCard~
    -objectiveDeck: Deck~ObjectiveCard~
    -resourceDeck: Deck~PlayableCard~
    -resourceCards: CardPair~PlayableCard~
    -objectiveCards: CardPair~ObjectiveCard~

    GameBoard fromJSON(JSONArray cards)
    GameBoard(CardsLoader loader)
    %% the game board has two constructors, one with parameters and one without
    %% the constructor with parameters is used to restore a game from a save file
    GameBoard(CardPair~Goldcards~ goldCards, CardPair~PlayableCard~ resourceCards, \nCardPair~ObjectiveCard~ objectiveCards, \nDeck~PlayableCard~ starterDeck, \nDeck~ObjectiveCard~ objectiveDeck, \nDeck~PlayableCard~ resourceDeck, \nDeck~GoldCard~ goldDeck)
    %% the constructor without parameters is used to create a new game
    GameBoard(List~PlayableCard~ goldCardsList,\n \nList~PlayableCard~ starterCardsList, \nList~ObjectiveCard~ objectiveCardsList, \nList~PlayableCard~ resourceCardsList)

    drawGoldCardFromDeck() PlayableCard ~~throws~~ EmptyDeckException
    +drawCard() PlayableCard
    +drawCard(DrawingCardSource drawingSource, DrawingDeckType deckType) PlayableCard ~~throws~~ EmptyDeckException
    +getGoldCard() CardPair~PlayableCard~
    +getResourceCards() CardPair~PlayableCard~
    +goldCardsLeft() int
    +drawStarterCardFromDeck() PlayableCard ~~EmptyDeckExcpetion~~
    +starteCardsLeft() int
    drawObjectiveCardFromDeck() ObjectiveCard ~~EmptyDeckExcpetion~~
    getObjectiveCards() CardPair~ObjectiveCard~
    insertObjectiveCard(ObjectiveCard card) void
    insertStarterCard(PlayableCard card) void
    objectiveCardsLeft() int
    drawResourceCardFromDeck() PlayableCard ~~throws~~ EmptyDeckExcpetion
    drawResourceCardFromDeck(int n) List~PlayableCard~
    drawResourceCardFromPair(Boolean first) PlayableCard~EmptyDeckExcpetion~
    resourceCardsLeft() int
    drawObjectiveCardPair() cardPair~ObjectiveCard~ ~~throws~~ EmptyDeckException
}

EmptyDeckExcpetion <-- GameBoard: throws

class PlayerNotFoundExcpetion {
    +PlayerNotFoundException(UUID playerId)
    +PlayerNotFoundExcpetion(String username)
}

class TokenAlreadyTakenExcepetion {
    +TokenALreadyTakenExcpetion(TokenColor color)

    getTokenColor() TokenColor
}

class IncompletePlayerBuilderException {
    +IncompletePlayerBuilderException(String message)

    +checkPlayerBuilder(Player.PlayerBuilder playerBuilder)
}



class EmptyDeckException {
    +EmptyDeckException()
}

Game "2"*--"4" Player : composition
Game "1"*--"1" GameBoard : composition
Game "1"*--"9" TokenColor : composition
GameBoard "1"*--"4" Deck : composition

PlayerBoard <-- Position : uses
Player --* PlayerBoard: composition

Player <-- DrawingSourceType : uses
Player <-- DeckType : uses
%% Player --> PlayerActions : offers

GameBoard --|> EmptyDeckException : composition
Deck --|> EmptyDeckException : composition

GameBoard <-- CardPair: uses
```

## Considerations

The rationale is to implement every element that can become graphical as a separate class, so that there is a correspondence once the view is implemented. Each element will have a decorator toString to realize the cli and a method to draw it on the GUI
