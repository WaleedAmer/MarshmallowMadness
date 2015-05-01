import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Movie {

    public static Animation window;
    public static Player player;

    // public static Files fileReader;

    public static Entity[] dynamicEntities = new Entity[6000];
    public static int dynamicEntityCount = 0;
    public static Pickup[] pickups = new Pickup[200];
    public static int pickupCount = 0;
    public static Enemy[] enemies = new Enemy[200];
    public static int enemyCount = 0;

    public static Block mainFloor;
    public static Entity floor;
    public static int gravity = 1;
    public static int tick = 0;

    public static boolean inMenu = true;
    public static int menuOption = 0;
    public static Entity[] menuItems = new Entity[50];
    public static int menuItemCount = 0;

    // public static int[] level1 = {1, 1, 2, 0, 1, 1, 2, 3, 2, 2, 1};

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
        setupLevel();

        // Setup player
        player = new Player("MarshmallowNew.png", 64, 256);
        initEntity(player, true);

        // Setup enemy
        for (int i = 0; i < 1; i++) {
            enemies[i] = new Enemy("Match.png", 32*i, 64);
            initEnemy(enemies[i]);
        }

        // Setup pickups
        Pickup coin = new Pickup("Coin.png", 96, 64);
        initPickup(coin);
        Pickup drop = new Pickup("drop.png", 156, 64);
        initPickup(drop);

        // Assign initial floor
        floor = mainFloor;

        // MAIN LOOP
        while (tick > -1) {

            // Assign floor
            floor = getFloor();

            // Handle player landing
            int offset = player.ySpeed;
            for (int speed = player.ySpeed; speed < 0; speed++) {
                if (player.getYposition() + speed == floorYposition()) {
                    offset = speed;
                }
            }

            // Player is on the ground
            if (checkCollision(floor.rectOfSelf(), player.destRect()) || player.getYposition() + offset <= floor.getYposition() + floor.getYsize()) {
                gravity = 0;
                player.ySpeed = 0;
                player.setPosition(player.getXposition(), floor.getYposition() + floor.getYsize());
                player.canJump = true;
            }
            else {
                //System.out.println("In the air");
                gravity = 1;
            }

            // Apply gravity to Player's ySpeed
            player.ySpeed -= gravity;

            // Maximum ySpeed
            if (player.ySpeed < -16) {
                player.ySpeed = -16;
            }

            // Wrapping around the screen
            if (player.getXposition() > window.getWidth()) { // Off the right
                player.setPosition(-64, player.getYposition());
            }
            if (player.getXposition() < -64) { // Off the left
                player.setPosition(window.getWidth(), player.getYposition());
            }

            // Move player by xSpeed and ySpeed
            if (placeFree(player, player.xSpeed, player.ySpeed)) {
                move(player, player.xSpeed, player.ySpeed);
            }
            else {
                // Snapping xSpeed
                int xSpeed = player.xSpeed;
                if (xSpeed > 0) { // Moving right
                    for (xSpeed = player.xSpeed; xSpeed > 0; xSpeed--) {
                        if (placeFree(player, xSpeed, player.ySpeed)) {
                            break;
                        }
                    }
                }
                else if (xSpeed < 0) { // Moving left
                    for (xSpeed = player.xSpeed; xSpeed < 0; xSpeed++) {
                        if (placeFree(player, xSpeed, player.ySpeed)) {
                            break;
                        }
                    }
                }

                // Snapping ySpeed
                int ySpeed = player.ySpeed;
                if (ySpeed > 0) { // Moving up
                    for (ySpeed = player.ySpeed; ySpeed > 0; ySpeed--) {
                        if (placeFree(player, xSpeed, ySpeed)) {
                            break;
                        }
                    }
                }
                else if (ySpeed < 0) { // Moving down
                    for (ySpeed = player.ySpeed; ySpeed < 0; ySpeed++) {
                        if (placeFree(player, xSpeed, ySpeed)) {
                            break;
                        }
                    }
                }

                // Move by new xSpeed and ySpeed
                move(player, xSpeed, ySpeed);
            }

            // Dynamic Entity logic
            for (int i = 0; i < dynamicEntityCount; i++) {
                // Call update method
                dynamicEntities[i].update(tick);
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

            // End frame
            window.frameFinished();
            tick += 1;
        }
    }

    // Set up an entity
    public static void initEntity(Entity entity, boolean updates) {
        if (updates) {
            dynamicEntities[dynamicEntityCount] = entity;
            dynamicEntityCount++;
        }

        if (inMenu) {
            menuItems[menuItemCount] = entity;
            menuItemCount++;
        }

        entity.game = new Movie();
        window.addSprite(entity);
    }

    // Set up an entity
    public static void initEntity(Entity entity, boolean updates, String pickupSprite) {
        if (updates) {
            dynamicEntities[dynamicEntityCount] = entity;
            dynamicEntityCount++;
        }

        entity.game = new Movie();
        window.addSprite(entity);

        initPickup(new Pickup(pickupSprite, entity));
    }

    // Determine floor
    public static Entity getFloor() {
        // Iterate through dynamic Entities and check if Player will collide
        for (int i = 0; i < dynamicEntityCount; i++) {
            Entity newFloor = dynamicEntities[i];
            if (newFloor != player && (checkCollision(newFloor.body, player.destRect()) || checkCollision(newFloor.body, new Rect(player.body, 0, -1)))) {
                return newFloor;
            }
        }
        return mainFloor;
    }

    // Set up an pickup
    public static void initPickup(Pickup pickup) {
        pickups[pickupCount] = pickup;
        pickupCount++;
        pickup.game = new Movie();
        window.addSprite(pickup);
    }

    // Set up an enemy
    public static void initEnemy(Enemy enemy) {
        enemies[enemyCount] = enemy;
        enemyCount++;
        enemy.game = new Movie();
        window.addSprite(enemy);
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
                // If Right or Left is released
                if ((e.getKeyCode() == 0x27 && player.xSpeed > 0) || (e.getKeyCode() == 0x25 && player.xSpeed < 0)) {
                    player.xSpeed = 0;
                }
            }
        };
        window.addKeyListener(kl);
        window.setFocusable(true);
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

        mainFloor = new Block("Grass.png", 0, -64);
        mainFloor.setSize(666, 64);
        mainFloor.setBody(new PhysicsBody(mainFloor));
        initEntity(mainFloor, true);

        for (int i = 0; i < code.length; i++) {
            for (int j = 0; j < code[i]; j++) {
                Block block = new Block("Grass.png", 64*i, 64*j);
                block.setBody(new PhysicsBody(block));
                initEntity(block, true, "Coin.png");
            }
        }
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
        for (int i = 0; i < dynamicEntityCount; i++) {
            Entity ent = dynamicEntities[i];
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
    public static int floorYposition() {
        return floor.getYposition() + floor.getYsize();
    }
}
