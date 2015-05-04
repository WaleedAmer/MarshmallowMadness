/**
 * Created by waleed on 4/20/15.
 */

public class Enemy extends Entity {

    public String file;
    public PhysicsBody body;
    public boolean canJump = false;
    public Player player;

    public Enemy(String filename, int xx, int yy) {
        super(filename, xx, yy);
        setSize(getXsize()*2, getYsize()*2);
        body = new PhysicsBody(this, getXsize()/3, 0, getXsize()/3, getYsize());
    }

    public void update(int tick) {

        x = getXposition();
        y = getYposition();

        if (player.getXposition() > x + 32) {
            xSpeed = 4;
        }
        else {
            xSpeed = 0;
        }

        if (!game.placeFree(this, 32, 0) && ySpeed == 0) {
            ySpeed = 8;
        }

    }
}