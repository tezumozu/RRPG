package minigame;

import java.io.File;
import java.util.HashMap;

import javafx.scene.image.Image;

public class ItemFactory {
	public static void makeItem(String itemID,int x,int y) {
		new ItemActor(itemID,Game.itemList.get(itemID).getImage(),x,y);
	}
}
