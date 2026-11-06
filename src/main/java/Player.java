import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Tracks the player's position, direction and active keys. */
public class Player {
    private static final double COLLISION_RADIUS = 0.2; // close to wall tweaking
    private double playerPosX = 1.5;
    private double playerPosY = 5;
    private double playerAngle = 0;
    private final double playerSpeed = 0.030;
    private final double playerRotSpeed = 0.030;
    private final Set<Integer> keys;
    private final Map m;

    /**
     * Creates a player in the given level.
     *
     * @param map The level used for collision checks.
     */
    public Player(final Map m) {
        keys = new HashSet<>();
        this.m = m;
    }

    /** Moves the player using first-person controls. */
    public void move() {
        move(false);
    }

    /**
     * Moves the player, control scheme is adjusted according to render mode.
     *
     * @param topDownMode Whether to use top-down movement controls.
     */
    public void move(final boolean topDownMode) {
        final double sinAngle = Math.sin(playerAngle);
        final double cosAngle = Math.cos(playerAngle);
        double dx = 0;
        double dy = 0;

        final double speed = playerSpeed;
        final double speedSin = speed * sinAngle;
        final double speedCos = speed * cosAngle;

        final Iterator<Integer> it = keys.iterator();
        while (it.hasNext()) {
            final int key = it.next();
            if (topDownMode) {
                if (key == 87) { // W
                    dy += -speed;
                } else if (key == 65) { // A
                    dx += -speed;
                } else if (key == 83) { // S
                    dy += speed;
                } else if (key == 68) { // D
                    dx += speed;
                } else if (key == 37) { // LEFT
                    playerAngle -= playerRotSpeed;
                } else if (key == 39) { // RIGHT
                    playerAngle += playerRotSpeed;
                }
            } else if (key == 87) { // W
                dx += speedCos;
                dy += speedSin;
            } else if (key == 65) { // A
                dx += speedSin;
                dy += -speedCos;
            } else if (key == 83) { // S
                dx += -speedCos;
                dy += -speedSin;
            } else if (key == 68) { // D
                dx += -speedSin;
                dy += speedCos;
            } else if (key == 37) { // LEFT
                playerAngle -= playerRotSpeed;
            } else if (key == 39) { // RIGHT
                playerAngle += playerRotSpeed;
            }
        }

        playerAngle %= Math.TAU;
        checkWallCollision(dx, dy);
    }

    private void checkWallCollision(final double dx, final double dy) {
        // check each axis separately so blocked movement can slide along a wall.
        if (canOccupy(playerPosX + dx, playerPosY)) {
            playerPosX += dx;
        }
        if (canOccupy(playerPosX, playerPosY + dy)) {
            playerPosY += dy;
        }
    }

    private boolean canOccupy(final double x, final double y) {
        final int firstColumn = (int) Math.floor(x - COLLISION_RADIUS);
        final int lastColumn = (int) Math.floor(x + COLLISION_RADIUS);
        final int firstRow = (int) Math.floor(y - COLLISION_RADIUS);
        final int lastRow = (int) Math.floor(y + COLLISION_RADIUS);

        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                if (m.checkIfWall(column, row)) {
                    // the nearest point also detects contact wall corners.
                    final double nearestX = Math.max(column, Math.min(x, column + 1));
                    final double nearestY = Math.max(row, Math.min(y, row + 1));
                    final double distanceX = x - nearestX;
                    final double distanceY = y - nearestY;
                    if (distanceX * distanceX + distanceY * distanceY
                            < COLLISION_RADIUS * COLLISION_RADIUS) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns the precise horizontal position of the player, with decimals.
     *
     * @return The horizontal position.
     */
    public double getPlayerX() {
        return this.playerPosX;
    }

    /**
     * Returns the precise vertical position of the player, with decimals.
     *
     * @return The vertical position.
     */
    public double getPlayerY() {
        return this.playerPosY;
    }

    /**
     * Returns the map column (cell) containing the player.
     *
     * @return The map column.
     */
    public int getPlayerColumn() {
        return (int) Math.floor(this.playerPosX); // floor is used for negative coords.
    }

    /**
     * Returns the map row (cell) containing the player.
     *
     * @return The map row.
     */
    public int getPlayerRow() {
        return (int) Math.floor(this.playerPosY); // floor is used for negative coords.
    }

    /**
     * Returns the direction the player faces, in radians.
     *
     * @return The facing angle in radians.
     */
    public double getPlayerAngle() {
        return this.playerAngle;
    }

    /**
     * Rotates the player by an angle in radians.
     *
     * @param amount The rotation in radians.
     */
    public void rotate(final double amount) {
        playerAngle += amount;
        playerAngle %= Math.TAU;
    }

    /**
     * Sets the direction the player faces.
     *
     * @param angle The facing angle in radians.
     */
    public void setAngle(final double angle) {
        playerAngle = angle % Math.TAU;
    }

    /**
     * Records a pressed key.
     *
     * @param k The key code.
     */
    public void addKey(final int k) {
        keys.add(k);
    }

    /**
     * Stops tracking a released key.
     *
     * @param k The key code.
     */
    public void removeKey(final int k) {
        keys.remove(k);
    }

    /** Clears active keys when the game loses focus or changes mode. */
    public void clearKeys() {
        keys.clear();
    }
}
