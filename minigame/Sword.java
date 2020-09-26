package minigame;

public class Sword extends Item{
	 public int power;
	
	public Sword(String name,int power,String text) {
		super(name,text);
		this.power = power;
	}
	
	public boolean use() {
		if(!((ItemMenu)Game.currentMenu).isEquipmented) {
			Game.allMenuList.forEach(e->{
				if(e instanceof ItemMenu) {
					if(Game.itemList.get(((ItemMenu)e).itemID) instanceof Sword) {
						((ItemMenu) e).isEquipmented = false;
					}
				}
			});
			Game.player.changeSword(this);
			((ItemMenu)Game.currentMenu).isEquipmented = true;
			
			Game.addText(name+"を装備した！");
			Actor.characterStop(700);
		}
		return false;
	}

}
