/**
 * Created by waleed on 4/20/15.
 */
public class Block extends Entity {

    public String file;
    public PhysicsBody body;

    public Block(String filename, int xx, int yy) {
        super(filename, xx, yy);
        this.setSize(64, 64);
        this.body = new PhysicsBody(this);
    }
}

