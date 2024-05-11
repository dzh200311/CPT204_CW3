import java.util.*;

public class Rogue extends Role{
    private Game game;
    private Dungeon dungeon;
    private int N;

    private int loopIndex = 0; //循环路径的指针，为了标记要返回的下一个移动的坐标

    private boolean isInLoop = false;

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
        Site[] loop = findLoop();
        // 还没写好，要测试monster就把这些注释起来
        /*// 接近怪物或未找到回路，尝试远离怪物
        if (loop == null || rogue.manhattanTo(monster) <= 1) {
            rogue = pathFind(monster, rogue);
            isInLoop = false;
        } else {
            //是否在循环的入口或者已经在循环内
            if (rogue.equals(loop[loopIndex]) || isInLoop) {
                rogue = loop[loopIndex];
                isInLoop = true;
                loopIndex = (loopIndex + 1) % loop.length; //指针指向下一个位置
            } else {
                rogue = pathFind(loop[loopIndex], rogue);
                isInLoop = false;
            }
        }*/
        return rogue;
    }

    private Site[] findLoop() {
        boolean[] visited = new boolean[N * N]; // 假设地牢可以表示为一维数组
        List<Site> path = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (!visited[i * N + j] && dfs(i, j, -1, -1, visited, path)) {
                    return path.toArray(new Site[0]);
                }
            }
        }
        return null;
    }

    private boolean dfs(int x, int y, int px, int py, boolean[] visited, List<Site> path) {
        if (visited[x * N + y]) {
            return true; // 发现环
        }
        visited[x * N + y] = true;
        path.add(new Site(x, y));

        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}}; // 上下左右移动
        for (int[] d : directions) {
            int nx = x + d[0], ny = y + d[1];
            if (nx >= 0 && nx < N && ny >= 0 && ny < N && !(nx == px && ny == py)) { // 检查是否在边界内并排除父节点
                if (dfs(nx, ny, x, y, visited, path)) {
                    return true;
                }
            }
        }

        path.remove(path.size() - 1);
        visited[x * N + y] = false;
        return false;
    }


    private Site pathFind(Site target, Site rogue) {
        Queue<Site> queue = new LinkedList<>();
        Map<Site, Site> prev = new HashMap<>(); // 存储路径
        queue.add(rogue);
        prev.put(rogue, null);
        while (!queue.isEmpty()) {
            Site current = queue.poll();
            if (current.equals(target)) {
                return reconstructPath(prev, target);
            }

            int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}}; // 上下左右移动
            for (int[] d : directions) {
                Site next = new Site(current.i() + d[0], current.j() + d[1]);
                if (dungeon.isLegalMove(current,next) && !prev.containsKey(next)) { // 检查有效性和是否已访问
                    queue.add(next);
                    prev.put(next, current);
                }
            }
        }
        return null; // 如果没有找到路径
    }

    private Site reconstructPath(Map<Site, Site> prev, Site target) {
        Site step = target;
        Site nextMove = null;
        while (prev.containsKey(step)) {
            nextMove = step;
            step = prev.get(step);
        }
        return nextMove; // 返回从起点到达终点的第一步
    }


}

