/**
 * Created by waleed on 4/20/15.
 */
public class Player extends Entity {

    public String file;
    public PhysicsBody body;
    public boolean canJump = false;

    public Player(String filename, int xx, int yy) {
        super(filename, xx, yy);
        setSize(getXsize()*2, getYsize()*2);
        body = new PhysicsBody(this, getXsize()/10, 0, getXsize() - getXsize()/5, getYsize());
    }
}