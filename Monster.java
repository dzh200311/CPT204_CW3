import java.util.*;

public class Monster extends Role{
    private Game game;
    private Dungeon dungeon;
    private int N;

    public Monster(Game game){
        super(game);
        this.game    = game;
        this.dungeon = game.getDungeon();
        this.N       = dungeon.size();
    }


    public Site move() {
        Site monster = game.getMonsterSite();
        Site rogue = game.getRogueSite();
        System.out.println("monster site: " + monster);
        if (monster.equals(rogue)) {
            return monster;  // If the monster is already in the rogue's position, return directly to the current position
        }

        // Finding the shortest path using BFS
        boolean[][] visited = new boolean[N][N];
        Queue<Site> queue = new LinkedList<>();
        Map<String, Site> cameFrom = new HashMap<>();
        boolean pathFound = false;

        queue.add(monster);
        visited[monster.i()][monster.j()] = true;

        while (!queue.isEmpty()) {
            Site current = queue.poll();

            // Check the neighbors in eight directions
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;
                    int newX = current.i() + i;
                    int newY = current.j() + j;
                    Site newSite = new Site(newX, newY);
                    if (dungeon.isCorridor(newSite) && Math.abs(i) == 1 && Math.abs(j) == 1) {
                        continue;
                    }
                    if (newX >= 0 && newX < N && newY >= 0 && newY < N && !visited[newX][newY] && dungeon.isLegalMove(current, newSite)) {
                        queue.add(newSite);
                        visited[newX][newY] = true;
                        cameFrom.put(util.siteToKey(newSite), current);
                        if (newSite.equals(rogue)) {  // Stop the search immediately if you reach the wanderer's location.
                            queue.clear();
                            pathFound = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!pathFound){
            System.out.println("no path found!");
            return monster;
        }
        // Backtracking from rogue position to optimal initial movement position
        String stepKey = util.siteToKey(rogue);
        Site move = rogue;  // initial is the rogue
        while (cameFrom.containsKey(stepKey) && !cameFrom.get(stepKey).equals(monster)) {
            move = cameFrom.get(stepKey);
            stepKey = util.siteToKey(move);
        }

        System.out.println("next monster site: " + move);

        return move;
    }


}
