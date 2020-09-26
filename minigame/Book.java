package minigame;

public class Book extends Item{
	Spell spell;
	
	public Book(String name,String text,Spell spell) {
		super(name,text);
		this.spell = spell;
		
	}
	
	public boolean use() {
		spell.spell(Game.player.posX, Game.player.posY);
		return true;
	}
	
	
	public static interface Spell{
		public void spell(int x,int y);
	}
}
