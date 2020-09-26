package minigame;

import javafx.scene.image.Image;

public class ItemActor extends Actor{
	public String itemID;
	public ItemActor(String itemID,Image image, int posX, int posY) {
		super(image, posX, posY, true);
		this.itemID = itemID;
		// TODO Auto-generated constructor stub
	}//ダンジョン内のアイテム
	
	public ItemActor get() {
		int count = 0;
		for(Menu m:Game.allMenuList) {
			if(m instanceof ItemMenu) {
				count++;
			}
		}
		if(count > 30) {
			return this;
		}else {
			
			synchronized (Actor.actors) {
				this.delete();
			}
			Game.allMenuList.add(new ItemMenu(this.itemID));
			return this;
		}
	}
}
