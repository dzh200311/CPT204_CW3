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
            return monster;  // 如果怪物已经在游荡者位置上，直接返回当前位置
        }

        // 使用BFS找到最短路径
        boolean[][] visited = new boolean[N][N];
        Queue<Site> queue = new LinkedList<>();
        //PriorityQueue<Site> queue = new PriorityQueue<>(Comparator.comparingInt(s -> s.manhattanTo(rogue)));
        Map<String, Site> cameFrom = new HashMap<>();
        boolean pathFound = false;

        queue.add(monster);
        visited[monster.i()][monster.j()] = true;

        while (!queue.isEmpty()) {
            Site current = queue.poll();

            // 检查八个方向的邻居
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;
                    int newX = current.i() + i;
                    int newY = current.j() + j;
                    Site newSite = new Site(newX, newY);

                    if (newX >= 0 && newX < N && newY >= 0 && newY < N && !visited[newX][newY] && dungeon.isLegalMove(current, newSite)) {
                        queue.add(newSite);
                        visited[newX][newY] = true;
                        cameFrom.put(util.siteToKey(newSite), current);
                        if (newSite.equals(rogue)) {  // 如果到达游荡者位置，立即停止搜索
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
        // 从游荡者位置回溯到最佳初始移动位置
        String stepKey = util.siteToKey(rogue);
        Site move = rogue;  // 默认移动为游荡者位置
        while (cameFrom.containsKey(stepKey) && !cameFrom.get(stepKey).equals(monster)) {
            move = cameFrom.get(stepKey);
            stepKey = util.siteToKey(move);
        }

        System.out.println("next monster site: " + move);
        return move;
    }


}
