import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class GameWindow implements Runnable {

	// display variables
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	final int WIDTH = (int) screenSize.getWidth();
	final int HEIGHT = (int) screenSize.getHeight();
	JFrame frame;
	Canvas canvas;
	BufferStrategy bufferStrategy;

	// tile editing variables
	ArrayList<EditRequest> EditRequests = new ArrayList<EditRequest>();
	private int currentTile;
	private int mapScale = 20; // pixels per grid square
	private int mapSize = 100; // grid square dimensions of the map
	private int[][] terrain = new int[mapSize][mapSize]; // map tile data

	// portal stuff
	portal redPortal = new portal();
	portal bluePortal = new portal();
	ArrayList<portalShot> portalShots = new ArrayList<portalShot>();
	long portalCooldown = 0;

	// user control variables
	playerEntity player = new playerEntity(525, 525, 54, 20);
	public boolean wPressed = false;
	public boolean aPressed = false;
	public boolean sPressed = false;
	public boolean dPressed = false;
	public boolean spacePressed = false;
	public boolean showDebug = false;

	// Collision detection variables
	int maxFixLoops = 50;

	// game state variables
	private boolean editingMode = false;

	// images
	Image playerSprite = null;
	Image aperatureLogo = null;
	Image bluePortalUpSprite = null;
	Image orangePortalUpSprite = null;
	Image bluePortalDownSprite = null;
	Image orangePortalDownSprite = null;
	Image bluePortalLeftSprite = null;
	Image orangePortalLeftSprite = null;
	Image bluePortalRightSprite = null;
	Image orangePortalRightSprite = null;

	public GameWindow() {
		frame = new JFrame("Java Portal");

		JPanel panel = (JPanel) frame.getContentPane();
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		panel.setLayout(null);

		canvas = new Canvas();
		canvas.setBounds(0, 0, WIDTH, HEIGHT);
		canvas.setIgnoreRepaint(true);

		panel.add(canvas);

		canvas.addMouseListener(new MouseControl());
		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					editingMode = !editingMode;
					System.out.println("toggled editing mode to " + editingMode);
				}

				if (e.getKeyCode() >= 48 && e.getKeyCode() <= 57 && editingMode) {
					currentTile = e.getKeyCode() - 48;
					System.out.println("changed current tile to " + currentTile);
				}

				if (e.getKeyCode() == KeyEvent.VK_W) {
					wPressed = false;
				}

				if (e.getKeyCode() == KeyEvent.VK_A) {
					aPressed = false;
				}

				if (e.getKeyCode() == KeyEvent.VK_S) {
					sPressed = false;
				}

				if (e.getKeyCode() == KeyEvent.VK_D) {
					dPressed = false;
				}

				if (e.getKeyCode() == 93) {
					System.out.println("Toggled debug mode.");
					showDebug = !showDebug;
				}

				if (e.getKeyCode() == KeyEvent.VK_R) {
					player.resetPosition();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_W) {
					wPressed = true;
				}

				if (e.getKeyCode() == KeyEvent.VK_A) {
					aPressed = true;
				}

				if (e.getKeyCode() == KeyEvent.VK_S) {
					sPressed = true;
				}

				if (e.getKeyCode() == KeyEvent.VK_D) {
					dPressed = true;
				}
			}
		});

		frame.setUndecorated(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);

		canvas.createBufferStrategy(2);
		bufferStrategy = canvas.getBufferStrategy();

		canvas.requestFocus();
	}

	private class MouseControl extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);

			if (SwingUtilities.isLeftMouseButton(e) && editingMode) {
				EditRequests.add(new EditRequest(e.getX(), e.getY(), currentTile));
			}

			if (SwingUtilities.isRightMouseButton(e) && !editingMode) {
				portalShots.add(new portalShot(player.getxPos(), player.getyPos(), e.getX(), e.getY(), true));
				System.out.println("Fired red portal.");
			}

			if (SwingUtilities.isLeftMouseButton(e) && !editingMode) {
				portalShots.add(new portalShot(player.getxPos(), player.getyPos(), e.getX(), e.getY(), false));
				System.out.println("Fired blue portal.");
			}
		}
	}

	long desiredFPS = 60;
	long desiredDeltaLoop = (1000 * 1000 * 1000) / desiredFPS;
	boolean running = true;

	public void run() {
		long beginLoopTime;
		long endLoopTime;
		long currentUpdateTime = System.nanoTime();
		long lastUpdateTime;
		long deltaLoop;

		init();

		while (running) {
			beginLoopTime = System.nanoTime();

			render();

			lastUpdateTime = currentUpdateTime;
			currentUpdateTime = System.nanoTime();
			update((int) ((currentUpdateTime - lastUpdateTime) / (1000 * 1000)));

			endLoopTime = System.nanoTime();
			deltaLoop = endLoopTime - beginLoopTime;

			if (deltaLoop > desiredDeltaLoop) {
				// Do nothing. We are already late.
			} else {
				try {
					Thread.sleep((desiredDeltaLoop - deltaLoop) / (1000 * 1000));
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
		}
	}

	private void init() {

		// clear map
		for (int i = 0; i < terrain.length; i++) {
			for (int j = 0; j < terrain[i].length; j++) {
				terrain[i][j] = -1;
			}
		}
		terrain[26][30] = 0;
		terrain[27][30] = 0;
		terrain[28][30] = 0;
		terrain[29][30] = 0;
		terrain[30][30] = 0;
		terrain[31][30] = 0;
		terrain[32][30] = 0;
		terrain[33][30] = 0;
		terrain[34][30] = 0;
		terrain[35][30] = 0;
		terrain[36][30] = 0;

		// load images
		try {
			playerSprite = ImageIO.read(GameWindow.class.getResource("/resources/bendy.png"));
			aperatureLogo = ImageIO.read(GameWindow.class.getResource("/resources/aperatureLogo.png"));
			bluePortalUpSprite = ImageIO.read(GameWindow.class.getResource("/resources/bluePortalUp.png"));
			orangePortalUpSprite = ImageIO.read(GameWindow.class.getResource("/resources/orangePortalUp.png"));
			bluePortalDownSprite = ImageIO.read(GameWindow.class.getResource("/resources/bluePortalDown.png"));
			orangePortalDownSprite = ImageIO.read(GameWindow.class.getResource("/resources/orangePortalDown.png"));
			bluePortalLeftSprite = ImageIO.read(GameWindow.class.getResource("/resources/bluePortalLeft.png"));
			orangePortalLeftSprite = ImageIO.read(GameWindow.class.getResource("/resources/orangePortalLeft.png"));
			bluePortalRightSprite = ImageIO.read(GameWindow.class.getResource("/resources/bluePortalRight.png"));
			orangePortalRightSprite = ImageIO.read(GameWindow.class.getResource("/resources/orangePortalRight.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void render() {
		Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
		g.clearRect(0, 0, WIDTH, HEIGHT);
		render(g);
		g.dispose();
		bufferStrategy.show();
	}

	// GAME LOGIC BELOW
	protected void update(int deltaTime) {

		// terrain edit requests
		if (editingMode) {
			for (int i = 0; i < EditRequests.size(); i++) {
				terrain[EditRequests.get(i).getScreenX() / mapScale][EditRequests.get(i).getScreenY()
						/ mapScale] = EditRequests.get(i).getTileType();
				System.out.println("Assigned tile #" + EditRequests.get(i).getTileType() + " to terrain position "
						+ EditRequests.get(i).getScreenX() / mapScale + ","
						+ EditRequests.get(i).getScreenY() / mapScale);
			}

			// clear edit requests
			EditRequests.clear();
		} else {

			// check collisions with portals
			if (new Rectangle((int) player.getxPos(), (int) player.getyPos(), playerSprite.getWidth(null),
					playerSprite.getHeight(null)).intersects(redPortal.getHitBox()) && bluePortal.isActive()
					&& redPortal.isActive()) {
				teleport(player, redPortal, bluePortal);
			} else if (new Rectangle((int) player.getxPos(), (int) player.getyPos(), playerSprite.getWidth(null),
					playerSprite.getHeight(null)).intersects(bluePortal.getHitBox()) && redPortal.isActive()
					&& bluePortal.isActive()) {
				teleport(player, bluePortal, redPortal);
			}

			// player movement controls
			if (wPressed) {
				player.jump(mapScale, terrain);
			}

			if (aPressed) {
				player.moveLeft();
			}

			if (dPressed) {
				player.moveRight();
				player.setAx(0.05);
			}

			// try to update player position
			try {
				player.updateMovementX(deltaTime, aPressed, dPressed, mapScale, terrain);
				player.updateMovementY(deltaTime, wPressed, mapScale, terrain);			
			} catch (ArrayIndexOutOfBoundsException e) {			
				player.resetPosition();
				System.out.println("Player out of bounds! Position reset.");
			}
			

			// update portalShot Positions
			for (int i = 0; i < portalShots.size(); i++) {
				portalShots.get(i).update(deltaTime, terrain, mapScale, redPortal, bluePortal);
			}

			// remove out of bounds or used portalShots
			for (int i = 0; i < portalShots.size(); i++) {
				if (portalShots.get(i).isUsed()) {
					portalShots.remove(i);
				}
			}
			
			// update portal teleportation cooldown
			portalCooldown = (portalCooldown > 0) ? portalCooldown - deltaTime : 0;
		}
	}

	private void teleport(playerEntity teleportingPlayer, portal used, portal destination) {
		if (portalCooldown == 0) {
			portalCooldown = 100;
			
			teleportingPlayer.setxPos(destination.getxPos());
			teleportingPlayer.setyPos(destination.getyPos());
	
			// correct direction and player speed
			if ((used.getDirection().equals(destination.getDirection())
					&& (used.getDirection().equals("up") || used.getDirection().equals("down")))) {
				teleportingPlayer.setVy(-teleportingPlayer.getVy());
			}
	
			if ((used.getDirection().equals(destination.getDirection())
					&& (used.getDirection().equals("left") || used.getDirection().equals("right")))) {
				teleportingPlayer.setVx(-teleportingPlayer.getVx());
			}
	
			if ((used.getDirection().equals("up") && destination.getDirection().equals("right"))) {
				double tempVy = teleportingPlayer.getVy();
				double tempVx = teleportingPlayer.getVx();
				teleportingPlayer.setVx(tempVy);
				teleportingPlayer.setVy(tempVx);
			}
			
			if ((used.getDirection().equals("up") && destination.getDirection().equals("left"))) {
				double tempVy = teleportingPlayer.getVy();
				double tempVx = teleportingPlayer.getVx();
				teleportingPlayer.setVx(-tempVy);
				teleportingPlayer.setVy(tempVx);
			}
	
			if (destination.getDirection() == "right") {
				teleportingPlayer.setyPos(teleportingPlayer.getyPos() - playerSprite.getHeight(null));
			}
			
			if (destination.getDirection() == "left") {
				teleportingPlayer.setyPos(teleportingPlayer.getyPos() - playerSprite.getHeight(null));
				teleportingPlayer.setxPos(teleportingPlayer.getxPos() - playerSprite.getWidth(null));
			}
	
			if (destination.getDirection() == "up") {
				teleportingPlayer.setyPos(teleportingPlayer.getyPos() - playerSprite.getHeight(null) - 25);
			}
		}
	} // teleport

	protected void render(Graphics2D g) {

		// draw aperature logo with reduced opacity
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		g.setComposite(ac);
		g.drawImage(aperatureLogo,
				(int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - aperatureLogo.getWidth(null) / 2,
				(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - aperatureLogo.getHeight(null) / 2,
				null);
		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
		g.setComposite(ac);

		// draw portal shots
		for (int i = 0; i < portalShots.size(); i++) {
			if (portalShots.get(i).isRedPortal()) {
				g.setColor(Color.orange);
			} else {
				g.setColor(Color.blue);
			}

			g.fillOval((int) portalShots.get(i).getxPos() - 5, (int) portalShots.get(i).getyPos() - 5, 10, 10);
		}

		// draw player
		g.drawImage(playerSprite, (int) player.getxPos(), (int) player.getyPos(), null);

		// draw debug info
		if (showDebug) {

			// player hitbox
			g.setColor(Color.green);
			g.drawRect((int) player.getxPos(), (int) player.getyPos(), playerSprite.getWidth(null),
					playerSprite.getHeight(null));

			g.setColor(Color.red);

			// bottom left
			g.fillRect((int) (player.getxPos()), (int) (player.getyPos() + playerSprite.getHeight(null)), 4, 4);

			// bottom right
			g.fillRect((int) (player.getxPos() + playerSprite.getWidth(null)), (int) (player.getyPos() + 55), 4, 4);

			// top left
			g.fillRect((int) (player.getxPos()), (int) (player.getyPos()), 4, 4);

			// top right
			g.fillRect((int) (player.getxPos()) + playerSprite.getWidth(null), (int) (player.getyPos()), 4, 4);

			// portal hitboxes
			g.setColor(Color.red);
			g.drawRect((int) bluePortal.getHitBox().getX(), (int) bluePortal.getHitBox().getY(),
					(int) bluePortal.getHitBox().getWidth(), (int) bluePortal.getHitBox().getHeight());
			g.drawRect((int) redPortal.getHitBox().getX(), (int) redPortal.getHitBox().getY(),
					(int) redPortal.getHitBox().getWidth(), (int) redPortal.getHitBox().getHeight());
		}

		// draw terrain grid
		if (editingMode) {
			g.setColor(Color.lightGray);
			for (int i = 0; i < mapSize; i++) {
				g.drawLine(0, i * mapScale, mapScale * mapSize, i * mapScale);
				g.drawLine(i * mapScale, 0, i * mapScale, mapScale * mapSize);
			}
		}

		// draw tiles
		g.setColor(Color.black);
		for (int i = 0; i < terrain.length; i++) {
			for (int j = 0; j < terrain[i].length; j++) {
				if (terrain[i][j] != -1) {
					g.fillRect(i * mapScale, j * mapScale, mapScale, mapScale);
				}
			}
		}

		Image portalSprite = null; // stores portal sprite to be drawn
		int compX = 0; // X position compensation for portal drawing and hit box
		int compY = 0; // Y position compensation for portal drawing and hit box

		// draw Portals if active
		if (redPortal.isActive()) {
			if (redPortal.getDirection() == "up") {
				compY = -29;
				compX = -20;
				portalSprite = orangePortalUpSprite;
			} else if (redPortal.getDirection() == "down") {
				compY = -20;
				compX = -20;
				portalSprite = orangePortalDownSprite;
			} else if (redPortal.getDirection() == "left") {
				compY = -40;
				compX = -9;
				portalSprite = orangePortalLeftSprite;
			} else {
				compY = -40;
				compX = 0;
				portalSprite = orangePortalRightSprite;
			}

			// Drawing the rotated image at the required drawing locations
			g.drawImage(portalSprite, (int) redPortal.getxPos() + compX, (int) redPortal.getyPos() + compY, null);
			redPortal.setHitBox(new Rectangle((int) redPortal.getxPos() + compX, (int) redPortal.getyPos() + compY,
					portalSprite.getWidth(null), portalSprite.getHeight(null)));
		}
		if (bluePortal.isActive()) {
			if (bluePortal.getDirection() == "up") {
				compY = -29;
				compX = -20;
				portalSprite = bluePortalUpSprite;
			} else if (bluePortal.getDirection() == "down") {
				compY = -20;
				compX = -20;
				portalSprite = bluePortalDownSprite;
			} else if (bluePortal.getDirection() == "left") {
				compY = -40;
				compX = -9;
				portalSprite = bluePortalLeftSprite;
			} else {
				compY = -40;
				compX = 0;
				portalSprite = bluePortalRightSprite;
			}

			// Drawing the rotated image at the required drawing locations
			g.drawImage(portalSprite, (int) bluePortal.getxPos() + compX, (int) bluePortal.getyPos() + compY, null);
			bluePortal.setHitBox(new Rectangle((int) bluePortal.getxPos() + compX, (int) bluePortal.getyPos() + compY,
					portalSprite.getWidth(null), portalSprite.getHeight(null)));
		}
	}

	public static void main(String[] args) {
		GameWindow ex = new GameWindow();
		new Thread(ex).start();
	}
}