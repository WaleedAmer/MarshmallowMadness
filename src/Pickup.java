/**
 * Created by waleed on 4/13/15.
 */
public class Pickup extends Entity {

    public String file;
    public PhysicsBody body;
    public boolean collected = false;

    public Pickup(String filename, int xx, int yy) {
        super(filename, xx, yy);
        this.setSize(getXsize() * 2, getYsize() * 2);
        this.body = new PhysicsBody(this, getXsize()/4, getYsize()/4, this.getXsize()/2, this.getYsize()/2);
    }

    // Create a pickup and place it on an entity
    public Pickup(String filename, Entity entity) {
        super(filename, entity.getXposition(), entity.getYposition() + entity.getYsize());
        this.setSize(getXsize() * 2, getYsize() * 2);
        this.body = new PhysicsBody(this, getXsize()/4, getYsize()/4, this.getXsize()/2, this.getYsize()/2);
        System.out.println("Made one: " + filename);
    }

    public void collect() {
        if (!collected) {
            this.ySpeed = 4;
            this.collected = true;
        }
    }

    public void bounce(int tick) {
        this.setPosition(this.x, this.y + (int)(5*Math.sin(tick/12)));
    }

    public void update(int tick) {
        x += xSpeed;
        y += ySpeed;

        bounce(tick);

        if (collected) {
            setSize(getXsize()-2, getYsize()-2);
            x += 1;
            y += 1;
        }
    }
}
