package minigame;

public class Armor extends Item{
	public int power;
	public Armor(String name,int power,String text) {
		super(name,text);
		this.power = power;
	}
	
	public boolean use() {
		if(!((ItemMenu)Game.currentMenu).isEquipmented) {
			Game.allMenuList.forEach(e->{
				if(e instanceof ItemMenu) {
					if(Game.itemList.get(((ItemMenu)e).itemID) instanceof Armor) {
						((ItemMenu) e).isEquipmented = false;
					}
				}
			});
			Game.player.changeArmor(this);
			((ItemMenu)Game.currentMenu).isEquipmented = true;
		}
		Game.addText(name+"を装備した！");
		Actor.characterStop(700);
		return false;
	}
}
