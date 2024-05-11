public abstract class Role implements Movable {
    protected Game game;
    protected Dungeon dungeon;
    protected int dungeonSize;

    public Role(Game game) {
        this.game = game;
        this.dungeon = game.getDungeon();
        this.dungeonSize = dungeon.size();
    }

    public abstract Site move();
}
