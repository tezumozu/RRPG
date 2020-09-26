package minigame;

import javafx.scene.image.Image;
public class Character extends Actor {
	enum Direction{
		N,
		S,
		E,
		W,
		Damage,
		Death,
		Attack
	} 
	protected int level = 1;
	protected int currentHP = getMaxHP();
	protected String name = "";
	public Character(Image image, int posX, int posY) {
		super(image, posX, posY, false);
		// TODO Auto-generated constructor stub
	}
	
	public int getLevel() {
		return this.level;
	}
	public int getCurrentHP() {
		return this.currentHP;
	}
	
	public String getName() {
		return name;
	}
	
	public int getAttackPoint() {
		return 7 +((this.level-1)*4);
	}
	public int getDifencePoint() {
		return 7 + ((this.level-1)*4);
	}
	public int getMaxHP() {
		return (18 + (this.level-1)*10);
	}
	
	public boolean damage(Character difence) {
		//return this;
		if(difence.currentHP < 1) {
			return true;
		}
		String br = System.getProperty("line.separator");
		String text = getName() + "の攻撃!" + br;
		
		int attackPoint = this.getAttackPoint();
		int difencePoint = difence.getDifencePoint();
		
		int damagePoint = (int)((attackPoint*2*(1+0.1*(this.level-difence.getLevel())) - difencePoint));
		if(damagePoint < 1) {
			damagePoint = 1;
		}
		
		
		int avoidance = (int)(5*(1+0.2*(difence.getLevel()-this.getLevel())));//回避率
		if(Math.random()*100 < avoidance) {
			text += difence.getName() + "は" + "攻撃を避けた!";
		}else {
			difence.currentHP -= damagePoint;
			text += difence.getName() + "へ " + damagePoint + " のダメージ!";
			if(difence instanceof Player) {
				((Player)difence).damageImage();
			}
		}
		
		
		Game.addText(text);
		characterStop(700);//テキストを描画するため
		if(difence instanceof Player) {
			((Player)difence).nomalImage();
		}
		
		if(difence.currentHP < 1) {//死んだら
			if(difence instanceof Player) {//プレイヤー
				text = difence.getName() + "は倒れた";
				Game.addText(text);
				((Player)difence).deathImage();
				characterStop(700);//描画のため;
			}else {
				difence.delete();
				text = difence.getName() + "は倒れた";
				Game.addText(text);
				characterStop(700);
				Enemy.checkEnemyTurnEnd();//敵を全て倒したらacceptがtrueになる
			}
			return true;
		}
		
		return false;
	} 
	
}
