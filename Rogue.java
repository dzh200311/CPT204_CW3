import java.util.*;

public class Rogue extends Role{
    private Game game;
    private Dungeon dungeon;
    private int N;

    private LinkedList<Site> cycle = null;

    ArrayList<Site> foundCorridor = new ArrayList<>();
    public Rogue(Game game) {
        super(game);
        this.game    = game;
        this.dungeon = game.getDungeon();
        this.N       = dungeon.size();
    }
    public Site move() {
        Site monster = game.getMonsterSite();
        Site rogue = game.getRogueSite();

        // Record the current position as the previous position first
        Site tempPrevious = rogue;
        System.out.println("rouge now: " + rogue);
        if (cycle == null){
            System.out.println("cycle finding...");
            cycle = findCycle(rogue);
        }
        if (cycle == null || rogue.manhattanTo(monster) <= 1) {
            // If you don't find the ring or the monster is too close, try to run away.
            System.out.println("run");
            rogue = escape(monster, rogue);

            if (cycle != null && tempPrevious.manhattanTo(cycle.getLast()) == 1 && dungeon.isRoom(tempPrevious)){
                System.out.println("yes");
                rogue = cycle.getLast();
            }

        } else {
            // If in the cycle, go down the next corridor (i.e., find your way to the exit of the one at the other end of the corridor, where the corridor at the other end connects to the room).
            if (rogue.equals(cycle.getLast())) {
                rogue = alongLoop(rogue, cycle);
            } else {
                // If not at the ring entrance, find your way to the entrance.
                rogue = pathFind(cycle.getLast(), rogue);
            }
        }

        System.out.println("rouge next: " + rogue);
        return rogue;
    }


    private Site alongLoop(Site start, Queue<Site> cycle) {
        System.out.println("rouge walk along loop");
        if (cycle == null || cycle.isEmpty()) return start;

        Site nextStep = cycle.poll(); // Remove elements from the head of the queue
        cycle.offer(nextStep); // Re-add the element to the tail of the queue, maintaining the ring structure

        return nextStep;
    }

    private LinkedList<Site> findCycle(Site start) {
        Site corridorStart = findCorridor(start);
        foundCorridor.add(corridorStart);

        // Using while loops to find valid corridors and rings
        while (corridorStart != null) {
            System.out.println("entry found");
            LinkedList<Site> cycle = cycleConstruct(corridorStart);

            // If the cycle is found, return the cycle
            if (cycle != null) {
                return cycle;
            }

            // No rings found. Keep looking for the next corridor.
            System.out.println("current corridor is not cycle. look for another");
            corridorStart = findCorridor(start);
            foundCorridor.add(corridorStart);
        }

        // Returns null if no corridor or cycle was found.
        System.out.println("No cycle. run.");
        return null;
    }



    private LinkedList<Site> cycleConstruct(Site corridorStart){
        HashSet<Site> visited = new HashSet<>();
        // Used to trace the path to each point
        Map<Site, Site> path = new HashMap<>();

        visited.add(corridorStart);
        Stack<Site> stack = new Stack<>();
        stack.push(corridorStart);
        Site previous = null;
        int iter = 0;
        while (!stack.isEmpty()){
            Site current = stack.pop();
            iter++;
            for (int i = 1; i >= -1; i--) {
                for (int j = 1; j >= -1; j--) {
                    Site next = new Site(current.i()+i, current.j()+j);
                    if (dungeon.isRoom(next)){
                        if((i==0 && j==0))continue;
                    }
                    if (dungeon.isCorridor(next)){
                        if ((Math.abs(i) == 1 && Math.abs(j) == 1) || (i==0 && j==0)) {
                            continue;
                        }
                    }

                    if ((next.i() >= N || next.j() >=N) || (previous != null && next.equals(previous))){
                        continue;
                    }
                    if (previous == null && dungeon.isRoom(next)) continue;
                    if (neighborIsRoom(current,iter,path) && dungeon.isRoom(next)) continue;
                    if(visited.contains(next)&&dungeon.isRoom(next))continue;

                    if (next.equals(corridorStart)){
                        LinkedList<Site> cycle = new LinkedList<>();
                        Site temp = current;
                        cycle.push(corridorStart);
                        while (temp != corridorStart ) {
                            cycle.addFirst(temp);
                            temp = path.get(temp);
                        }
                        System.out.println("cycle found:" + cycle);
                        return cycle;
                    }
                    if (!visited.contains(next) && dungeon.isLegalMove(current,next)){
                        visited.add(next);
                        stack.push(next);
                        path.put(next, current);
                    }
                    if (iter == 1 && stack.size() == 2 ){
                        stack.pop();
                        visited.remove(next);
                    }
                }
            }
            previous = current;
        }
        return null;
    }

