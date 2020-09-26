package minigame;

public class Menu {
	public Game.MenuKind parentMenuType;
	public Game.MenuKind menuType;
	public String name;
	
	
	Menu(String name,Game.MenuKind parentMenuType ,Game.MenuKind menuType){
		this.name = name;
		this.parentMenuType = parentMenuType;
		this.menuType = menuType;
	}
	
}
