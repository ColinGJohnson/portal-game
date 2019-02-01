public class playerEntity extends entity {
	private boolean jumping = false;
	private int playerHeight = 0;
	private int playerWidth = 0;
	private double airDeceleration = 0.015;

	public playerEntity(double xPos, double yPos, int playerHeight, int playerWidth) {
		super(xPos, yPos, 0, 0, 0, 0);

		this.playerHeight = playerHeight;
		this.playerWidth = playerWidth;
	}

	public void jump(int mapScale, int[][] terrain) {
		if (!jumping && onGround(mapScale, terrain)) {
			jumping = true;
			setVy(getVy() - 0.6);
		}
	}
	
	public boolean onGround(int mapScale, int[][] terrain){
		return terrain[(int) ((getxPos() + playerWidth/2) / mapScale)][(int) ((getyPos() + playerHeight)/ mapScale) + 1] != -1 ? true : false;
	}

	public void updateMovementX(int delta, boolean aPressed, boolean dPressed, int mapScale, int[][] terrain) {

		// set X acceleration depending on input
		if (dPressed && aPressed) {
			setAx(0);
		} else {
			if (dPressed) {
				setAx(jumping ? 0.01 : 0.015);
			}
			if (aPressed) {
				setAx(jumping ? -0.01 : -0.015);
			}
		}

		// stop player when not controlled and on ground or slow them in the air
		if (!aPressed && !dPressed) {
			if (terrain[(int) ((getxPos() + playerWidth/2) / mapScale)][(int) ((getyPos() + playerHeight)/ mapScale) + 1] != -1) {
				setVx(0);
				setAx(0);
			} else if (getVx() > airDeceleration || getVx() < -airDeceleration) {
				setVx(getVx() - Math.signum(getVx()) * airDeceleration);
			} else {
				setVx(0);
				setAx(0);
			}
		} 

		// apply acceleration to velocities
		setVx(getVx() + getAx());

		// terminal velocity
		if (getVx() > 1) {
			setVx(1);
		} else if (getVx() < -1) {
			setVx(-1);
		}
		
		// collision detection
		boolean collision = false;
		if (getVx() > 0) {
			for (double i = 0; i < getVx(); i += 0.01) {
				if (	   terrain[(int) (getxPos() + playerWidth + i * delta) / mapScale][(int) (getyPos()) / mapScale] != -1
						|| terrain[(int) (getxPos() + playerWidth + i * delta)/ mapScale][(int) (getyPos() + playerHeight) / mapScale] != -1			
						|| terrain[(int) (getxPos() + i * delta) / mapScale][(int) (getyPos()) / mapScale] != -1
					    || terrain[(int) (getxPos() + i * delta) / mapScale][(int) (getyPos() + playerHeight)/ mapScale] != -1){
					setVx(i - 0.02);
					break;
				}
			}
		} else if (getVx() < 0){
			for (double i = 0; i > getVx(); i -= 0.01) {
				if (	   terrain[(int) (getxPos() + playerWidth + i * delta) / mapScale][(int) (getyPos()) / mapScale] != -1
						|| terrain[(int) (getxPos() + playerWidth + i * delta)/ mapScale][(int) (getyPos() + playerHeight) / mapScale] != -1			
						|| terrain[(int) (getxPos() + i * delta) / mapScale][(int) (getyPos()) / mapScale] != -1
					    || terrain[(int) (getxPos() + i * delta) / mapScale][(int) (getyPos() + playerHeight)/ mapScale] != -1){
					setVx(i + 0.02);
					break;
				}
			}
		}
		
		
		// apply velocity to position
		if (!collision) {
			setxPos(getxPos() + delta * getVx());
		}
		
	}

	public void updateMovementY(int delta, boolean wPressed, int mapScale, int[][] terrain) {

		// gravity
		setAy(0.025);

		// apply acceleration to velocities
		setVy(getVy() + getAy());

		// terminal velocity
		if (getVy() > 2) {
			setVy(2);
		} else if (getVy() < -2) {
			setVy(-2);
		}
		
		// collision detection
		if (getVy() > 0) {
			for (double i = 0; i < getVy(); i += 0.01) {
				if (terrain[(int) (getxPos()) / mapScale][(int) (getyPos() + playerHeight + i * delta) / mapScale] != -1
						|| terrain[(int) (getxPos() + playerWidth)/ mapScale][(int) (getyPos() + playerHeight + i * delta) / mapScale] != -1) {
					jumping = false;
					setVy(i - 0.04);
					break;
				}
			}
		} else if (getVy() < 0) {
			for (double i = 0; i > getVy(); i -= 0.01) {
				if (terrain[(int) (getxPos()) / mapScale][(int) (getyPos() + i * delta) / mapScale] != -1
						|| terrain[(int) (getxPos() + playerWidth) / mapScale][(int) (getyPos() + i * delta)
								/ mapScale] != -1) {
					setVy(i + 0.04);
					break;
				}
			}
		}

		// apply velocity to position
		setyPos(getyPos() + delta * getVy());
	}

	public boolean isJumping() {
		return jumping;
	}

	public void setJumping(boolean jumping) {
		this.jumping = jumping;
	}

	public void moveLeft() {
		setAx(-0.05);
	}

	public void moveRight() {
		setAx(0.05);
	}

	public void resetPosition() {
		setxPos(525);
		setyPos(525);
		setVx(0);
		setVx(0);
	}
}
