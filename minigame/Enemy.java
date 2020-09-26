package minigame;

import javafx.scene.image.Image;

public class Enemy extends Character{
	public boolean alive = true;
	private boolean playerFound = false;
	private static int enemyCount = 0;//エネミーの個体数
	private static int playedCount = 0; //エネミーが何匹行動したか

	public Enemy(Image image, int posX, int posY,int level,String name) {
		super(image, posX, posY);
		this.start();
		this.level = level;
		this.currentHP = getMaxHP();
		this.name = name;
		findPlayer();
		enemyCount++;
	}

	public void start() {
		new Thread(() -> {
			synchronized(myTurn){
				await();
				while(this.alive) {
					playTurn();
					await();
					
				}
			}
		}).start();
	}

	private void playTurn() {
		findPlayer();	
		if(playerFound) {//プレイヤーを見つけたら
			int dx = Math.abs(Game.player.posX-this.posX);
			int dy = Math.abs(Game.player.posY-this.posY);
			if(dx <= 1 && dy <= 1 && dx+dy != 2) {
				damage(Game.player);
			}else {
				chase();
			}
		}else {
			if(playerFound) {//見つけていたら
				chase();
			}else {
				if(Math.random()*2 > 1) {
					moves((int)(Math.random()*3)-1,0);
				}else {
					moves(0,(int)(Math.random()*3)-1);
				}
			}
		}
		
		playedCount++;
		checkEnemyTurnEnd();
	}
	public static void checkEnemyTurnEnd() {
		if(playedCount == enemyCount) {
			playedCount = 0;
			if(Game.clear) {
				String text = "ゲームクリア！！";
				Game.addText(text);
				characterStop(1000);
				Game.call();
			}else if(Game.player.currentHP < 1) {
				Game.call();
			}else {
				Model.canAccept();
			}
		}
	}
	private void chase() {
		boolean moved = false;
		int dx = Math.abs(Game.player.posX - this.posX);
		int dy = Math.abs(Game.player.posY - this.posY);
		if(dx < dy){
			moved = moves(0,(Game.player.posY-this.posY)/dy);
		}else{
			moved = moves((Game.player.posX-this.posX)/dx,0);
		}
		
		if(!moved) {
			if(Math.abs(Game.player.posX - this.posX) < Math.abs(Game.player.posY - this.posY) && dx != 0){
				moves((Game.player.posX-this.posX)/dx,0);
			}else if(dy != 0){
				moved = moves(0,(Game.player.posY-this.posY)/dy);
			}
		}
	}

	private void findPlayer() {
		for(int i = posX-3; i < posX-3 + 7; i++) {
			for(int j = posY-3; j < posY-3 + 7; j++) {
				if(Game.player.posX == i && Game.player.posY == j) {
					playerFound = true;
					return;
				}
			}
		}
		playerFound = false;
	}
	
	private void await() {
		while (true) {//呼び出されるまで起きない
			try {
				myTurn.wait();
				//System.out.println(count);
				break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
	}

	public Actor delete() {
		Actor.actors.remove(this);
		if (this instanceof Character) {
			((Enemy) this).alive = false;
		}
		enemyCount--;
		return this;
	}

	public static void killAll() {
		enemyCount = 0;
		playedCount = 0;
		synchronized (myTurn) {
			for (Actor actor : Actor.actors) {
				if (actor instanceof Enemy) {
					((Enemy) actor).alive = false;
				}
			}
			myTurn.notifyAll();
		}
	}
}
