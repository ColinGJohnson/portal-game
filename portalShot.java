import java.awt.Point;

public class portalShot extends entity {
	private boolean isRedPortal;
	private float shotAngle;
	private boolean used = false;
	private float collisonAccuracy = 0.01f;

	public portalShot(double xPos, double yPos, double clickX, double clickY, boolean isRedPortal) {
		super(xPos + 10, yPos + 10, 0, 0, 0, 0);
		xPos += 10;
		yPos += 20;
		this.setRedPortal(isRedPortal);

		float dx = (float) (clickX - xPos), dy = (float) (clickY - yPos);
		float distance = (float) Math.sqrt(dx * dx + dy * dy);

		setVx(dx * 1 / distance);
		setVy(dy * 1 / distance);
	}

	public void update(int delta, int terrain[][], int mapScale, portal redPortal, portal bluePortal) {
		if (updateMovementX(delta, terrain, mapScale)) {
			used = true;
			tryPlacePortal((int) getxPos(), (int) getyPos(), terrain, "x", redPortal, bluePortal);
		}
		if (updateMovementY(delta, terrain, mapScale)) {
			used = true;
			tryPlacePortal((int) getxPos(), (int) getyPos(), terrain, "y", redPortal, bluePortal);
		}
	}

	// places a portal if there is enough room
	public void tryPlacePortal(int originX, int originY, int terrain[][], String direction, portal redPortal, portal bluePortal) {
		System.out.println("Attempting to place a portal");
		
		if (getVx() > 0 && direction.equals("x")) {
			if (       terrain[toTileMap(originX) + 1][toTileMap(originY)   ] != -1
					&& terrain[toTileMap(originX) + 1][toTileMap(originY)+ 1] != -1
					&& terrain[toTileMap(originX) + 1][toTileMap(originY)- 1] != -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY)+ 1] == -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY)- 1] == -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY)   ] == -1) {
				System.out.println("Area valid for left facing portal placement.");
				if (isRedPortal) {
					redPortal.setDirection("left");
					placePortal(redPortal, originX, originY);
				} else {
					bluePortal.setDirection("left");
					placePortal(bluePortal, originX, originY);
				}
			} else {
				System.out.println("Area invalid for portal placement.");
			}
		} else if (getVx() < 0 && direction.equals("x")) {
			if (       terrain[toTileMap(originX) - 1][toTileMap(originY)   ] != -1
					&& terrain[toTileMap(originX) - 1][toTileMap(originY)- 1] != -1
					&& terrain[toTileMap(originX) - 1][toTileMap(originY)+ 1] != -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY)- 1] == -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY)+ 1] == -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY)   ] == -1) {
				System.out.println("Area valid for left facing portal placement.");
				if (isRedPortal) {
					redPortal.setDirection("right");
					placePortal(redPortal, originX, originY);
				} else {
					bluePortal.setDirection("right");
					placePortal(bluePortal, originX, originY);
				}
			} else {
				System.out.println("Area invalid for portal placement.");
			}
		} else if (getVy() > 0 && direction.equals("y")) {
			if (       terrain[toTileMap(originX) + 1][toTileMap(originY) + 1] != -1
					&& terrain[toTileMap(originX) - 1][toTileMap(originY) + 1] != -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY) + 1] != -1
					&& terrain[toTileMap(originX) + 1][toTileMap(originY)    ] == -1
					&& terrain[toTileMap(originX) - 1][toTileMap(originY)    ] == -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY)    ] == -1
					) {
				System.out.println("Area valid for left facing portal placement.");
				if (isRedPortal) {
					redPortal.setDirection("up");
					placePortal(redPortal, originX, originY);					
				} else {
					bluePortal.setDirection("up");
					placePortal(bluePortal, originX, originY);
				}
			} else {
				System.out.println("Area invalid for portal placement.");
			}
		} else {
			if (       terrain[toTileMap(originX) + 1][toTileMap(originY) - 1] != -1
					&& terrain[toTileMap(originX) - 1][toTileMap(originY) - 1] != -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY) - 1] != -1
					&& terrain[toTileMap(originX) + 1][toTileMap(originY)    ] == -1
					&& terrain[toTileMap(originX) - 1][toTileMap(originY)    ] == -1
					&& terrain[toTileMap(originX)    ][toTileMap(originY)    ] == -1
					) {
				System.out.println("Area valid for left facing portal placement.");
				if (isRedPortal) {
					redPortal.setDirection("down");
					placePortal(redPortal, originX, originY);					
				} else {
					bluePortal.setDirection("down");
					placePortal(bluePortal, originX, originY);
				}
			} else {
				System.out.println("Area invalid for portal placement.");
			}
		}
	}

	public void placePortal(portal toPlace, int originX, int originY) {
		toPlace.setActive(true);
		toPlace.setxPos(originX);
		toPlace.setyPos(originY + 20);
		if (isRedPortal) {
			System.out.println("Placed red portal.");
		} else {
			System.out.println("Placed blue portal.");
		}

	}

	public boolean updateMovementX(int delta, int terrain[][], int mapScale) {
		if ((getxPos() + delta * getVx()) < 0 || (getxPos() + delta * getVx()) > terrain.length * mapScale) {
			used = true;
		} else {

			// collision detection
			if (getVx() > 0) {
				for (double i = 0; i < getVx(); i += collisonAccuracy) {
					if (terrain[(int) (getxPos() + i * delta) / mapScale][(int) (getyPos()) / mapScale] != -1) {

						// safe move
						setxPos(getxPos() + (i - collisonAccuracy) * delta);
						return true;
					}
				}
			} else if (getVx() < 0) {
				for (double i = 0; i > getVx(); i -= collisonAccuracy) {
					if (terrain[(int) (getxPos() + i * delta) / mapScale][(int) (getyPos()) / mapScale] != -1) {

						// safe move
						setxPos(getxPos() + (i + collisonAccuracy) * delta);
						return true;
					}
				}
			}

			// full move
			setxPos(getxPos() + delta * getVx());
		}

		return false;
	}

	public boolean updateMovementY(int delta, int terrain[][], int mapScale) {
		if ((getyPos() + delta * getVy()) < 0 || (getyPos() + delta * getVy()) > terrain.length * mapScale) {
			used = true;
		} else {

			// collision detection
			if (getVy() > 0) {
				for (double i = 0; i < getVy(); i += collisonAccuracy) {
					if (terrain[(int) (getxPos()) / mapScale][(int) (getyPos() + i * delta) / mapScale] != -1) {
						
						// safe move
						setyPos(getyPos() + (i - collisonAccuracy) * delta);
						return true;
					}
				}
			} else if (getVy() < 0) {
				for (double i = 0; i > getVy(); i -= collisonAccuracy) {
					if (terrain[(int) (getxPos()) / mapScale][(int) (getyPos() + i * delta) / mapScale] != -1) {
						
						// safe move
						setyPos(getyPos() + (i + collisonAccuracy) * delta);
						return true;
					}
				}
			}

			// full move
			setyPos(getyPos() + delta * getVy());
		}

		return false;
	}

	public boolean isRedPortal() {
		return isRedPortal;
	}

	public void setRedPortal(boolean isRedPortal) {
		this.isRedPortal = isRedPortal;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

}
