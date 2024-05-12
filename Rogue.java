import java.util.*;

public class Rogue extends Role{
    private Game game;
    private Dungeon dungeon;
    private int N;

    private Site previousSite = null;
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

        Site nearCorridor = findCorridor(rogue);
        if (nearCorridor == null || rogue.manhattanTo(monster) <= 1) {
            // 如果没找到走廊或怪物太近，尝试逃跑
            System.out.println("run");
            rogue = escape(monster, rogue);
        } else {
            // 如果在走廊中，往下一个走廊走（即寻路至走廊另一端的那个的出口，即另一端走廊与房间的连接处）
            if (dungeon.isCorridor(rogue)) {

                rogue = alongLoop(rogue, previousSite);
            } else {
                // 如果不在loop入口，寻路至入口
                rogue = pathFind(nearCorridor, rogue);
            }
        }

        // 更新previousSite为这次移动之前的位置
        previousSite = tempPrevious;

        return rogue;
    }


    private Site findCorridor(Site start) {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // 顺序是右，下，左，上
        int step = 1; // 初始步长
        int x = start.i();
        int y = start.j();

        if (dungeon.isCorridor(start)) {
            return start; // 如果起点已是走廊，则直接返回
        }

        while (true) {
            for (int i = 0; i < directions.length; i++) {
                for (int j = 0; j < step; j++) {
                    x += directions[i][0];
                    y += directions[i][1];

                    if (x >= 0 && x < N && y >= 0 && y < N) { // 确保坐标在地图范围内
                        Site current = new Site(x, y);
                        if (dungeon.isCorridor(current)) {
                            System.out.println("corridor found " + current);
                            return current; // 找到走廊
                        }
                    }
                }
                if (i % 2 == 1) { // 在每完成一对方向（左右或上下）后增加步长
                    step++;
                }
            }
        }
    }

    private Site alongLoop(Site current, Site previous) {
        //TODO: 逻辑不对
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        Site nextStep = null;
        System.out.println("previous: " + previous + "current: " + current);
        for (int[] dir : directions) {
            int ni = current.i() + dir[0];
            int nj = current.j() + dir[1];
            Site neighbor = new Site(ni, nj);

            if (ni >= 0 && ni < N && nj >= 0 && nj < N && !neighbor.equals(previous)) {
                if (dungeon.isCorridor(neighbor)) {
                    nextStep = neighbor;
                    break;
                }
            }
        }
        return nextStep != null ? nextStep : current;
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
        //TODO: 检测是否是corridor，是的话就不能斜向走
        int dx = monster.i() - rogue.i();
        int dy = monster.j() - rogue.j();

        int dirX = Integer.compare(dx, 0);
        int dirY = Integer.compare(dy, 0);

        Site bestMove = null;
        int maxDist = -1;

        // Check all surrounding cells in a 3x3 grid centered on the rogue
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                if (dungeon.isCorridor(rogue) && Math.abs(i) == 1 && Math.abs(j) == 1) {
                    continue;
                }
                // Compute the potential move coordinates
                int newX = rogue.i() + i;
                int newY = rogue.j() + j;
                Site newSite = new Site(newX, newY);

                // Calculate the Manhattan distance from the new site to the monster
                int dist = newSite.manhattanTo(monster);

                // Check if moving to this site increases the distance to the monster and is not a wall
                if ((i != dirX || j != dirY) && !dungeon.isWall(newSite) && dist > maxDist) {
                    maxDist = dist;
                    bestMove = newSite;
                }
            }
        }

        // Return the best move found; if no move found, return the original position
        return bestMove != null ? bestMove : rogue;
    }





}

