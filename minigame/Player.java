package minigame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;

public class Player extends Character{
	static HashMap<String,Image> imageMap = new HashMap<String,Image>();
	static {
		ArrayList<String> imageList = new ArrayList<String>();
		//リストの取得
		InputStream input = Game.class.getResourceAsStream("Material/Lists/PlayerList.txt");
		BufferedReader din = new BufferedReader(new InputStreamReader(input));

		try {
			String s;
			while((s = din.readLine()) != null) {
				imageList.add(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		imageList.forEach((name)-> {
			Image i = new Image(Game.class.getResourceAsStream("Material/Player/"+name));
			imageMap.put(name , i);
		});
	}

	public int exp = 0;
	public Direction direction = Direction.S;
	private double recovery = 0;
	private Sword sword = (Sword) Game.itemList.get("短剣");
	private Armor armor = (Armor) Game.itemList.get("ワンピース");
	public Player( int posX, int posY) {
		super(imageMap.get("S.png"), posX, posY);
		this.name = "主人公";

		this.level = 1;
		// TODO Auto-generated constructor stub
	}	

	public boolean playTurn(KeyCode code){
		boolean moved = false;
		String text = "";
		switch (code) {
		case SPACE :
			playerAttack();
			moved = true;
			break;
		case UP: // 上
			if(direction == Direction.N) {
				moved = this.moves(0, -1);
			}else {
				direction = Direction.N;
				this.image = imageMap.get("N.png");
			}
			text = "";
			Game.addText(text);
			break;
		case DOWN: // 下
			if(direction == Direction.S) {
				moved =this.moves(0, 1);
			}else {
				direction = Direction.S;
				this.image = imageMap.get("S.png");
			}
			text = "";
			Game.addText(text);
			break;
		case LEFT: //左
			if(direction == Direction.W) {
				moved = this.moves(-1, 0);
			}else {
				direction = Direction.W;
				this.image = imageMap.get("W.png");
			}
			text = "";
			Game.addText(text);
			break;
		case RIGHT: // 右
			if(direction == Direction.E) {
				moved = this.moves(1, 0);
			}else {
				direction = Direction.E;
				this.image = imageMap.get("E.png");
			}
			text = "";
			Game.addText(text);

			break;
		default:
			break;
		}

		if(moved) {
			recovery += (double)getMaxHP()/100;//回復
			if(recovery >= 1) {
				currentHP += (int)recovery;
				if(currentHP > getMaxHP()) {
					currentHP = getMaxHP();
				}
				recovery = 0;
			}
		}
		return moved;
	}

	public void damageImage() {
		switch(direction) {
		case N:
			this.image = imageMap.get("N_D.png");
			break;
		case S:
			this.image = imageMap.get("S_D.png");
			break;
		case E:
			this.image = imageMap.get("E_D.png");
			break;
		case W:
			this.image = imageMap.get("W_D.png");
			break;
		}
	}
	
	public void nomalImage() {
		switch(direction) {
		case N:
			this.image = imageMap.get("N.png");
			break;
		case S:
			this.image = imageMap.get("S.png");
			break;
		case E:
			this.image = imageMap.get("E.png");
			break;
		case W:
			this.image = imageMap.get("W.png");
			break;
		}
	}
	
	public void deathImage() {
		this.image = imageMap.get("DEATH.png");
	}

	public int getAttackPoint() {
		return (int)(super.getAttackPoint()+sword.power);
	}
	public int getDifencePoint() {
		return (int)(super.getDifencePoint() +armor.power);
	}

	public void playerAttack() {
		Actor target = null;
		Enemy enemy = null;
		switch (direction) {
		case N: // 上
			target = existsAtOnryEnemy(posX,posY-1);
			break;
		case S: // 下
			target  = existsAtOnryEnemy(posX,posY+1);
			break;
		case W: //左
			target  = existsAtOnryEnemy(posX-1,posY);
			break;
		case E: // 右
			target  = existsAtOnryEnemy(posX+1,posY);
			break;
		}

		if(target != null) {
			if(target instanceof Enemy) {
				enemy  = (Enemy)target;
				if(damage(enemy)) {
					exp += enemy.level*2;
					if(exp >= getMaxExp(level)) {//レベルが上がる
						levelUP();
					}
					if(enemy instanceof Boss) {
						Game.clear = true;
					}
				}
				
			}
		}
	}

	public int getMaxExp(int level){
		if(level == 0) {
			return 0;
		}
		return getMaxExp(level-1)+level*7;
	}


	public void levelUP() {
		level++;
		currentHP = getMaxHP();
		Game.addText("レベルが "+ level +" に上がった！！");
		characterStop(700);//テキスト描画するため。
	}


	public void changeSword(Sword s) {
		this.sword = s;
	}

	public void changeArmor(Armor a) {
		this.armor = a;
	}

	public String getSword() {
		return this.sword.name + " +" + this.sword.power;
	}
	public String getArmor() {
		return this.armor.name + " +" + this.armor.power;
	}

	synchronized public boolean moves(int x,int y) {
		ItemActor item = null;
		if(x==0 && y==0) {//自分で自分を食べてしまうので
			return false;
		}
		Actor target = existsAt(posX+x, posY+y); 
		if(target != null) {
			if(target.isEdible) {
				if(existsAtOnryEnemy(posX+x,posY+y) == null) {
					item = ((ItemActor)target).get();
				}else {
					return false;
				}
			}else {
				return false;
			}
		}
		if(Game.dungeon[posX+x][posY+y] != MapMaker.DObject.YUKA && Game.dungeon[posX+x][posY+y] != MapMaker.DObject.EXIT ) {
			return false;
		}

		this.posX = posX+x;
		this.posY = posY+y;
		Game.baseX = Game.player.posX-Game.NUMTILE_X/2;
		Game.baseY = Game.player.posY-Game.NUMTILE_Y/2;
		if(item != null) {
			int count = 0;
			for(Menu m:Game.allMenuList) {
				if(m instanceof ItemMenu) {
					count++;
				}
			}
			if(count > 30) {
				Game.addText("持ち物がいっぱいです。");
				Actor.characterStop(700);
			}else {
				Game.addText(Game.itemList.get(item.itemID).name + " を手に入れた");
				Actor.characterStop(700);
			}
		}
		return true;
	}
	
	public Enemy existsAtOnryEnemy(int x,int y) {
		for(Actor a: actors) {
			if(a.posX == x && a.posY == y && a instanceof Enemy) {
				return (Enemy) a;
			}
		}
		return null;
	}
}
