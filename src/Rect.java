/**
 * Created by waleed on 4/20/15.
 */
public class Rect {
    public int x, y, width, height;

    // Rect
    public Rect(int xx, int yy, int w, int h) {
        x = xx;
        y = yy;
        width = w;
        height = h;
    }

    // Rect of body with offset
    public Rect(PhysicsBody body, int offsetX, int offsetY) {
        x = body.parent.getXposition() + body.offsetX + offsetX;
        y = body.parent.getYposition() + body.offsetY + offsetY;
        width = body.width;
        height = body.height;
    }

    // Rect with offset
    public Rect(Rect rect, int offsetX, int offsetY) {
        x = rect.x + offsetX;
        y = rect.y + offsetY;
        width = rect.width;
        height = rect.height;
    }

    public void shift(int xx, int yy) {
        x += xx;
        y += yy;
    }
}
