/**
 * Created by waleed on 4/20/15.
 */

public class Enemy extends Entity {

    public String file;
    public PhysicsBody body;
    public boolean canJump = false;

    public Enemy(String filename, int xx, int yy) {
        super(filename, xx, yy);
        setSize(getXsize()*2, getYsize()*2);
        body = new PhysicsBody(this, getXsize()/3, 0, getXsize() *1/3, getYsize());
    }

    public void update(int tick) {

        if (game.player.getXposition() > getXposition() + 64) {
            xSpeed = 4;
        }
        else if (game.player.getXposition() < getXposition() - 64) {
            xSpeed = -4;
        }
        else {
            xSpeed = 0;
        }

        if (game.placeFree(this, xSpeed, ySpeed)) {
            game.move(this, xSpeed, ySpeed);
        }
    }
}