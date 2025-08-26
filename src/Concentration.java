import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class Concentration extends World {
  ArrayList<ArrayList<Card>> board;
  int score;
  int stepsLeft; // Tracks remaining steps
  int maxSteps; // Maximum steps allowed
  ArrayList<Card> flippedCards;
  static final int CARD_WIDTH = 50;
  static final int CARD_HEIGHT = 70;
  int flipBackCounter = 0; // Counter to manage delay
  int timeElapsed = 0;

  Concentration() {
    this.board = new ArrayList<>();
    this.score = 26;
    this.maxSteps = 100;
    this.stepsLeft = this.maxSteps;
    this.flippedCards = new ArrayList<>();
    this.initBoard();
  }

  public static void main(String[] args) {
    Concentration world = new Concentration();
    world.bigBang(700, 450, 0.1); // Increase height for score display
  }

  public void initBoard() {
    ArrayList<Card> deck = new ArrayList<>();
    ArrayList<String> suits = new ArrayList<>(Arrays.asList("♣", "♦", "♥", "♠"));

    for (String suit : suits) {
      for (int rank = 1; rank <= 13; rank++) {
        deck.add(new Card(rank, suit));
      }
    }

    this.shuffle(deck);

    for (int row = 0; row < 4; row++) {
      ArrayList<Card> cardRow = new ArrayList<>();
      for (int col = 0; col < 13; col++) {
        cardRow.add(deck.get(row * 13 + col));
      }
      this.board.add(cardRow);
    }
  }

  public void shuffle(ArrayList<Card> deck) {
    Random random = new Random();
    for (int i = deck.size() - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      Card temp = deck.get(i);
      deck.set(i, deck.get(j));
      deck.set(j, temp);
    }
  }

  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(700, 450);

    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(i).size(); j++) {
        Card card = board.get(i).get(j);
        WorldImage cardImage = card.render();
        scene.placeImageXY(cardImage, CARD_WIDTH / 2 + j * CARD_WIDTH, CARD_HEIGHT / 2 
            + i * CARD_HEIGHT);
      }
    }

    // Display the timer
    int minutes = timeElapsed / 600;
    int seconds = (timeElapsed / 10) % 60;
    String timeText = String.format("Time: %02d:%02d", minutes, seconds);
    WorldImage timeImage = new TextImage(timeText, 20, Color.BLACK);
    scene.placeImageXY(timeImage, 350, 320); // Position the timer in the bottom center

    // Add score display
    WorldImage scoreDisplay = new TextImage("Score: " + this.score, 20, Color.BLACK);
    WorldImage stepsDisplay = new TextImage("Steps Left: " + this.stepsLeft, 20, Color.BLACK);
    scene.placeImageXY(scoreDisplay, 350, 350); // Position at bottom center
    scene.placeImageXY(stepsDisplay, 350, 380); // Below score display

    return scene;
  }

  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board.clear();
      this.score = 26;
      this.stepsLeft = this.maxSteps;
      this.flippedCards.clear();
      this.initBoard();
      this.timeElapsed = 0;
    }
  }

  public void onMouseClicked(Posn pos) {
    if (this.stepsLeft <= 0) {
      return; // No further actions allowed if steps are exhausted
    }

    int row = pos.y / CARD_HEIGHT;
    int col = pos.x / CARD_WIDTH;

    if (row >= 0 && row < 4 && col >= 0 && col < 13) {
      Card clickedCard = this.board.get(row).get(col);

      if (!clickedCard.faceUp && this.flippedCards.size() < 2) {
        clickedCard.flip();
        this.flippedCards.add(clickedCard);

        if (this.flippedCards.size() == 2) {
          Card firstCard = this.flippedCards.get(0);
          Card secondCard = this.flippedCards.get(1);

          if (firstCard.equals(secondCard)) {
            this.score--;
            this.flippedCards.clear(); // Keep matching cards face-up
          } else {
            this.stepsLeft--; // Updated: Decrement steps only for incorrect pairs
            this.flipBackCounter = 20;
          }
        }
      }
    }
  }

  public void onTick() {
    if (this.flipBackCounter > 0) {
      this.flipBackCounter--;
      if (this.flipBackCounter == 0 && this.flippedCards.size() == 2) {
        this.flippedCards.get(0).flip();
        this.flippedCards.get(1).flip();
        this.flippedCards.clear();
      }
    }
    this.timeElapsed++;
  }
}

class Card {
  int rank; // The rank of the card (1-13)
  String suit; // The suit of the card ("♣", "♦", "♥", "♠")
  boolean faceUp; // Whether the card is face-up

  // Constructs a new card with the given rank and suit
  Card(int rank, String suit) {
    this.rank = rank;
    this.suit = suit;
    this.faceUp = false;
  }

  // Flips the card, toggling its face-up state
  public void flip() {
    this.faceUp = !this.faceUp;
  }

  //Determines if this card is red (hearts or diamonds)
  public boolean isRed() {
    return this.suit.equals("♥") || this.suit.equals("♦");
  }

