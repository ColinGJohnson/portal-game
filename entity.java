public class entity {
	private double xPos = 0;
	private double yPos = 0;
	private double Vx = 0;
	private double Vy = 0;
	private double Ax = 0;
	private double Ay = 0;
	
	public entity(double xPos, double yPos, double Vx, double Vy, double Ax, double Ay) {
		this.setxPos(xPos);
		this.setyPos(yPos);
		this.setVx(Vx);
		this.setVy(Vy);
		this.setAx(Ax);
		this.setAy(Ay);
	}
	
	public int toTileMap(double cord){
		return (int) (cord/20); 
	}

	public double getxPos() {
		return xPos;
	}

	public void setxPos(double xPos) {
		this.xPos = xPos;
	}

	public double getyPos() {
		return yPos;
	}

	public void setyPos(double yPos) {
		this.yPos = yPos;
	}

	public double getVx() {
		return Vx;
	}

	public void setVx(double vx) {
		Vx = vx;
	}

	public double getVy() {
		return Vy;
	}

	public void setVy(double vy) {
		Vy = vy;
	}

	public double getAx() {
		return Ax;
	}

	public void setAx(double ax) {
		Ax = ax;
	}

	public double getAy() {
		return Ay;
	}

	public void setAy(double ay) {
		Ay = ay;
	}
}
