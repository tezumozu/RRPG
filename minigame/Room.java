package minigame;

public class Room {
	private int x;
	private int y;
	private int w;
	private int h;
	private boolean isRoom = true;
	
	Room(int x,int y,int w,int h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		if(h == 1 || w == 1) {//wかhが1なら通路
			this.isRoom = false;
		}
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getW() {
		return w;
	}
	public int getH() {
		return h;
	}
	public boolean isRoom() {
		return isRoom;
	}
}
