import java.awt.Rectangle;

public class portal extends entity{
	
	private boolean active = false;
	private double rotation = 0;
	private String direction = "";
	private Rectangle hitBox = new Rectangle(0, 0, 0, 0);
	
	public portal() {
		super(0, 0, 0, 0, 0, 0);
		
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public Rectangle getHitBox() {
		return hitBox;
	}

	public void setHitBox(Rectangle hitBox) {
		this.hitBox = hitBox;
	}
}
