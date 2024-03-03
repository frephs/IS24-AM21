# UML progetto di ingegneria del software
 A rough view of the UML of Model View Controller:
- **Model**: 
    - *Game*: enforces the games rhythm, general rules about the game status, calls for turns and rounds, creates the game boards (common and personal), instantiates the players (passing their boards as parameters) and the decks of cards, keeps track of the game state and the game over condition.
    - *Player*: can play a turn (try to play a card on the player board given as parameter to their constructor and receive drawn cards), has points, 
        - *Player Board*: enforces rules for card placement and keeps tracks of resources, objets the geometry of the cards placed onto it
    - *Cards hierarchy*: The uml is pretty self explanatory
    - *GameBoard*: composed of
        - *Score Board*: hashmap with buckets, of (lists of) player tokens (optional utility to useful for the GUI development eventually)
        - *Common Board*: common cards for everyone to use, methods to get and draw cards from here (the latter also draws a new card from the deck) (card retrieval methods comprehend common goals retrival for point calculation). 
        - *Card's Decks* : methods to shuffle and retrieve cards from the decks.
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

class ResourcesTypes {
    <<Enumeration>>
    PLANT_KINGDOM
    ANIMAL_KINGDOM
    FUNGI_KINGDOM
    INSECT_KINGDOM

    +toString() String

}

class ObjectTypes{
    <<Enumeration>>
    QUILL
    INKWELL
    MANUSCRIPT

    +toString() String
}

class Card {
    %% potrebbe essere un'interfaccia
    <<Abstract>>
    +createCard() void
}


class CardSide {
    +corners: Optional~Corner~ [4] 
    %%+SidedCard(corners[]) 
}

class SidedCard {
    +CardSide front;
    +CardSide back;
    +SidedCard(CardSide, CardSide)
}

class Corner~T~{
    +content: Optional~T~
    +linkedCorner: Optional~Corner~
    +isLinked() bool
    +link(Corner) void
    getActualContent() T 
    %% returns the content of the linked corner if it's linked, else returns the content of the corner, also depending on which card is above (the one underneath will be the one that has the linked corner)
}

class Linkable{
    <<Interface>>
    -link() void
    %% couples that connect are 1-4 2-3 with transitivity
    %% come implementiamo link? 
}

class GoldCard{
    conditionalSet: ResourceType[5]
    %% o è meglio una lista?
}

class ResourceCard{
    +kingdom: ResourceType
    +points: Optional~int~
}

class StarterCard{
    +Corner~ResourceType~() 
    %% solo risorse niente oggetti negli angoli.
    +Token 
    %% useful for the GUI
}

class ObjectiveCard{
    +points: final int
    +objective: Optional~ObjectTypes[3]~
    %% è meglio una lista?
    +conditionalRule() bool
}

class Objective{
    <<abstract class>>
    +evaluate() bool
}

class GeometricObjective{
    +geometry: TODO
    +evaluate() bool
}

class CountingObjective{
    resources: HashMap~ResourceType, int~
    objects: HashMap~Objects, int~
    evaluate() bool
}

Card <|-- SidedCard : Inherits from
CardSide "1"--* "4" Corner: is composed of
SidedCard  "1"*--"2" CardSide : is composed of 
SidedCard  "2"*--"1" ResourceCard : is composed of 
SidedCard "2"*--"1" StarterCard: is composed of 
Card*-- ObjectiveCard: is composed of 
ResourceCard <|-- GoldCard : inherits from
GoldCard --|> Linkable : implements
ResourceCard --|> Linkable : implements
StarterCard --|> Linkable : implements
ObjectiveCard --* Objective : is composed of
Objective ..|> GeometricObjective : realization
Objective ..|> CountingObjective : realization




```

### Game model

```mermaid

---
title: Rest of the model
---
classDiagram

class TokenColors{
    <<Enumeration>>
    RED
    BLUE
    GREEN
    YELLOW
    BLACK
    
    +toString() String
}


class Game{
    -tokens: Token[9] 


    -players: List~Player~ 
    +gameBoard: GameBoard
    
    +Game()
    +isGameOver() bool
    %%GameOver() void

}


class Deck {
    -cards: Set~Card~ 
    %% Una pila forse
    +Deck(Card[])
    -shuffle() void
    -draw() Card
    -draw(int) Card []
    -cardsLeft() int
}

class Player {
    %%nickname: String forse va nel client o forse si può mantenere per la persistenza del client.
    -points: int
    -cards: SidedCards[3]
    objectiveCards: ObjectiveCard[2]
    token: Token
    board: personalBoard
    %%+chooseToken(Token [] availableTokens) Token maybe implemented in client?
    +Player(personalBoard)
    +getPoints() int
    -setPoints(int) void
    %% points are abviously private
    -playTurn() void
    -drawCard() card

}

class Token {
    +color: TokenColors
    +Token(TokenColors)

}

class GameBoard {
    goldDeck : deck~GoldCard~
    resourceDeck: deck~ResourceCard~
    starterDeck: deck~StarterCard~
    objectiveDeck: deck~ObjectiveCard~ 
    -commonBoard: CommonBoard
    + scoreBoard: ScoreBoard
    +GameBoard()
    %% ? +ceint[30]
}

class CommonBoard{
    -goldCards: GoldCard[2]
    -resourceCards: ResourceCard[2]
    -objectiveCards: ObjectiveCard[2]
    
    +CommonBoard()
    %% without parameters cause we'll draw 2 cards with deck.draw(2) for each type.

    drawGoldCard() GoldCard
    drawResourceCard() ResourceCard
    %% the card drawn are replaced
    +getObjectiveCards() ObjectiveCard[]
}

class ScoreBoard {
    -buckets: HashMap ~int~~Token~
    +addPoints(TokenColors, int) void
    +getPoints(TokenColors) int
    +getPoints() HashMap~TokenColors, int~

}

class PersonalBoard {
    cards: SidedCard[3]
    geometry: TODO
    resources: HashMap~ResourceType, int~
    objects: HashMap~Objects, int~
    placeCard(SidedCard) bool
    evaluatePoints() int
}

Game "2"--"4" Player : has
Game "1"--"1" GameBoard : is composed of
Game "1"--*"9" Token : is composed of
Token -- TokenColors : uses
GameBoard "1"--*"4" Deck : is composed of
GameBoard "1"--*"1" CommonBoard : is composed of
GameBoard "1"--*"1" ScoreBoard : is composed of

Player --|> Iterable : implements
Player --* PersonalBoard: composed of

```

## Considerazioni
La ratio è implementare ogni elemento che può diventare grafico come una classe a se stante, in modo tale che ci sia corrispondenza una volta che si implementa la view. Ogni elemento avrà un decorator toString per la realizzare la cli e un metodo per disegnarlo sulla GUI



