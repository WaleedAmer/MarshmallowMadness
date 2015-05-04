import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class Movie {

    public static Animation window;
    public static Player player;

    public static Entity[] entities = new Entity[6000];
    public static int entityCount = 0;

    public static Entity[] solidEntities = new Entity[6000];
    public static int solidEntityCount = 0;

    public static Pickup[] pickups = new Pickup[6000];
    public static int pickupCount = 0;

    public static Enemy[] enemies = new Enemy[6000];
    public static int enemyCount = 0;

    public static Entity[] dynamicEntities = new Entity[6000];
    public static int dynamicEntityCount = 0;
    
    public static Block mainFloor;
    public static int tick = 0;

    public static boolean inMenu = true;
    public static int menuOption = 0;
    public static Entity[] menuItems = new Entity[50];
    public static int menuItemCount = 0;

    public static int[] firstBlob = {1};
    public static int[][] blobs = {{1, 1, 2, 0, 1, 1}, {2, 0, 1, 1, 2, 2},
                                   {1, 1, 2, 3, 3, 0}, {2, 1, 0, 0, 1, 2},
                                   {2, 2, 2, 2, 2, 0}, {2, 1, 2, 1, 0, 0}};

    public static Random rand = new Random();

    public static void main(String[] args) {
        // Setup window
        window = new Animation(666, 512);
        window.setFrameRate(60);
        window.setTitle("Marshmallow Madness");

        // Setup key handling
        setupKeyboardInput();

        // Handle menu
        while(inMenu) {
            window.setBackgroundImage("Menu"+menuOption+".png");
            window.frameFinished();
        }
        // Clear menu items
        for (int i = 0; i < menuItemCount; i++) {
            Entity item = menuItems[i];
            move(item, 0, 512);
        }
        window.setBackgroundImage("sky.png");

        // Generate the level
        setupLevel(firstBlob);

        // Setup player
        player = new Player("MarshmallowNew.png", 192, 256);
        initEntity(player, true);
        dynamicEntities[dynamicEntityCount] = player;
        dynamicEntityCount++;

        // Setup enemy
        Enemy enemy = new Enemy("Match.png", 64, 64);
        initEnemy(enemy);

        // Assign initial floor
        for (int i = 0; i < dynamicEntityCount; i++) {
            Entity ent = dynamicEntities[i];

            ent.floor = mainFloor;
        }

        // MAIN LOOP
        while (tick > -1) {

            // Solid Entity logic
            for (int i = 0; i < solidEntityCount; i++) {
                // Call update method
                solidEntities[i].update(tick);
            }

            // Pickup logic
            for (int i = 0; i < pickupCount; i++) {
                Pickup pickup = pickups[i];

                // If player collides with a pickup
                if (!pickup.collected && checkCollision(player, pickup)) {
                    pickup.collect();
                }

                // Call update method of the Pickup
                pickup.update(tick);
            }

            // Enemy logic
            for (int i = 0; i < enemyCount; i++) {
                // Call update method
                Enemy en = enemies[i];
                en.update(tick);
            }

            // "Pan the camera"
            for (int i = 0; i < entityCount; i++) {
                entities[i].setPosition(entities[i].getXposition() - 2, entities[i].getYposition());
                entities[i].body.x -= 2;
            }

            // Generate next procedural section of the level
            if (tick % 192 == 0) {
                setupLevel(blobs[randInt(0, 5)]);
            }

            // Iterate through dynamic (affected by gravity) entities
            for (int i = 0; i < dynamicEntityCount; i++) {
                Entity ent = dynamicEntities[i];
                
                // Assign floor
                ent.floor = getFloor(ent);

                // Handle ent landing
                int offset = ent.ySpeed;
                for (int speed = ent.ySpeed; speed < 0; speed++) {
                    if (ent.getYposition() + speed == floorYposition(ent)) {
                        offset = speed;
                    }
                }

                // ent is on the ground
                if (checkCollision(ent.floor.rectOfSelf(), ent.destRect()) || ent.getYposition() + offset <= ent.floor.getYposition() + ent.floor.getYsize()) {
                    ent.gravity = 0;
                    ent.ySpeed = 0;
                    ent.setPosition(ent.getXposition(), ent.floor.getYposition() + ent.floor.getYsize());
                    ent.canJump = true;
                } else {
                    //System.out.println("In the air");
                    ent.gravity = 1;
                }

                // Apply gravity to ent's ySpeed
                ent.ySpeed -= ent.gravity;

                // Maximum ySpeed
                if (ent.ySpeed < -16) {
                    ent.ySpeed = -16;
                }

                // Move ent by xSpeed and ySpeed
                if (placeFree(ent, ent.xSpeed, ent.ySpeed)) {
                    move(ent, ent.xSpeed, ent.ySpeed);
                } else {
                    // Snapping xSpeed
                    int xSpeed = ent.xSpeed;
                    if (xSpeed > 0) { // Moving right
                        for (xSpeed = ent.xSpeed; xSpeed > 0; xSpeed--) {
                            if (placeFree(ent, xSpeed, ent.ySpeed)) {
                                break;
                            }
                        }
                    } else if (xSpeed < 0) { // Moving left
                        for (xSpeed = ent.xSpeed; xSpeed < 0; xSpeed++) {
                            if (placeFree(ent, xSpeed, ent.ySpeed)) {
                                break;
                            }
                        }
                    }

                    // Snapping ySpeed
                    int ySpeed = ent.ySpeed;
                    if (ySpeed > 0) { // Moving up
                        for (ySpeed = ent.ySpeed; ySpeed > 0; ySpeed--) {
                            if (placeFree(ent, xSpeed, ySpeed)) {
                                break;
                            }
                        }
                    } else if (ySpeed < 0) { // Moving down
                        for (ySpeed = ent.ySpeed; ySpeed < 0; ySpeed++) {
                            if (placeFree(ent, xSpeed, ySpeed)) {
                                break;
                            }
                        }
                    }

                    // Move by new xSpeed and ySpeed
                    move(ent, xSpeed, ySpeed);

                    // DEBUG FEATURE - REMOVE IN FULL VERSION
                    // Wrapping around the screen
                    if (ent.getXposition() > window.getWidth()) { // Off the right
                        ent.setPosition(window.getWidth() - ent.getXsize(), ent.getYposition());
                    }
                    if (ent.getXposition() < 0) { // Off the left
                        ent.setPosition(0, ent.getYposition());
                    }

                }
            }

            // End frame
            window.frameFinished();
            tick += 1;
        }
    }

    // Set up game scene
    public static void setupLevel() {
        mainFloor = new Block("Grass.png", 0, 0);
        mainFloor.setSize(666, 64);
        mainFloor.setBody(new PhysicsBody(mainFloor));
        initEntity(mainFloor, true);

        Block block = new Block ("Grass.png", 256, 64);
        block.setBody(new PhysicsBody(block));
        initEntity(block, true, "Coin.png");

        Block block2 = new Block ("Grass.png", 384, 64);
        block2.setBody(new PhysicsBody(block2));
        initEntity(block2, true);

        Block block3 = new Block ("Grass.png", 256+128, 128);
        block3.setBody(new PhysicsBody(block3));
        initEntity(block3, true, "Coin.png");
    }

    public static void setupLevel(int[] code) {
        int offset = 2*(code.length*64);

        if (code == firstBlob) {
            mainFloor = new Block("Grass.png", 0, 0);
            mainFloor.setSize(64*12+2, 64);
            mainFloor.setBody(new PhysicsBody(mainFloor));
            initEntity(mainFloor, true);
        }
        else {
            mainFloor = new Block("Grass.png", offset, -64);
            mainFloor.setSize(384, 64);
            mainFloor.setBody(new PhysicsBody(mainFloor));
            initEntity(mainFloor, true);

            for (int i = 0; i < code.length; i++) {
                for (int j = 0; j < code[i]; j++) {
                    Block block = new Block("Grass.png", offset + 64 * i, 64 * j);
                    block.setBody(new PhysicsBody(block));
                    if (j == code[i] - 1) {
                        initEntity(block, true, "Coin.png");
                    } else {
                        initEntity(block, true);
                    }
                }
            }
        }
    }

    // Set up an entity
    public static void initEntity(Entity entity, boolean updates) {
        if (updates) {
            solidEntities[solidEntityCount] = entity;
            solidEntityCount++;
        }

        if (inMenu) {
            menuItems[menuItemCount] = entity;
            menuItemCount++;
        }

        entities[entityCount] = entity;
        entityCount++;

        entity.game = new Movie();
        window.addSprite(entity);
    }

    // Set up an entity
    public static void initEntity(Entity entity, boolean updates, String pickupSprite) {
        if (updates) {
            solidEntities[solidEntityCount] = entity;
            solidEntityCount++;
        }
        window.addSprite(entity);

        entity.game = new Movie();

        entities[entityCount] = entity;
        entityCount++;

        initPickup(new Pickup(pickupSprite, entity));
    }

    // Determine floor
    public static Entity getFloor(Entity ent) {
        // Iterate through dynamic Entities and check if Player will collide
        for (int i = 0; i < solidEntityCount; i++) {
            Entity newFloor = solidEntities[i];
            if (newFloor != ent && (checkCollision(newFloor.body, ent.destRect()) || checkCollision(newFloor.body, new Rect(ent.body, 0, -1)))) {
                return newFloor;
            }
        }
        return mainFloor;
    }

    // Set up an pickup
    public static void initPickup(Pickup pickup) {
        pickups[pickupCount] = pickup;
        pickupCount++;
        window.addSprite(pickup);

        pickup.game = new Movie();

        entities[entityCount] = pickup;
        entityCount++;
    }

    // Set up an enemy
    public static void initEnemy(Enemy enemy) {
        enemies[enemyCount] = enemy;
        enemyCount++;
        window.addSprite(enemy);

        enemy.player = player;
        enemy.floor = mainFloor;
        enemy.game = new Movie();

        entities[entityCount] = enemy;
        entityCount++;

        dynamicEntities[dynamicEntityCount] = enemy;
        dynamicEntityCount++;
    }

    // Set up keyboard input
    public static void setupKeyboardInput() {

        KeyListener kl = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                //
            }

            @Override
            public void keyPressed(KeyEvent e) {

                if (inMenu) {
                    // If Space is pressed
                    if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (menuOption == 0) {
                            inMenu = false;
                        }
                        else if (menuOption == 1) {
                            menuOption = 2;
                        }
                        else if (menuOption == 2) {
                            menuOption = 1;
                        }
                    }

                    // Toggle option
                    if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                        if (menuOption == 0){
                            menuOption = 1;
                        }
                        else if (menuOption == 1) {
                            menuOption = 0;
                        }
                    }
                }
                else {
                    // If Right is pressed
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        player.xSpeed = 6;
                    }

                    // If Left is pressed
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        player.xSpeed = -6;
                    }

                    // If Up is pressed
                    if (e.getKeyCode() == KeyEvent.VK_UP && player.canJump && player.ySpeed == 0) {
                        player.ySpeed = 16;
                        player.canJump = false;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!inMenu) {
                    // If Right or Left is released
                    if ((e.getKeyCode() == 0x27 && player.xSpeed > 0) || (e.getKeyCode() == 0x25 && player.xSpeed < 0)) {
                        player.xSpeed = 0;
                    }
                }
            }
        };
        window.addKeyListener(kl);
        window.setFocusable(true);
    }

    // Move an entity
    public static void move(Entity entity, int xx, int yy) {
        entity.setPosition(entity.getXposition() + xx, entity.getYposition() + yy);
    }

    // Check if an Entity's destination is empty (no collision)
    public static boolean placeFree(Entity entity, int xx, int yy) {
        // Create a Rect to represent where the body will go
        Rect destRect = new Rect(entity.body, xx, yy);

        // Check if destRect collides with a dynamic Entity
        for (int i = 0; i < solidEntityCount; i++) {
            Entity ent = solidEntities[i];
            if (ent != entity && checkCollision(ent.body, destRect)){
                return false;
            }
        }
        return true;
    }


    // Check collision between two PhysicsBodies
    public static boolean checkCollision(PhysicsBody bodyA, PhysicsBody bodyB) {
        int xA = bodyA.parent.getXposition() + bodyA.offsetX;
        int yA = bodyA.parent.getYposition() + bodyA.offsetY;
        int widthA = bodyA.width;
        int heightA = bodyA.height;

        int xB = bodyB.parent.getXposition() + bodyB.offsetX;
        int yB = bodyB.parent.getYposition() + bodyB.offsetY;
        int widthB = bodyB.width;
        int heightB = bodyB.height;

        return xA < xB + widthB &&
                xA + widthA > xB &&
                yA < yB + heightB &&
                yA + heightA > yB;
    }

    // Check collision between two PhysicsBodies
    public static boolean checkCollision(Entity entityA, Entity entityB) {
        PhysicsBody bodyA = entityA.body;
        PhysicsBody bodyB = entityB.body;

        int xA = bodyA.parent.getXposition() + bodyA.offsetX;
        int yA = bodyA.parent.getYposition() + bodyA.offsetY;
        int widthA = bodyA.width;
        int heightA = bodyA.height;

        int xB = bodyB.parent.getXposition() + bodyB.offsetX;
        int yB = bodyB.parent.getYposition() + bodyB.offsetY;
        int widthB = bodyB.width;
        int heightB = bodyB.height;

        return xA < xB + widthB &&
                xA + widthA > xB &&
                yA < yB + heightB &&
                yA + heightA > yB;
    }

    // Check collision between a PhysicsBody and a Rect
    public static boolean checkCollision(PhysicsBody body, Rect rect) {
        int xA = body.x;
        int yA = body.y;
        int widthA = body.width;
        int heightA = body.height;

        int xB = rect.x;
        int yB = rect.y;
        int widthB = rect.width;
        int heightB = rect.height;

        return xA < xB + widthB &&
                xA + widthA > xB &&
                yA < yB + heightB &&
                yA + heightA > yB;
    }

    // Check collision between two Rects
    public static boolean checkCollision(Rect rectA, Rect rectB) {
        int xA = rectA.x;
        int yA = rectA.y;
        int widthA = rectA.width;
        int heightA = rectA.height;

        int xB = rectB.x;
        int yB = rectB.y;
        int widthB = rectB.width;
        int heightB = rectB.height;

        return xA < xB + widthB &&
                xA + widthA > xB &&
                yA < yB + heightB &&
                yA + heightA > yB;
    }

    // Get position of floor's surface
    public static int floorYposition(Entity ent) {
        return ent.floor.getYposition() + ent.floor.getYsize();
    }

    public static int randInt(int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }
}
