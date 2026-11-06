import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** Runs the game window and handles the player's input. */
public class Control extends JFrame implements ActionListener, KeyListener, MouseListener,
		MouseMotionListener, WindowFocusListener {
	private JDialog optionsDialog;
	private final Map map;
	private final Textures textures;
	private final Player player;
	private final View view;
	private final RayCasting cast;
	private final Robot robot;
	private final Cursor defaultCursor;
	private final Cursor hiddenCursor;
	private boolean mouseCaptured = false;
	private boolean ignoringRobotMove = false;
	private double mouseSensitivity3D = 0.001;
	private double mouseSensitivityTopDown = 0.0005;
	private final double topDownAimRadius = 250;
	private double topDownAimX = 250;
	private double topDownAimY = 0;

	/** Creates the game window and starts its update timer. */
	public Control() {
		map = new Map();
		textures = new Textures();
		player = new Player(map);
		cast = new RayCasting(map, player, textures);
		view = new View(map, player, cast);
		robot = createRobot();
		defaultCursor = view.getCursor();
		hiddenCursor = createHiddenCursor();
		view.setPreferredSize(new java.awt.Dimension(RenderSettings.VIEW_WIDTH, RenderSettings.VIEW_HEIGHT));
		view.setBackground(Color.black);
		view.setFocusable(true);
		view.addKeyListener(this);
		view.addMouseListener(this);
		view.addMouseMotionListener(this);
		setLocation(50, 50);
		add(view);
		addKeyListener(this);
		addWindowFocusListener(this);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();

		final Timer t = new Timer(10, this); // fps, time per frame (10ms = 100fps, 25ms = 40fps etc.)
		t.setRepeats(true);
		t.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		player.move(view.isTopDownMode());
		view.repaint();
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		final int k = e.getKeyCode();
		if (k == KeyEvent.VK_ESCAPE) {
			releaseMouse();
		} else if (k == KeyEvent.VK_W) {
			player.addKey(k);
		} else if (k == KeyEvent.VK_A) {
			player.addKey(k);
		} else if (k == KeyEvent.VK_S) {
			player.addKey(k);
		} else if (k == KeyEvent.VK_D) {
			player.addKey(k);
		} else if (k == KeyEvent.VK_LEFT) {
			player.addKey(k);
		} else if (k == KeyEvent.VK_RIGHT) {
			player.addKey(k);
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		final int k = e.getKeyCode();
		if (k == KeyEvent.VK_F10) {
			toggleOptionsDialog();
		} else if (k == KeyEvent.VK_F11) {
			view.toggleRenderMode();
			if (view.isTopDownMode()) {
				syncTopDownAimToPlayerAngle();
				player.clearKeys();
				captureMouse();
			}
		} else if (k == KeyEvent.VK_W) {
			player.removeKey(k);
		} else if (k == KeyEvent.VK_A) {
			player.removeKey(k);
		} else if (k == KeyEvent.VK_S) {
			player.removeKey(k);
		} else if (k == KeyEvent.VK_D) {
			player.removeKey(k);
		} else if (k == KeyEvent.VK_LEFT) {
			player.removeKey(k);
		} else if (k == KeyEvent.VK_RIGHT) {
			player.removeKey(k);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		view.requestFocusInWindow();
		captureMouse();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (view.isTopDownMode()) {
			handleTopDownMouseLook(e);
		} else {
			handleMouseLook(e);
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if (view.isTopDownMode()) {
			handleTopDownMouseLook(e);
		} else {
			handleMouseLook(e);
		}
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
		releaseMouse();
		player.clearKeys();
	}

	private void toggleOptionsDialog() {
		if (isOptionsDialogOpen()) {
			hideOptionsDialog();
		} else {
			showOptionsDialog();
		}
	}

	private void showOptionsDialog() {
		releaseMouse();
		player.clearKeys();

		// if this is the first time, create the options panel.
		if (optionsDialog == null) {
			optionsDialog = createOptionsDialog();
		}

		optionsDialog.setLocationRelativeTo(this);
		optionsDialog.setVisible(true);
	}

	private void hideOptionsDialog() {
		if (optionsDialog != null) {
			optionsDialog.setVisible(false);
		}

		view.requestFocusInWindow();

		// reset the aim correctly if in topdown mode.
		if (view.isTopDownMode()) {
			syncTopDownAimToPlayerAngle();
			captureMouse();
		}
	}

	private boolean isOptionsDialogOpen() {
		return optionsDialog != null && optionsDialog.isVisible();
	}

	private JDialog createOptionsDialog() {
		final JDialog dialog = new JDialog(this, "Options", false);
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		final JLabel sensitivity3DLabel = new JLabel();
		final JSlider sensitivity3DSlider = new JSlider(1, 200, (int)Math.round(mouseSensitivity3D * 10000));
		sensitivity3DSlider.setMajorTickSpacing(10);
		sensitivity3DSlider.setMinorTickSpacing(1);
		sensitivity3DSlider.setPaintTicks(true);
		updateSensitivity3DLabel(sensitivity3DLabel);
		sensitivity3DSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				mouseSensitivity3D = sensitivity3DSlider.getValue() / 10000.0;
				updateSensitivity3DLabel(sensitivity3DLabel);
			}
		});

		final JLabel sensitivityTopDownLabel = new JLabel();
		final JSlider sensitivityTopDownSlider = new JSlider(1, 200, (int)Math.round(mouseSensitivityTopDown * 10000));
		sensitivityTopDownSlider.setMajorTickSpacing(10);
		sensitivityTopDownSlider.setMinorTickSpacing(1);
		sensitivityTopDownSlider.setPaintTicks(true);
		updateSensitivityTopDownLabel(sensitivityTopDownLabel);
		sensitivityTopDownSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				mouseSensitivityTopDown = sensitivityTopDownSlider.getValue() / 10000.0;
				updateSensitivityTopDownLabel(sensitivityTopDownLabel);
			}
		});

		final JLabel fovLabel = new JLabel();
		final JSlider fovSlider = new JSlider(45, 120, cast.getFovDegrees());
		fovSlider.setMajorTickSpacing(15);
		fovSlider.setMinorTickSpacing(5);
		fovSlider.setPaintTicks(true);
		fovSlider.setPaintLabels(true);
		updateFovLabel(fovLabel);
		fovSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				cast.setFovDegrees(fovSlider.getValue());
				updateFovLabel(fovLabel);
				view.repaint();
			}
		});

		panel.add(sensitivity3DLabel);
		panel.add(sensitivity3DSlider);
		panel.add(sensitivityTopDownLabel);
		panel.add(sensitivityTopDownSlider);
		panel.add(fovLabel);
		panel.add(fovSlider);

		registerOptionsKeys(dialog);
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setResizable(false);

		return dialog;
	}

	private void registerOptionsKeys(final JDialog dialog) {
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "toggleOptions");
		dialog.getRootPane().getActionMap().put("toggleOptions", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleOptionsDialog();
			}
		});

		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hideOptions");
		dialog.getRootPane().getActionMap().put("hideOptions", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hideOptionsDialog();
			}
		});
	}

	private void updateSensitivity3DLabel(final JLabel label) {
		label.setText("2.5D mouse sensitivity: " + String.format("%.4f", mouseSensitivity3D));
	}

	private void updateSensitivityTopDownLabel(final JLabel label) {
		label.setText("Top-down mouse sensitivity: " + String.format("%.4f", mouseSensitivityTopDown));
	}

	private void updateFovLabel(final JLabel label) {
		label.setText("Field of view: " + cast.getFovDegrees() + " deg");
	}

	private void syncTopDownAimToPlayerAngle() {
		topDownAimX = Math.cos(player.getPlayerAngle()) * topDownAimRadius;
		topDownAimY = Math.sin(player.getPlayerAngle()) * topDownAimRadius;
	}

	private void updateTopDownAim(final int dx, final int dy) {
		final double virtualMouseScale = mouseSensitivityTopDown * 1000;
		topDownAimX += dx * virtualMouseScale;
		topDownAimY += dy * virtualMouseScale;

		final double length = Math.sqrt(topDownAimX * topDownAimX + topDownAimY * topDownAimY);
		if (length < 0.0001) { // fixes some weirdness.
			syncTopDownAimToPlayerAngle();
			return;
		}

		topDownAimX = topDownAimX / length * topDownAimRadius;
		topDownAimY = topDownAimY / length * topDownAimRadius;
		player.setAngle(Math.atan2(topDownAimY, topDownAimX));
	}

	private Robot createRobot() {
		try {
			return new Robot();
		} catch (AWTException e) {
			return null;
		}
	}

	private Cursor createHiddenCursor() {
		final BufferedImage cursorImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "hidden cursor");
	}

	private void captureMouse() {
		if (robot == null || mouseCaptured || isOptionsDialogOpen()) {
			return;
		}

		if (view.isTopDownMode()) {
			syncTopDownAimToPlayerAngle();
		}
		mouseCaptured = true;
		view.setCursor(hiddenCursor);
		view.requestFocusInWindow();
		centerMouse();
	}

	private void releaseMouse() {
		if (!mouseCaptured) {
			return;
		}

		mouseCaptured = false;
		ignoringRobotMove = false;
		view.setCursor(defaultCursor);
	}

	private void handleMouseLook(final MouseEvent e) {
		if (!mouseCaptured || view.isTopDownMode()) {
			return;
		}

		final int centerX = view.getWidth() / 2;
		final int centerY = view.getHeight() / 2;

		if (ignoringRobotMove && e.getX() == centerX && e.getY() == centerY) {
			ignoringRobotMove = false;
			return;
		}
		ignoringRobotMove = false;

		final int dx = e.getX() - centerX;
		final int dy = e.getY() - centerY;
		if (dx == 0 && dy == 0) {
			return;
		}

		player.rotate(dx * mouseSensitivity3D);
		centerMouse();
	}

	private void handleTopDownMouseLook(final MouseEvent e) {
		if (!mouseCaptured || !view.isTopDownMode()) {
			return;
		}

		final int centerX = view.getWidth() / 2;
		final int centerY = view.getHeight() / 2;

		if (ignoringRobotMove && e.getX() == centerX && e.getY() == centerY) {
			ignoringRobotMove = false;
			return;
		}
		ignoringRobotMove = false;

		final int dx = e.getX() - centerX;
		final int dy = e.getY() - centerY;
		if (dx == 0 && dy == 0) {
			return;
		}

		updateTopDownAim(dx, dy);
		centerMouse();
	}

	private void centerMouse() {
		if (robot == null || !view.isShowing()) {
			return;
		}

		final Point center = new Point(view.getWidth() / 2, view.getHeight() / 2);
		SwingUtilities.convertPointToScreen(center, view);
		ignoringRobotMove = true;
		robot.mouseMove(center.x, center.y);
	}

	/**
	 * Starts the engine.
	 */
	public static void main(String[] args) {
		new Control();
	}
}
