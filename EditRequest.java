public class EditRequest {
	private int screenX = 0;
	private int screenY = 0;
	private int tileType = 0;
	
	public EditRequest(int screenX, int screenY, int tileType) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.setTileType(tileType);
	}

	public int getScreenY() {
		return screenY;
	}

	public void setScreenY(int screenY) {
		this.screenY = screenY;
	}

	public int getScreenX() {
		return screenX;
	}

	public void setScreenX(int screenX) {
		this.screenX = screenX;
	}

	public int getTileType() {
		return tileType;
	}

	public void setTileType(int tileType) {
		this.tileType = tileType;
	}
}
