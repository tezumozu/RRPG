package minigame;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.image.Image;

public class MobFactory {
	static HashMap<String,Image> imageMap = new HashMap<String,Image>();
	static {
		ArrayList<String> imageList = new ArrayList<String>();
		//リストの取得
		InputStream input = Game.class.getResourceAsStream("Material/Lists/TestList.txt");
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
			Image i = new Image(Game.class.getResourceAsStream("Material/Test/"+name));
			imageMap.put(name , i);
		});
	}
	public static void makeMob1(int level,int x,int y) {
		Image i = imageMap.get("Mob.png");
		new Enemy(i,x,y,level,"モブ");
	}
	
	public static void makeBoss(int x,int y) {
		Image i = imageMap.get("Boss.png");
		new Boss(i,x,y,12,"ボス");
	}
}
