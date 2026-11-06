import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;

/** Draws the first-person view by casting rays against the map. */
public class RayCasting {
    private double fov = Math.toRadians(75);
    private double halfFov = fov / 2;
    private final double numRays = RenderSettings.VIEW_WIDTH / 2.0;
    private final int maxDepth = 20;

    // 2.5d projection settings
    private final double halfWidth = RenderSettings.VIEW_WIDTH / 2.0;
    private final double halfHeight = RenderSettings.VIEW_HEIGHT / 2.0;
    private double screenDist = halfWidth / Math.tan(halfFov);
    private final double scale = RenderSettings.VIEW_WIDTH / numRays;

    private final Map m;
    private final Player p;
    private final Textures t;

    /**
     * Creates a renderer for the level and player.
     *
     * @param m The level to render.
     * @param p The player whose view is rendered.
     * @param textures The images used to render the walls.
     */
    public RayCasting(final Map m, final Player p, final Textures t) {
        this.m = m;
        this.p = p;
        this.t = t;
    }

    /**
     * Returns the horizontal field of view in degrees.
     *
     * @return The field of view in degrees.
     */
    public int getFovDegrees() {
        return (int)Math.round(Math.toDegrees(fov));
    }

    /**
     * Sets the field of view, limited to 30 through 150 degrees.
     *
     * @param degrees The requested field of view in degrees.
     */
    public void setFovDegrees(int degrees) {
    	// sanity check the value
        if (degrees < 30) {
            degrees = 30;
        } else if (degrees > 150) {
            degrees = 150;
        }

        fov = Math.toRadians(degrees);
        halfFov = fov / 2;
        screenDist = halfWidth / Math.tan(halfFov);
    }

    /**
     * Returns the distance shading for a ceiling or floor row.
     *
     * @param rowOffset The row's vertical offset from the horizon, in pixels.
     * @return The black overlay opacity from 0 through 255.
     */
    public int getSurfaceDarkness(final int rowOffset) {
        if (rowOffset == 0) {
            return 255;
        }

        // The level camera is halfway between a floor and ceiling one map cell apart.
        final double depth = screenDist / (2.0 * Math.abs((double) rowOffset));
        return getDarkness(depth);
    }

    private static int getDarkness(final double depth) {
        final double brightness = 1.0 / (1.0 + Math.pow(depth, 5) * 0.00002);
        return (int) Math.round(255 * (1.0 - brightness));
    }

    /**
     * Draws the walls visible from the player's current position.
     *
     * @param g The graphics context to draw on.
     * @param viewportHeight The height of the visible view, in pixels.
     */
    public void rayCast(final Graphics g, final int viewportHeight) {
        if (viewportHeight <= 0) {
            return;
        }

        final double playerX = p.getPlayerX();
        final double playerY = p.getPlayerY();
        final int playerColumn = p.getPlayerColumn();
        final int playerRow = p.getPlayerRow();
        final double playerAngle = p.getPlayerAngle();

        for (int i = 0; i < numRays; i++) {
            final double screenX = i * scale + scale / 2 - halfWidth;
            final double rayAngle = playerAngle + Math.atan(screenX / screenDist) + 0.0001;
            final double sinAngle = Math.sin(rayAngle);
            final double cosAngle = Math.cos(rayAngle);

            final RayHit horizontal = findHit(
                    playerX, playerY, playerColumn, playerRow,
                    sinAngle, cosAngle, GridAxis.HORIZONTAL);
            final RayHit vertical = findHit(
                    playerX, playerY, playerColumn, playerRow,
                    sinAngle, cosAngle, GridAxis.VERTICAL);
            if (horizontal.wallType() == 0 && vertical.wallType() == 0) {
            	// we have an empty cell (no wall)
                continue;
            }
            final RayHit nearest = vertical.depth() < horizontal.depth() ? vertical : horizontal;

            // use perpendicular distance to avoid getting a fisheye effect.
            final double perpendicularDepth = nearest.depth() * Math.cos(playerAngle - rayAngle);

            drawWallStrip(g, i, viewportHeight, nearest, perpendicularDepth);
        }
    }

