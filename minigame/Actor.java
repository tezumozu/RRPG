package minigame;

import javafx.scene.image.*;
import javafx.scene.canvas.*;
import java.util.*;
public class Actor {
	public Image image;
	protected int posX;
	protected int posY;
	protected boolean isEdible = false;
	protected static String myTurn = "aaaa";//自分のターンを実行する権利
	static List<Actor> actors = Collections.synchronizedList(new ArrayList<>());
	
	public Actor(Image image, int posX, int posY,boolean isEdible) {
		this.image = image;
		this.posX = posX;
		this.posY = posY;
		this.isEdible = isEdible;
		Actor.actors.add(this);
	}

	static public void paintActors(GraphicsContext gc) {
		synchronized (Actor.actors) {
			for (Actor actor : Actor.actors) {
				actor.draw(gc);
			}
		}
	}
	public void draw(GraphicsContext gc) {
		gc.drawImage(this.image,0,0,this.image.getWidth(),this.image.getHeight(),this.posX * Game.TILESIZE, this.posY * Game.TILESIZE,Game.TILESIZE,Game.TILESIZE);
	}
	
	synchronized public boolean moves(int x,int y) {
		ItemActor item = null;
		if(x==0 && y==0) {//自分で自分を食べてしまうので
			return false;
		}
		Actor target = existsAt(posX+x, posY+y); 
		if(target != null) {
			if(target != null) {
				if(target.isEdible) {
				}else {
					return false;
				}
			}
		}
		if(Game.dungeon[posX+x][posY+y] != MapMaker.DObject.YUKA && Game.dungeon[posX+x][posY+y] != MapMaker.DObject.EXIT ) {
			return false;
		}
		
		this.posX = posX+x;
		this.posY = posY+y;
		return true;
	}
	
	
	public void eaten() {
		if (this.isEdible) {
			synchronized (Actor.actors) {
				this.delete();
			}
		}
	}
	
	
	public Actor delete() {
		Actor.actors.remove(this);
		if (this instanceof Character) {
			((Enemy) this).alive = false;
		}
		return this;
	}
	
	
	static public Actor existsAt(int x, int y) {
		for(Actor a: actors) {
			if(a.posX == x && a.posY == y) {
				return a;
			}
		}
		return null;
	}
	
	public static void characterStop(int milliTime) {//スレッドを止めずに１秒待つ
		Thread t = new Thread(()-> {
			while(true) {
				try {
					Thread.sleep(milliTime);
					break;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
			}
		});
		
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
	}
	
	public static void playEnemyTurn() {
		synchronized(myTurn){
			myTurn.notifyAll();
		}
	}
}
