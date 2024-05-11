import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

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
        System.out.println("monster: " + monster);
        if (monster.equals(rogue)) {
            return monster;  // 如果怪物已经在游荡者位置上，直接返回当前位置
        }

        // 使用BFS找到最短路径
        boolean[][] visited = new boolean[N][N];
        Queue<Site> queue = new LinkedList<>();
        Map<String, Site> cameFrom = new HashMap<>();

        queue.add(monster);
        visited[monster.i()][monster.j()] = true;

        while (!queue.isEmpty()) {
            Site current = queue.poll();

            // 检查八个方向的邻居
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int newX = current.i() + dx;
                    int newY = current.j() + dy;
                    Site newSite = new Site(newX, newY);

                    if (newX >= 0 && newX < N && newY >= 0 && newY < N && !visited[newX][newY] && dungeon.isLegalMove(current, newSite)) {
                        queue.add(newSite);
                        visited[newX][newY] = true;
                        cameFrom.put(siteToKey(newSite), current);
                        if (newSite.equals(rogue)) {  // 如果到达游荡者位置，立即停止搜索
                            queue.clear();
                            break;
                        }
                    }
                }
            }
        }

        // 从游荡者位置回溯到最佳初始移动位置
        String stepKey = siteToKey(rogue);
        Site move = monster;  // 默认移动为游荡者位置
        while (cameFrom.containsKey(stepKey) && !cameFrom.get(stepKey).equals(monster)) {
            move = cameFrom.get(stepKey);
            stepKey = siteToKey(move);
        }


        System.out.println("next: " + move);
        if (move.equals(monster)){
            System.out.println("no path found!");
        }
        return move;
    }

    private String siteToKey(Site site) {
        return site.i() + "_" + site.j();
    }
}