  // Determines if this card is black (spades or clubs)
  public boolean isBlack() {
    return this.suit.equals("♠") || this.suit.equals("♣");
  }

  // Checks if this card matches another card based on rank and color (red or black)
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true; // Same reference, they are equal
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false; // Null or not the same class, not equal
    }
    Card other = (Card) obj; // Safe cast
    return this.rank == other.rank && (this.isRed() == other.isRed()
        || this.isBlack() == other.isBlack());
  }

  @Override
  public int hashCode() {
    int result = Integer.hashCode(rank); // Use the rank for the hash
    result = 31 * result + suit.hashCode(); // Include the suit in the hash
    return result;
  }

  // Renders the card as a WorldImage
  public WorldImage render() {
    Color beige = new Color(185, 183, 167); // Light purple for card front
    Color teal = new Color(124, 144, 160); // Dark purple for card outline
    Color clubColor = Color.BLACK;
    Color diamondColor = Color.RED;
    Color heartColor = Color.RED;
    Color spadeColor = Color.BLACK;

    // Define the width and height of the card
    int cardWidth = Concentration.CARD_WIDTH;
    int cardHeight = Concentration.CARD_HEIGHT;

    // Outline for the card
    WorldImage outline = new RectangleImage(
        cardWidth + 4, cardHeight + 4,
        OutlineMode.SOLID, teal);

    // Create the back of the card (diamond design)
    WorldImage diamondOutline = new OverlayImage(
        new LineImage(new Posn(cardWidth / 2, cardHeight / 2), teal),  // Left to Top
        new OverlayImage(
            new LineImage(new Posn(cardWidth / 2, -cardHeight / 2), teal),  // Top to Right
            new OverlayImage(
                new LineImage(new Posn(-cardWidth / 2, -cardHeight / 2), teal),  // Right to Bottom
                new LineImage(new Posn(-cardWidth / 2, cardHeight / 2), teal)   // Bottom to Left
                )
            )
        );

    // Rectangle around the diamond design
    WorldImage diamondBorder = new RectangleImage(
        cardWidth - 20, cardHeight - 20, // Adjust the size to fit around the diamond
        OutlineMode.OUTLINE, teal // Outline mode and color
        );

    WorldImage cardImage;
    if (this.faceUp) {
      // Set suit color based on the suit
      Color suitColor = null; // Initialize to ensure it's always defined
      switch (this.suit) {
        case "♣":
          suitColor = clubColor;
          break;
        case "♦":
          suitColor = diamondColor;
          break;
        case "♥":
          suitColor = heartColor;
          break;
        case "♠":
          suitColor = spadeColor;
          break;
        default:
          suitColor = Color.BLACK; // Fallback color
          break;
      }

      // Front of the card with light purple background
      cardImage = new OverlayImage(
          new TextImage(rankToSymbol() + this.suit, 20, suitColor),
          new RectangleImage(
              cardWidth, cardHeight,
              OutlineMode.SOLID, beige // Light purple front background
              )
          );
    } else {
      // Back of the card with diamond design and rectangle around it
      cardImage = new OverlayImage(
          new OverlayImage(diamondOutline, diamondBorder), // Add the diamond border
          new RectangleImage(
              cardWidth, cardHeight,
              OutlineMode.SOLID, beige // Light purple for the back design
              )
          );
    }

    // Combine the outline and the card image
    return new OverlayImage(cardImage, outline);
  }

  // Converts the card's rank to a string symbol (e.g., "A", "J", "Q", "K", or a number)
  public String rankToSymbol() {
    if (this.rank == 1) {
      return "A";
    } else if (this.rank == 11) {
      return "J";
    } else if (this.rank == 12) {
      return "Q";
    } else if (this.rank == 13) {
      return "K";
    } else {
      return Integer.toString(this.rank);
    }
  }

}


class ExamplesConcentration {
  Concentration game;

  // Initialize a game instance before each test
  void initGame() {
    game = new Concentration();
  }

  // Test for shuffle
  void testShuffle(Tester t) {
    initGame();

    ArrayList<Card> deckBeforeShuffle = new ArrayList<>();
    for (ArrayList<Card> row : game.board) {
      deckBeforeShuffle.addAll(row);
    }

    ArrayList<Card> deckAfterShuffle = new ArrayList<>(deckBeforeShuffle);
    game.shuffle(deckAfterShuffle);

    // Check that the deck has the same cards after shuffle
    t.checkExpect(deckBeforeShuffle.size(), deckAfterShuffle.size());

    // Verify that some cards have moved by checking if the order has changed
    boolean orderChanged = false;
    for (int i = 0; i < deckBeforeShuffle.size(); i++) {
      if (!deckBeforeShuffle.get(i).equals(deckAfterShuffle.get(i))) {
        orderChanged = true;
        break;
      }
    }
    t.checkExpect(orderChanged, true);
  }

