import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import javax.imageio.ImageIO;

/** Loads the texture images used by the renderer. */
public class Textures {
    private final BufferedImage[] wallTextures = {
        null, // empty cell, is never called because it's never drawn.
        loadWallTexture("/tex/greystone.png"),
        loadWallTexture("/tex/redbrick.png"),
        loadWallTexture("/tex/wood.png")
    };

    /**
     * Returns the texture for a wall type.
     *
     * @param wallType The wall type.
     * @return The loaded image for the wall type.
     * @throws IllegalArgumentException If the given wall type has no texture.
     */
    public BufferedImage getWallTexture(final int wallType) {
        if (wallType <= 0 || wallType >= wallTextures.length) {
            throw new IllegalArgumentException("No texture for wall type: " + wallType);
        }
        return wallTextures[wallType];
    }

    private static BufferedImage loadWallTexture(final String path) {
        final URL resource = Textures.class.getResource(path);
        if (resource == null) {
            throw new IllegalStateException("Wall texture is missing: " + path);
        }

        final BufferedImage image;
        try {
            image = ImageIO.read(resource);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read the wall texture: " + path, e);
        }

        if (image == null) {
            throw new IllegalStateException("Wall texture has an unsupported image format: " + path);
        }
        return image;
    }
}