    private RayHit findHit(final double playerX, final double playerY, final int playerColumn,
        	final int playerRow, final double sinAngle, final double cosAngle, final GridAxis axis) {
        final boolean horizontal = axis == GridAxis.HORIZONTAL;

        // 'across' crosses grid boundaries, 'along' locates the hit on the wall.
        final double playerAcross = horizontal ? playerY : playerX;
        final double playerAlong = horizontal ? playerX : playerY;
        final int cellAcross = horizontal ? playerRow : playerColumn;
        final double directionAcross = horizontal ? sinAngle : cosAngle;
        final double directionAlong = horizontal ? cosAngle : sinAngle;

        double across;
        final double acrossStep;
        if (directionAcross > 0) {
            across = cellAcross + 1;
            acrossStep = 1;
        } else {
            // sample inside the preceding cell when crossing in the negative direction.
            across = cellAcross - 0.000001;
            acrossStep = -1;
        }

        double depth = (across - playerAcross) / directionAcross;
        double along = playerAlong + depth * directionAlong;
        final double depthStep = acrossStep / directionAcross;
        final double alongStep = depthStep * directionAlong;

        for (int step = 0; step < maxDepth; step++) {
            final double hitX = horizontal ? along : across;
            final double hitY = horizontal ? across : along;
            final int wallType = m.getCellType((int) hitX, (int) hitY);
            if (wallType != 0) {
                return new RayHit(depth, along, wallType);
            }

            along += alongStep;
            across += acrossStep;
            depth += depthStep;
        }

        return new RayHit(Double.POSITIVE_INFINITY, along, 0);
    }

    private void drawWallStrip(final Graphics g, final int rayIndex, final int viewportHeight,
            final RayHit hit, final double depth) {
        final BufferedImage wallTexture = t.getWallTexture(hit.wallType());

        // the fractional hit position selects a column within this wall tile.
        final double textureOffset = hit.wallPosition() - Math.floor(hit.wallPosition());
        final int textureX = Math.min(
            wallTexture.getWidth() - 1,
            (int) (textureOffset * wallTexture.getWidth())
        );

        final double projHeight = screenDist / (depth + 0.0001);
        final int stripX = rayIndex * (int) scale;
        final int stripWidth = (int) scale;
        final int stripY = (int) (halfHeight - projHeight / 2);
        final int stripHeight = Math.max(1, (int) projHeight);

        final int visibleTop = Math.max(0, stripY);
        final int visibleBottom = Math.min(viewportHeight, stripY + stripHeight);
        if (visibleBottom <= visibleTop) {
            return;
        }

        // crop the texture with the wall so close walls never use crazy huge draw coordinates.
        final double textureScale = (double) wallTexture.getHeight() / stripHeight;
        final int textureTop = Math.min(
        	wallTexture.getHeight() - 1, (int) Math.floor((visibleTop - stripY) * textureScale)
        );
        final int textureBottom = Math.min(
        	wallTexture.getHeight(), Math.max(textureTop + 1, (int) Math.ceil((visibleBottom - stripY) * textureScale))
        );

        g.drawImage(
            wallTexture,
            stripX, visibleTop,
            stripX + stripWidth, visibleBottom,
            textureX, textureTop,
            textureX + 1, textureBottom,
            null
        );

        final int darkness = getDarkness(depth);
        g.setColor(new Color(0, 0, 0, darkness));
        g.fillRect(stripX, visibleTop, stripWidth, visibleBottom - visibleTop);
    }

    private enum GridAxis {
        HORIZONTAL,
        VERTICAL
    }

    // note that this is a record class.
    private record RayHit(double depth, double wallPosition, int wallType) {
    }
}
