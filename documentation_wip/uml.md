# UML progetto di ingegneria del software

A rough view of the UML of Model View Controller:

- **Model**: will be connected to the server side controller
  - _Game_: enforces the games rhythm, general rules about the game status, calls for turns and rounds, creates the game boards (common and personal), instantiates the players (passing their boards as parameters) and the decks of cards, keeps track of the game state and the game over condition.
  - _Player_: can play a turn (try to play a card on the player board given as parameter to their constructor and receive drawn cards), has points,
    - _Player Board_: enforces rules for card placement and keeps tracks of resources, objets the geometry of the cards placed onto it
  - _Cards hierarchy_: The uml is pretty self explanatory
  - _GameBoard_: composed of (composition class to keep things tidy (is it necessary tho?)
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

### Cards

```mermaid
classDiagram

class ResourceType {
    <<Enumeration>>
    PLANT_KINGDOM
    ANIMAL_KINGDOM
    FUNGI_KINGDOM
    INSECT_KINGDOM

    +toString() String
    +has(Object value) boolean
}

class ObjectType {
    <<Enumeration>>
    QUILL
    INKWELL
    MANUSCRIPT

    +toString() String
    +has(Object value) boolean
}

class CardSideType {
    <<Enumeration>>
    FRONT
    BACK
}

class CornerPosition {
    <<Enumeration>>
    UP_LEFT
    DOWN_LEFT
    UP_RIGHT
    DOWN_RIGHT
}

class Card {
    <<Abstract>>
    -id: int

    Card(int id)

    getId() int
    evaluate(PlayerBoard playerBoard) int*
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
}
PlayableSide "1" *-- "1..4" Corner: composition

class PointConditionType {
    <<Enumeration>>
    OBJECTS
    CORNERS
}

class ObjectiveCard {
    -points: int
    -objective: Objective

    ObjectiveCard(int id, int points, Objective objective)

    evaluate(PlayerBoard playerBoard) int
    %% return points * objective.evaluate()
}

class Objective {
    <<Abstract>>
    evaluate(PlayerBoard playerBoard) int*
    %% lo realizzeremo dentro evaluate count: int
}
ObjectiveCard "1" *-- "1" Objective: composition
Card <|.. ObjectiveCard: realization 

class GeometricObjective {
    -geometry: ResourceType[3][3]

    GeometricObjective(ResourceType[3][3] geometry)

    evaluate(PlayerBoard playerBoard) int
}
Objective <|.. GeometricObjective : realization
%% ResourceType "3..n" <-- "n" GeometricObjective: dependency


class CountingObjective {
    -resources: HashMap~ResourceType; int~
    -objects: HashMap~ObjectType; int~
    
    CountingObjective(HashMap~ResourceType; int~ resources, HashMap~ObjectType; int~ objects)
    
    evaluate(PlayerBoard playerBoard) int
}
Objective <|.. CountingObjective : realization
%% ResourceType "0..4" <-- "n" CountingObjective: dependency
%% ObjectType "0..3" <-- "n" CountingObjective: dependency

class PlayableCard {
    -frontSide: PlayableFrontSide
    -backSide: PlayableBackSide
    -playedSide: CardSideType[0..1]
    -coveredCorners: int
    -kingdom: ResourceType[0..1]

    PlayableCard(int id, PlayableSide front, PlayableSide back)
    PlayableCard(int id, PlayableSide front, PlayableSide back, ResourceType kingdom)

    getKingdom() ResourceType[0..1]
    getPlayedSide() PlayableSide
    setPlayedSide(CardSideType sideType) void
    getCoveredCorners() int
    setCoveredCorners(int n) void
    evaluate(PlayerBoard playerBoard) int
}
Card <|.. PlayableCard: realization
%% CardSideType "0..1" <-- "n" PlayableCard: dependency

class PlayableSide {
    <<Abstract>>
    -corners: Corner[1..4]

    getCorners() Corner[1..4]
    setCorner(CornerPosition position, ResourceType resource)
    setCorner(CornerPosition position, ObjectType object)
    evaluate(PlayerBoard playerBoard) int*
}
%% CornerPosition "1..4" <-- "n" PlayableSide: dependency
%% ResourceType "0..4" <-- "n" PlayableSide: dependency
%% ObjectType "0..4" <-- "n" PlayableSide: dependency

class PlayableBackSide {
    -permanentResources: ResourceType[1..3]

    PlayableBackSide(ResourceType[1..3] permanentResources)

    getResources() ResourceType[1..3]
    evaluate(PlayerBoard playerBoard) int
}
PlayableSide <|.. PlayableBackSide: realization
PlayableCard "1" *-- "1"  PlayableBackSide: composition
%% ResourceType "1..3" <-- "n" PlayableBackSide: dependency

class PlayableFrontSide {
    <<Abstract>>
}
PlayableSide <-- PlayableFrontSide: inheritance
PlayableCard "1" *-- "1" PlayableFrontSide: composition

class StarterCardFrontSide {
    StarterCardFrontSide()

    evaluate(PlayerBoard playerBoard) int
}
PlayableFrontSide <|.. StarterCardFrontSide: realization

class ResourceCardFrontSide {
    -points: int

    ResourceCardFrontSide(int points)

    evaluate(PlayerBoard playerBoard) int
}
PlayableFrontSide <|.. ResourceCardFrontSide: realization

class GoldCardFrontSide {
    -placementCondition: ResourceType[1..5]
    -pointCondition: PointConditionType[0..1]
    -pointConditionObject: ObjectType[0..1]

    GoldCardFrontSide(int points, ResourceType[1..5] placementCondition, PointConditionType[0..1] pointCondition, ObjectType[0..1] pointConditionObject)

    evaluate(PlayerBoard playerBoard) int
}
ResourceCardFrontSide <|-- GoldCardFrontSide: inheritance
%% ResourceType "1..5" <-- "n" GoldCardFrontSide: dependency
%% PointConditionType "0..1" <-- "n" GoldCardFrontSide: dependency
%% ObjectType "0..1" <-- "n" GoldCardFrontSide: dependency
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

    +toString() String
}
 

class Game {
    -tokens: TokenColor[8] 
    -players: Player[2..4] 
    -gameBoard: GameBoard
    -state: GameStates[0..1]
    -scores: HashMap~string, int~
    -currentPlayer: Player


    Game(int players)
    %% contstructor: creates all the game assets.

    -isGameOver() boolean
    %%GameOver() void
    
    addPlayer(String nickname) boolean
    %% TODO: decidere come gestire il caso in cui viene rifiutata la richiesta di aggiunta di un giocatore (nickname già presente o troppi giocatori)

    %% TODO
    getGameState() GameState

    getPlayerNames() String[2..4]

    playTurn() void
    %% to be specified

    evaluateObjectives()
    %% calls player.evaluateObjectives() for each player with the common objectives as parameter
}

%% TODO decidere se implementarlo come un obietti
class GameState{
    <<Enumeration>>
    GAME_INIT
    %%WAITING
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

    Player(String nickname, PlayableCard starterCard, PlayableCard[3] hand)
    
    getNickname() String
    getToken() TokenColor

    setToken(TokenColor token) void
    setObjectiveCard(ObjectiveCards) void

    getPoints() int
    setPoints(int) void

    drawCard(PlayableCard card) void
    %% receive card and put it in the player's hand

    placeCard(PlayableCard card, CardSidesType side, Position position) void
    %% calls the player board placeCard method with the card as parameter and updates the player's points calling the evaluate method on the played card

    evaluate(ObjectiveCard objectiveCard) void
    %% calls the player board evaluate method with the objective card as parameter
}

class PlayerBoard {
    %%FIXME: type of cards
    -cards: SidedCard[3]
    -objectiveCard: ObjectiveCard
    
    -playedCards: HashMap~Position, PlayableCard~
    %% the geometry is an hashmap of positions and played cards
    
    -availableSpots: Set~Position~
    %% the available spots for the player to place a card on the board

    -resources: HashMap~ResourceType, int~
    -objects: HashMap~Objects, int~
    %% the resources and objects the player has on the board

    PlayerBoard(PlayableCard[3] cards, PlayableCard starterCard)
    
    setObjectiveCard(ObjectiveCard objectiveCard) void
    %% sets the objective card in the player board after the player has chosen it

    placeCard(PlayableCard card, cardSidesType side, Position position) void
    %% sets the played side in the card object, puts the card in the played cards hashmap and updates the available spots and player's resources and objects
    updateResourcesandObjects(PlayableCard playedCard, Position position) void
    %% updates the player's resources and objects after a card has been placed on the board

    updateAvailableSpots(Position position) void
    %% updats the list of available spots in which card can be placed

    %%evaluate(PlayedCard card) int
    %%evaluate(ObjectiveCard objectiveCard) int
    %% 2 overloads of the evaluate method, the first one is called on Playable cards every turn, the second one is called on the objective card at the end of the game.
}

class PlayerActions {
    <<Interface>>
    chooseTokenColor(int choice) TokenColor
    chooseObjectiveCard(int choice) ObjectiveCard

    chooseDrawingSource(int choice) DrawingSourceType
    chooseDrawingDeck(int choice) DrawingDeckType

    choosePlayingCard(int choice) PlayableCard
    choosePlayingCardSide(int choice) CardSidesType
    choosePlayingCardPosition(int choice) Position
}

class DrawingDeckType {
    <<Enumeration>>
    GOLD_DECK
    RESOURCE_DECK
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
    
    %% Overriding default hashmap key methods
    equals(Position position) boolean
    computeLinkingPosition(CornerEnum linkedCorner) Position
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
    
    %% the game board has two constructors, one with parameters and one without
    %% the constructor with parameters is used to restore a game from a save file
    GameBoard(CardPair~Goldcards~ goldCards, CardPair~PlayableCard~ resourceCards, \nCardPair~ObjectiveCard~ objectiveCards, \nDeck~PlayableCard~ starterDeck, \nDeck~ObjectiveCard~ objectiveDeck, \nDeck~PlayableCard~ resourceDeck, \nDeck~GoldCard~ goldDeck)
    %% the constructor without parameters is used to create a new game
    GameBoard(List~PlayableCard~ goldCardsList,\n \nList~PlayableCard~ starterCardsList, \nList~ObjectiveCard~ objectiveCardsList, \nList~PlayableCard~ resourceCardsList)

    drawGoldCardFromDeck() PlayableCard ~~throws~~ EmptyDeckException
    drawGoldCardFromPair(boolean first) PlayableCard ~~throws~~ EmptyDeckException
    getGoldCards() CardPair~PlayableCard~
    getGoldCardsLeft() int

    drawStarterCard() PlayableCard ~~throws~~ EmptyDeckException
    getStarterCardsLeft() int
    
    drawObjectiveCardFromDeck() ObjectiveCard ~~throws~~ EmptyDeckException
    drawObjectiveCardFromPair(boolean first) ObjectiveCard ~~throws~~ EmptyDeckException
    getObjectiveCards() CardPair~ObjectiveCard~
    getObjectiveCardsLeft() int

    drawResourceCardFromDeck() PlayableCard ~~throws~~ EmptyDeckException
    drawResourceCardFromPair(boolean first) PlayableCard ~~throws~~ EmptyDeckException
    getResourceCards() CardPair~PlayableCard~
    getResourceCardsLeft() int
}

class EmptyDeckException {
    
}

Game "2"*--"4" Player : is composed of
Game "1"*--"1" GameBoard : is composed of
Game "1"*--"9" TokenColor : is composed of
GameBoard "1"*--"4" Deck : is composed of

PlayerBoard <-- Position : uses
Player --* PlayerBoard: composed of

Player <-- DrawingSourceType : uses
Player <-- DrawingDeckType : uses
Player --> PlayerActions : offers

GameBoard --|> EmptyDeckException : composition
Deck --|> EmptyDeckException : composition

GameBoard <-- CardPair: uses
```

## Considerations

The rationale is to implement every element that can become graphical as a separate class, so that there is a correspondence once the view is implemented. Each element will have a decorator toString to realize the cli and a method to draw it on the GUI
