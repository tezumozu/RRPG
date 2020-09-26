package minigame;

import minigame.Game.MenuKind;

public class ItemMenu extends Menu{
	public boolean isEquipmented = false;
	public String itemID;
	public ItemMenu(String itemID) {
		super(Game.itemList.get(itemID).name, Game.MenuKind.ItemList,MenuKind.Item);
		this.itemID = itemID;
		// TODO Auto-generated constructor stub
	}//メニュー状態のアイテム
	
	public boolean use() {
		return Game.itemList.get(itemID).use();
		
	}

}
