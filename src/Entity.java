/**
 * Created by waleed on 4/20/15.
 */
public class Entity extends Sprite {

    public String file;
    public int x, y, xSpeed, ySpeed, width, height;
    public PhysicsBody body;
    public boolean doesUpdate;
    public String type = "";
    public Movie game;

    public Entity(String filename, int xx, int yy) {
        super(filename);
        setPosition(xx, yy);
        this.x = xx;
        this.y = yy;
        this.file = filename;
        width = getXsize();
        height = getYsize();
        body = new PhysicsBody(this);
        doesUpdate = false;
    }

    public PhysicsBody getBody() {
        return this.body;
    }

    public void setBody(PhysicsBody newbody) {
        body = newbody;
    }

    public Rect rectOfSelf() {
        return new Rect(x, y, getXsize(), getYsize());
    }

    public Rect destRect() {
        return new Rect(x + xSpeed, y + ySpeed, getXsize(), getYsize());
    }

    public Rect rectOfSelfWithOffset(int xx, int yy) {
        return new Rect(x+xx, y+yy, getXsize(), getYsize());
    }

    public void update(int tick) {
    }
}
