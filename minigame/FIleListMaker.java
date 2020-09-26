package minigame;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import javafx.scene.image.Image;

public class FIleListMaker {
	public static void main(String[] args) {
		try{
			File file = new File("src/minigame/Material/Lists/PlayerList.txt");
			FileWriter filewriter = new FileWriter(file);
			
			String path = "Material/Player";
			String[] imagelist = null;
			try {
				imagelist = new File(Game.class.getResource(path).toURI()).list();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch bloc
			}
			for(String name:imagelist) {
				String br = System.getProperty("line.separator");
				filewriter.write(name+ br);
			};
			filewriter.close();
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
}
