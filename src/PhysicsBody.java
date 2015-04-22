/**
 * Created by waleed on 4/20/15.
 */
public class PhysicsBody {

    public int x, y, width, height, offsetX, offsetY;
    public boolean sameAsSprite;
    public Sprite parent;

    // PhysicsBody with manual attributes
    public PhysicsBody(Entity entity, int offX, int offY, int w, int h) {
        offsetX = offX;
        offsetY = offY;
        width = w;
        height = h;
        x = entity.getXposition() + offsetX;
        y = entity.getYposition() + offsetY;

        sameAsSprite = false;
        parent = entity;
        entity.setBody(this);
    }

    // PhysicsBody from Entity
    public PhysicsBody(Entity entity) {
        offsetX = 0;
        offsetY = 0;
        width = entity.getXsize();
        height = entity.getYsize();
        x = entity.getXposition();
        y = entity.getYposition();

        sameAsSprite = true;
        parent = entity;
        entity.setBody(this);
    }
}