  // Test for flipping cards
  void testFlip(Tester t) {
    initGame();

    // Flip a card
    Card card = game.board.get(0).get(0);
    t.checkExpect(card.faceUp, false);

    card.flip();
    t.checkExpect(card.faceUp, true);

    card.flip();
    t.checkExpect(card.faceUp, false);
  }

  // Test for reset functionality
  void testReset(Tester t) {
    initGame();

    // Flip some cards and change the score
    game.board.get(0).get(0).flip();
    game.board.get(1).get(1).flip();
    game.score = 10;

    game.onKeyEvent("r"); // Reset the game

    // After reset, all cards should be face-down and score should be 26
    t.checkExpect(game.score, 26);
    for (ArrayList<Card> row : game.board) {
      for (Card card : row) {
        t.checkExpect(card.faceUp, false);
      }
    }
  }

  // Test for clicking cards
  void testClickingCards(Tester t) {
    initGame();

    // Simulate clicking on the first card
    Card firstCard = game.board.get(0).get(0);
    game.onMouseClicked(new Posn(10, 10));
    t.checkExpect(firstCard.faceUp, true);

    // Simulate clicking on a second card
    Card secondCard = game.board.get(0).get(1);
    game.onMouseClicked(new Posn(60, 10)); // Adjust positions based on the card dimensions
    t.checkExpect(secondCard.faceUp, true);

    // Check if both cards are added to flippedCards
    t.checkExpect(game.flippedCards.size(), 2);

    // Check if cards match or are scheduled to flip back
    if (firstCard.rank == secondCard.rank && firstCard.suit.equals(secondCard.suit)) {
      t.checkExpect(game.flippedCards.size(), 0);  // Matching cards stay face-up
      t.checkExpect(game.score, 25);               // Score decreases
    } else {
      t.checkExpect(game.flipBackCounter > 0, true); // Non-matching cards flip back
    }
  }

  // Test for isRed method
  void testIsRed(Tester t) {
    Card redHeart = new Card(5, "♥");
    Card redDiamond = new Card(8, "♦");
    Card blackSpade = new Card(7, "♠");
    Card blackClub = new Card(10, "♣");

    // Check if red suits return true for isRed
    t.checkExpect(redHeart.isRed(), true);
    t.checkExpect(redDiamond.isRed(), true);

    // Check if black suits return false for isRed
    t.checkExpect(blackSpade.isRed(), false);
    t.checkExpect(blackClub.isRed(), false);
  }

  // Test for isBlack method
  void testIsBlack(Tester t) {
    Card redHeart = new Card(5, "♥");
    Card redDiamond = new Card(8, "♦");
    Card blackSpade = new Card(7, "♠");
    Card blackClub = new Card(10, "♣");

    // Check if black suits return true for isBlack
    t.checkExpect(blackSpade.isBlack(), true);
    t.checkExpect(blackClub.isBlack(), true);

    // Check if red suits return false for isBlack
    t.checkExpect(redHeart.isBlack(), false);
    t.checkExpect(redDiamond.isBlack(), false);
  }

  // Test for equals method
  void testEquals(Tester t) {
    Card redHeart5 = new Card(5, "♥");
    Card redHeart8 = new Card(8, "♥");
    Card redDiamond5 = new Card(5, "♦");
    Card blackSpade5 = new Card(5, "♠");
    Card blackSpade10 = new Card(10, "♠");

    // Cards with the same rank and same color should be equal
    t.checkExpect(redHeart5.equals(redHeart5), true); // Same card
    t.checkExpect(redHeart5.equals(redDiamond5), true); // Same rank, same color (red)

    // Cards with the same rank but different colors should not be equal
    t.checkExpect(redHeart5.equals(blackSpade5), false); // Same rank, different colors

    // Cards with different ranks should not be equal, even if colors match
    t.checkExpect(redHeart5.equals(redHeart8), false); // Different rank, same color

    // Cards with different ranks and different colors should not be equal
    t.checkExpect(redHeart5.equals(blackSpade10), false); // Different rank, different color
  }


  // Test onTick for flipping cards back if they don’t match
  void testOnTickFlipBack(Tester t) {
    initGame();

    // Simulate flipping two non-matching cards
    Card firstCard = game.board.get(0).get(0);
    Card secondCard = game.board.get(0).get(1);
    firstCard.flip();
    secondCard.flip();
    game.flippedCards.add(firstCard);
    game.flippedCards.add(secondCard);
    game.flipBackCounter = 1; // Set counter to 1 to simulate delay

    // Call onTick to process flip-back
    game.onTick();
    t.checkExpect(firstCard.faceUp, false);
    t.checkExpect(secondCard.faceUp, false);
    t.checkExpect(game.flippedCards.size(), 0);
  }

  //Run the bigBang method to launch the game
  void testBigBang(Tester t) {
    Concentration world = new Concentration();
    int worldWidth = 700; // Adjust width to match the scene dimensions in makeScene
    int worldHeight = 400;
    double tickRate = 0.1; // Adjust for real-time gameplay experience
    world.bigBang(worldWidth, worldHeight, tickRate);
  }
}