    private boolean neighborIsRoom(Site site,int iter,Map<Site, Site> path){
        int num = path.size() > 40 ? 12 : 6;
        if (iter <= num){
            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0},{-1,-1},{-1,1},{1,1},{1,-1}};
            for(int[] dir : directions){
                Site neighbor = new Site(site.i()+dir[0], site.j()+dir[1]);
                if (dungeon.isRoom(neighbor)){
                    return true;
                }
            }
        }
        return false;
    }

    private Site findCorridor(Site start) {
        // Direction array: right, down, left, up
        int[][] directions = {{1, 0}, {0, 1}, {-1,0}, {0, -1}};
        Queue<Site> queue = new LinkedList<>();
        Set<Site> visited = new HashSet<>(); // Used to prevent repeat visits

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Site current = queue.poll();

            // Check if the current position is an entrance
            if (dungeon.isCorridor(current) && !foundCorridor.contains(current)) {
                int roomCount = 0;
                List<Site> adjacentSites = new ArrayList<>();

                for (int[] direction : directions) {
                    int adjacentX = current.i() + direction[0];
                    int adjacentY = current.j() + direction[1];
                    if (adjacentX >= 0 && adjacentX < N && adjacentY >= 0 && adjacentY < N) {
                        Site adjacent = new Site(adjacentX, adjacentY);
                        adjacentSites.add(adjacent);
                        if (dungeon.isRoom(adjacent) && dungeon.isLegalMove(current, adjacent)) {
                            roomCount++;
                        }
                    }
                }

                // Check if exactly one adjacent site is a room, and check whether current entry is closer to monster
                if (roomCount >= 1 && ((current.manhattanTo(game.getMonsterSite()) >= 3 && current.manhattanTo(game.getRogueSite()) <=3 )|| current.manhattanTo(game.getMonsterSite()) >= 5)) {
                    return current; // This is a valid connection point
                }
            }

            // Explore all four directions
            for (int[] direction : directions) {
                int newX = current.i() + direction[0];
                int newY = current.j() + direction[1];

                if (newX >= 0 && newX < N && newY >= 0 && newY < N) { // Make sure the coordinates are within the map
                    Site next = new Site(newX, newY);
                    if (!visited.contains(next)&& dungeon.isLegalMove(current,next)) {
                        queue.offer(next);
                        visited.add(next); // Marked as visited
                    }
                }
            }
        }

        return null; // Returns null if no corridor is found
    }


    private Site pathFind(Site target, Site start) {

        //Targeting the entrance to the loop, bfs pathfinding
        if (start.equals(target)){
            return  start;
        }
        boolean[][] visited = new boolean[N][N];
        Queue<Site> queue = new LinkedList<>();
        Map<String, Site> cameFrom = new HashMap<>();
        boolean pathFound = false;

        queue.add(start);
        visited[start.i()][start.j()] = true;
        while (!queue.isEmpty()){
            Site current = queue.poll();

            for (int i = -1; i <= 1 ; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;

                    int newI = current.i() + i;
                    int newJ = current.j() + j;
                    Site newSite = new Site(newI,newJ);
                    if (dungeon.isCorridor(newSite) && Math.abs(i) == 1 && Math.abs(j) == 1) {
                        continue;
                    }
                    if (newI >= 0 && newI < N && newJ >= 0 && newJ < N && !visited[newI][newJ]&& dungeon.isLegalMove(current, newSite)){
                        queue.add(newSite);
                        visited[newI][newJ] = true;
                        cameFrom.put(util.siteToKey(newSite), current);
                        if (newSite.equals(target)) {
                            queue.clear();
                            pathFound = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!pathFound){
            System.out.println("rogue no path found!");
            return start;
        }
        // Backtracking from wanderer position to optimal initial movement position
        String stepKey = util.siteToKey(target);
        Site move = target;  // Default move to wanderer position
        while (cameFrom.containsKey(stepKey) && !cameFrom.get(stepKey).equals(start)) {
            move = cameFrom.get(stepKey);
            stepKey = util.siteToKey(move);
        }

        return move;
    }



    private Site escape(Site monster, Site rogue) {
        int dx = monster.i() - rogue.i();
        int dy = monster.j() - rogue.j();

        int dirX = Integer.compare(dx, 0);
        int dirY = Integer.compare(dy, 0);

        Site bestMove = null;
        int maxDist = -1;
        List<Site> validMoves = new ArrayList<>();  // List to store all valid moves

        // Check all surrounding cells in a 3x3 grid centered on the rogue
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newX = rogue.i() + i;
                int newY = rogue.j() + j;
                Site newSite = new Site(newX, newY);
                // Continue if the move is diagonal and in a corridor, which is not allowed
                if(i == 0 && j == 0)continue;
                if (dungeon.isCorridor(newSite) && Math.abs(i) == 1 && Math.abs(j) == 1) {
                    continue;
                }

                // Calculate the Manhattan distance from the new site to the monster
                int dist = newSite.manhattanTo(monster);

                // Check if the site is a legal move and not the current position
                if (dungeon.isLegalMove(rogue, newSite)) {
                    validMoves.add(newSite);  // Add to valid moves list

                    // Check if moving to this site increases the distance to the monster
                    if ((i != dirX || j != dirY) && dist > maxDist) {
                        maxDist = dist;
                        bestMove = newSite;
                    }
                }
            }
        }

        // If a best move was found, return it; otherwise, pick a random valid move
        if (bestMove != null) {
            return bestMove;
        } else if (!validMoves.isEmpty()) {
            Random rand = new Random();
            return validMoves.get(rand.nextInt(validMoves.size()));
        } else {
            return rogue;  // If no valid moves are found, return the original position
        }
    }






}

