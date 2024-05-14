import java.util.*;

public class Rogue extends Role{
    private Game game;
    private Dungeon dungeon;
    private int N;

    private Site previousSite = null;
    private LinkedList<Site> cycle = null;

    ArrayList<Site> foundCorridor = new ArrayList<>();
    public Rogue(Game game) {
        super(game);
        this.game    = game;
        this.dungeon = game.getDungeon();
        this.N       = dungeon.size();
    }

    // TAKE A RANDOM LEGAL MOVE
    // YOUR MAIN TASK IS TO RE-IMPLEMENT THIS METHOD TO DO SOMETHING INTELLIGENT
    public Site move() {
        Site monster = game.getMonsterSite();
        Site rogue = game.getRogueSite();

        // 先记录当前位置为之前的位置
        Site tempPrevious = rogue;
        System.out.println("rouge now: " + rogue);
        if (cycle == null){
            cycle = findCycle(rogue);
        }
        if (cycle == null || rogue.manhattanTo(monster) <= 1) {
            // 如果没找到环或怪物太近，尝试逃跑
            System.out.println("run");
            rogue = escape(monster, rogue);
        } else {
            // 如果在环中，往下一个走廊走（即寻路至走廊另一端的那个的出口，即另一端走廊与房间的连接处）
            if (rogue.equals(cycle.getLast())) {
                rogue = alongLoop(rogue, cycle);
            } else {
                // 如果不在环入口，寻路至入口
                rogue = pathFind(cycle.get(0), rogue);
            }
        }

        // 更新previousSite为这次移动之前的位置
        previousSite = tempPrevious;
        System.out.println("rouge next: " + rogue);
        return rogue;
    }


    private Site alongLoop(Site start, Queue<Site> cycle) {
        System.out.println("rouge walk along loop");
        if (cycle == null || cycle.isEmpty()) return start;

        Site nextStep = cycle.poll(); // 从队列头部移除元素
        cycle.offer(nextStep); // 将该元素重新加入队列尾部，保持环状结构

        return nextStep;
    }

    private LinkedList<Site> findCycle(Site start){
        // 先寻找最近的走廊位置,若没有，则记录找过的这个走廊，不找他找下一个
        Site corridorStart = findCorridor(start);
        foundCorridor.add(corridorStart);
        if (corridorStart == null) {
            //没有走廊也没有环
            return null;
        }
       LinkedList<Site> cycle = dfs(corridorStart);
        if (corridorStart == null){
            //所找的走廊没有环，换一个
            corridorStart = findCorridor(start);
            foundCorridor.add(corridorStart);
            if (corridorStart == null) {
                //没有走廊也没有环
                return null;
            }
            cycle = dfs(corridorStart);
        }
        return cycle;
    }

    private LinkedList<Site> dfs(Site corridorStart){
        HashSet<Site> visited = new HashSet<>();
        // 用于追踪到达每个点的路径
        Map<Site, Site> path = new HashMap<>();

        visited.add(corridorStart);
        Stack<Site> stack = new Stack<>();
        stack.push(corridorStart);
        Site previous = null;

        while (!stack.isEmpty()){
            Site current = stack.pop();
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
                    if (previous == null && dungeon.isRoom(next))continue;

                    if(visited.contains(next)&&dungeon.isRoom(next))continue;

                    if (next.equals(corridorStart)){
                        LinkedList<Site> cycle = new LinkedList<>();
                        Site temp = current;
                        cycle.push(corridorStart);
                        while (temp != corridorStart ) {
                            cycle.addFirst(temp);
                            temp = path.get(temp);
                        }
                        System.out.println(cycle);
                        return cycle;
                    }
                    if (!visited.contains(next) && dungeon.isLegalMove(current,next)){
                        visited.add(next);
                        stack.push(next);
                        path.put(next, current);
                    }
                }
            }
            previous = current;
        }
        return null;
    }

    private Site findCorridor(Site start) {
        // 方向数组：右，下，左，上
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        Queue<Site> queue = new LinkedList<>();
        Set<Site> visited = new HashSet<>(); // 用于防止重复访问

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Site current = queue.poll();

            // 检查当前位置是否为走廊
            if (dungeon.isCorridor(current) && !foundCorridor.contains(current)) {
                int roomCount = 0;
                int wallCount = 0;
                List<Site> adjacentSites = new ArrayList<>();

                for (int[] direction : directions) {
                    int adjacentX = current.i() + direction[0];
                    int adjacentY = current.j() + direction[1];
                    if (adjacentX >= 0 && adjacentX < N && adjacentY >= 0 && adjacentY < N) {
                        Site adjacent = new Site(adjacentX, adjacentY);
                        adjacentSites.add(adjacent);
                        if (dungeon.isRoom(adjacent)) {
                            roomCount++;
                        } else if (!dungeon.isLegalMove(current, adjacent)) {
                            wallCount++;
                        }
                    } else {
                        wallCount++; // Treat out of bounds as walls
                    }
                }

                // Check if exactly one adjacent site is a room and two are walls
                if (roomCount == 1 && wallCount == 2) {
                    return current; // This is a valid connection point
                }
            }

            // 探索所有四个方向
            for (int[] direction : directions) {
                int newX = current.i() + direction[0];
                int newY = current.j() + direction[1];

                if (newX >= 0 && newX < N && newY >= 0 && newY < N) { // 确保坐标在地图范围内
                    Site next = new Site(newX, newY);
                    if (!visited.contains(next)&& dungeon.isLegalMove(current,next)) {
                        queue.offer(next);
                        visited.add(next); // 标记为已访问
                    }
                }
            }
        }

        return null; // 如果没有找到走廊，返回 null
    }


    private Site pathFind(Site target, Site start) {

        System.out.println("rogue current: " + start);
        //以loop的入口为目标，bfs寻路
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
        // 从游荡者位置回溯到最佳初始移动位置
        String stepKey = util.siteToKey(target);
        Site move = target;  // 默认移动为游荡者位置
        while (cameFrom.containsKey(stepKey) && !cameFrom.get(stepKey).equals(start)) {
            move = cameFrom.get(stepKey);
            stepKey = util.siteToKey(move);
        }

        System.out.println("rogue next: " + move);
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
                if (dungeon.isCorridor(newSite) && Math.abs(i) == 1 && Math.abs(j) == 1) {
                    continue;
                }

                // Calculate the Manhattan distance from the new site to the monster
                int dist = newSite.manhattanTo(monster);

                // Check if the site is a legal move and not the current position
                if (dungeon.isLegalMove(rogue, newSite) && (newX != rogue.i() || newY != rogue.j())) {
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

