package minigame;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import minigame.Game.MenuKind;
import minigame.MapMaker.DObject;

public class Model {
	private static boolean accept = true;
	private static Thread play = new Thread();
	public static void keyPressed(KeyCode key) {
		if(accept) {
			accept = false;//入力を受け付けなくする。処理が終わるまで
			play = new Thread(() -> {
				boolean plyaerMoved = false;
				if(Game.showMenu) {
					plyaerMoved = menu(key);
				}else {
					if(key == KeyCode.TAB) {
						Game.showMenu  = true;
					}else {
						plyaerMoved = Game.player.playTurn(key);
					}
				}
				if(plyaerMoved) {
					if(Game.dungeon[Game.player.posX][Game.player.posY] == DObject.EXIT) {
						Game.showMenu = true;
						Game.currentMenu = Game.exit;
						Game.currentMenuType = Game.MenuKind.Exit;
						getMenuList();
					}
					Enemy.checkEnemyTurnEnd();//もし敵がいないない時のために
					Actor.playEnemyTurn();
				}else {
					canAccept();
				}
				//this.draw(); // 画面再描画
			});
			play.start();
		}
	}

	
	public static void canAccept() {
		accept = true;//次の受付が可能にする。
	}

	
	public static boolean getAccept() {
		return accept;
	}
	
	
	public static void GameEND() {
		Actor.characterStop(1500);
		Platform.runLater(()->{
			Game.stage.setScene(Game.titleScene);
		});
	}




	private static boolean menu(KeyCode key) {
		boolean playerMoved = false;
		switch(key) {
		case TAB:
			Game.addText("");
			if(Game.currentMenuType == Game.MenuKind.Main || Game.currentMenuType == Game.MenuKind.Exit) {
				Game.showMenu = false;
			}else {
				Game.currentMenuType = Game.currentMenu.parentMenuType;
				Game.allMenuList.forEach(m->{
					if(m.menuType == Game.currentMenuType) {
						Game.currentMenu = m;
					}
				});
				getMenuList();
			}
			Game.index = 0;
			break;
		case SPACE:
			if(Game.selectedMenu.menuType == Game.MenuKind.End) {
				if(Game.currentMenuType == Game.MenuKind.Exit) {
					if(Game.selectedMenu.name.equals("降りる")) {
						Game.stepDown();
						accept = true;
					}
				}else {
					if(Game.selectedMenu.name.equals("使う")) {
						playerMoved = ((ItemMenu)Game.currentMenu).use();
					}else {
						if(canTrash()) {
							ItemFactory.makeItem(((ItemMenu)Game.currentMenu).itemID,Game.player.posX,Game.player.posY);
							Game.allMenuList.remove(Game.currentMenu);
						}else {
							Game.addText("アイテムを捨てることができません");
							Actor.characterStop(500);
						}
					}
				}
				Game.addText("");
				Game.showMenu = false;
				Game.index = 0;
				Game.currentMenu = Game.main;
				Game.currentMenuType = Game.main.menuType;
				getMenuList();
			}else {
				Game.currentMenu = Game.selectedMenu;
				Game.currentMenuType = Game.currentMenu.menuType;
				getMenuList();
			}
			break;
			
			
		case UP:
			if(Game.menuList.size() != 0) {
				Game.index = (Game.menuList.size() + (Game.index - 1))%Game.menuList.size();
				Game.selectedMenu = Game.menuList.get(Game.index);
			}

			break;
		case DOWN:
			if(Game.menuList.size() != 0) {
				Game.index = (Game.index+1)%Game.menuList.size();
				Game.selectedMenu = Game.menuList.get(Game.index);
			}

			break;
		case LEFT:
			if(Game.menuList.size() != 0) {
				if(Game.menuList.size() > 15) {
					Game.index = (Game.menuList.size() + (Game.index - 15))%Game.menuList.size();
					Game.selectedMenu = Game.menuList.get(Game.index);
				}
			}
			break;
		case RIGHT:
			if(Game.menuList.size() != 0) {
				if(Game.menuList.size() > 15) {
					Game.index = (Game.index+15)%Game.menuList.size();
					Game.selectedMenu = Game.menuList.get(Game.index);
				}
			}
			break;
		}
		return playerMoved;
	}

	public static void getMenuList() {
		Game.index = 0;
		Game.menuList.removeAll(Game.menuList);
		Game.allMenuList.forEach((m)->{//メニューリストの取得
			if(m.parentMenuType == Game.currentMenuType) {
				Game.menuList.add(m);
			}
		});

		if(Game.menuList.size() != 0) {
			Game.selectedMenu = Game.menuList.get(0);
		}

	}
	
	public static boolean canTrash() {
		if(((ItemMenu)Game.currentMenu).isEquipmented) {
			return false;
		}
		
		for(Actor a: Actor.actors){
			if(a instanceof ItemActor) {
				if(Game.player.posX == a.posX && Game.player.posY == a.posY) {
					return false;
				}
			}
		}
		return true;
	}

}
