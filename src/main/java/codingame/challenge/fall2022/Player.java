package codingame.challenge.fall2022;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
  int width;

  int height;

  int myMatter;

  int oppMatter;

  List<Tile> tiles = new ArrayList<>();

  public static void main(String args[]) {
    Player player = new Player();
    Scanner in = new Scanner(System.in);
    player.width = in.nextInt();
    player.height = in.nextInt();

    // game loop
    while (true) {
      player.myMatter = in.nextInt();
      player.oppMatter = in.nextInt();
      player.tiles.clear();
      for (int y = 0; y < player.height; y++) {
        for (int x = 0; x < player.width; x++) {
          player.addTile(in, y, x);
        }
      }
      System.out.println(player.action());
    }
  }

  void addTile(Scanner in, int y, int x) {
    tiles.add(new Tile(in, x, y));
  }

  class Tile {
    int x, y;

    int scrapAmount;

    int owner; // 1 = me, 0 = foe, -1 = neutral

    int units;

    int recycler;

    int canBuild;

    int canSpawn;

    int inRangeOfRecycler;

    public Tile(Scanner in, int x, int y) {
      this.x = x;
      this.y = y;
      scrapAmount = in.nextInt();
      owner = in.nextInt();
      units = in.nextInt();
      recycler = in.nextInt();
      canBuild = in.nextInt();
      canSpawn = in.nextInt();
      inRangeOfRecycler = in.nextInt();
    }

    int dist(Tile t) {
      return abs(t.x - x) + abs(t.y - y);
    }

    boolean isMine() {
      return owner == 1;
    }

    boolean isNeutral() {
      return owner == -1;
    }

    boolean isRecycler() {
      return recycler == 1;
    }

    int getScrapAmount() {
      return scrapAmount;
    }

    int getRecyclableScrap() {
      return tiles.stream().filter(t -> dist(t) <= 1).mapToInt(Tile::getScrapAmount).sum();
    }

    int distToOpponent() {
      return tiles.stream().filter(t -> !t.isMine() && !t.isNeutral())
        .mapToInt(this::dist)
        .min()
        .orElse(150);
    }

    Tile bestTileToMove() {
      return tiles.stream().filter(t -> !t.isMine() && !t.isNeutral())
        .min(Comparator.comparingInt(this::dist))
        .orElse(this);
    }

  }

  String action() {
    long myRecyclerCount = tiles.stream().filter(t -> t.isRecycler() && t.isMine()).count();
    int totalAvailableScrap = tiles.stream().filter(t -> t.isNeutral() || t.isMine())
      .mapToInt(Tile::getScrapAmount)
      .sum();
    System.err.println("myRecyclerCount : " + myRecyclerCount);
    System.err.println("totalAvailableScrap : " + totalAvailableScrap);
    List<String> instructions = new ArrayList<>();
    instructions.add(addRecycler(myRecyclerCount, totalAvailableScrap));
    instructions.add(addRecycler(myRecyclerCount, totalAvailableScrap));
    while (myMatter > 10) {
      myMatter -= 10;
      Tile bestTile = tiles.stream().filter(Tile::isMine)
        .min(Comparator.comparingInt(Tile::distToOpponent))
        .orElse(null);
      if (bestTile != null) {
        instructions.add("SPAWN 1 " + bestTile.x + " " + bestTile.y);
      }
    }

    tiles.stream().filter(t -> t.isMine() && t.units > 0).forEach(t -> {
      Tile toMove = t.bestTileToMove();
      if (!toMove.equals(t)) {
        int nb;
        if (t.units > 5) {
          nb = t.units - 2;
        } else if (t.units == 1) {
          nb = 1;
        } else {
          nb = t.units - 1;
        }
        instructions.add("MOVE " + nb + " " + t.x + " " + t.y + " " + toMove.x + " " + toMove.y);
      }
    });

    if (!instructions.isEmpty()) {
      return instructions.stream().filter(Objects::nonNull).collect(Collectors.joining(";"));
    }
    return "WAIT";
  }

  private String addRecycler(long myRecyclerCount, int totalAvailableScrap) {
    if (myRecyclerCount < 2 && totalAvailableScrap >= 10 && myMatter >= 10) {
      Tile bestTile = tiles.stream().filter(Tile::isMine)
        .max(Comparator.comparingInt(Tile::getRecyclableScrap))
        .orElse(null);
      if (bestTile != null) {
        bestTile.recycler = 1;
        myMatter -= 10;
        return "BUILD " + bestTile.x + " " + bestTile.y;
      }
    }
    return null;
  }
}