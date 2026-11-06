import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/** Displays either the first-person view or the top-down map. */
public class View extends JPanel {
	private static final Color CEILING_COLOR = new Color(35, 35, 35);
	private static final Color FLOOR_COLOR = new Color(103, 103, 103);
	private static final Color FPS_BACKGROUND_COLOR = new Color(0, 0, 0, 180);
	private static final Font FPS_FONT = new Font(Font.MONOSPACED, Font.BOLD, 16);
	private final Map m;
	private final Player p;
	private final RayCasting r;
	private boolean topDownMode = false;
	private BufferedImage background;
	private int backgroundWidth;
	private int backgroundFov;
	private long fpsSampleStart = System.nanoTime();
	private int paintedFrames;
	private String fpsText = "FPS: --";

	/**
	 * Creates a view of the level and player.
	 *
	 * @param m The level to display.
	 * @param p The player to display.
	 * @param r The first-person renderer.
	 */
	public View(final Map m, final Player p, final RayCasting r) {
		this.m = m;
		this.p = p;
		this.r = r;
	}

	/** Switches between the first-person and top-down modes. */
	public void toggleRenderMode() {
		topDownMode = !topDownMode;
		repaint();
	}

	/**
	 * Returns whether the top-down mode is active.
	 *
	 * @return {@code true} when the top-down mode is active.
	 */
	public boolean isTopDownMode() {
		return topDownMode;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);

		if (topDownMode) {
			drawTopDown(g);
		} else {
			drawBackground(g);
			r.rayCast(g, getHeight());
			drawCrosshair(g);
		}
		updateFps(System.nanoTime());
		drawFps(g);
	}

	private void updateFps(final long now) {
		// count actual paints because swing can apparently combine multiple repaint requests.
		paintedFrames++;
		final long elapsedNanos = now - fpsSampleStart;
		if (elapsedNanos >= 1_000_000_000L) {
			fpsText = "FPS: " + Math.round(paintedFrames * 1_000_000_000.0 / elapsedNanos);
			paintedFrames = 0;
			fpsSampleStart = now;
		}
	}

	private void drawFps(final Graphics g) {
		g.setFont(FPS_FONT);
		final FontMetrics metrics = g.getFontMetrics();
		g.setColor(FPS_BACKGROUND_COLOR);
		g.fillRect(10, 10, metrics.stringWidth(fpsText) + 12, metrics.getHeight() + 8);
		g.setColor(Color.white);
		g.drawString(fpsText, 16, 14 + metrics.getAscent());
	}

	private void drawBackground(final Graphics g) {
		final int width = getWidth();
		final int height = getHeight();
		if (width <= 0 || height <= 0) {
			return;
		}

		final int fov = r.getFovDegrees();
		if (background == null || background.getHeight() != height
				|| backgroundWidth != width || backgroundFov != fov) {
			background = createBackground(height);
			backgroundWidth = width;
			backgroundFov = fov;
		}
		g.drawImage(background, 0, 0, width, height, null);
	}

	private BufferedImage createBackground(final int height) {
		// every pixel in a row has the same color, so one opaque column is enough.
		final BufferedImage image = new BufferedImage(1, height, BufferedImage.TYPE_INT_RGB);
		final Graphics graphics = image.getGraphics();

		try {
			final int horizonY = height / 2;
			graphics.setColor(CEILING_COLOR);
			graphics.fillRect(0, 0, 1, horizonY);
			graphics.setColor(FLOOR_COLOR);
			graphics.fillRect(0, horizonY, 1, height - horizonY);

			// cache the shading because player movement does not change these distances.
			for (int y = 0; y < height; y++) {
				final int darkness = r.getSurfaceDarkness(y - horizonY);
				graphics.setColor(new Color(0, 0, 0, darkness));
				graphics.fillRect(0, y, 1, 1);
			}
		} finally {
			graphics.dispose();
		}

		return image;
	}

	private void drawCrosshair(final Graphics g) {
		final int centerX = getWidth() / 2;
		final int centerY = getHeight() / 2;
		final int gap = 8; // half-life style crosshair
		final int length = 20;

		g.setColor(Color.yellow);
		g.drawLine(centerX - length, centerY, centerX - gap, centerY);
		g.drawLine(centerX + gap, centerY, centerX + length, centerY);
		g.drawLine(centerX, centerY - length, centerX, centerY - gap);
		g.drawLine(centerX, centerY + gap, centerX, centerY + length);
	}

	private void drawTopDown(final Graphics g) {
		final int[][] map = m.getMap();
		final int rows = map.length;
		final int cols = map[0].length;
		final int cellW = getWidth() / cols;
		final int cellH = getHeight() / rows;

		// wall blocks
		g.setColor(Color.gray);
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				if (map[y][x] != 0) {
					g.fillRect(x * cellW, y * cellH, cellW, cellH);
				}
			}
		}

		final int playerX = (int) (p.getPlayerX() * cellW);
		final int playerY = (int) (p.getPlayerY() * cellH);

		// laser sight
		g.setColor(Color.red);
		g.drawLine(
			playerX,
			playerY,
			playerX + (int) (getWidth() * Math.cos(p.getPlayerAngle())),
			playerY + (int) (getWidth() * Math.sin(p.getPlayerAngle()))
		);

		// player dot
		g.setColor(Color.orange);
		g.fillOval(playerX - 15, playerY - 15, 30, 30);
	}
}
