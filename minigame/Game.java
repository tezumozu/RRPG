package minigame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import minigame.MapMaker.DObject;

public class Game extends Application {

	//ゲームの状態変異
	enum GameScene{
		GAME,
		TITLE
	}

	GameScene gameScene = GameScene.TITLE;


	//描画用画像読み込み
	static HashMap<String,Image> imageMap = new HashMap<String,Image>();
	static {
		ArrayList<String> imageList = new ArrayList<String>();
		//リストの取得
		InputStream input = Game.class.getResourceAsStream("Material/Lists/DObjectList.txt");
		BufferedReader din = new BufferedReader(new InputStreamReader(input));

		try {
			String s;
			while((s = din.readLine()) != null) {
				imageList.add(s);
			}
		} catch (IOException e) {
		}
		imageList.forEach((name)-> {
			Image i = new Image(Game.class.getResourceAsStream("Material/DObject/"+name));
			imageMap.put(name , i);
		});
	}
	//クリア
	static boolean clear = false;
	//タイルの大きさ（タイルは正方形）
	static final int TILESIZE = 65;
	//画面のタイルの枚数
	public static final int NUMTILE_X = 7;//描画する範囲横
	public static final int NUMTILE_Y = 7;//描画する範囲縦

	//タイルの量から画面サイズを計算
	static final int PANEL_W = TILESIZE * NUMTILE_X;//枚数と大きさから横幅
	static final int PANEL_H = TILESIZE * NUMTILE_Y;//枚数と大きさから縦幅
	static long startTime;
	//
	static Game game;
	private static Canvas dungeonCanvas;//ダンジョン描画用のキャンバス
	private static Canvas mapCanvas;//マップを表示するキャンバス
	private static Canvas statusCanvas;//ステータス表示用キャンバス　
	private static Canvas textCanvas;//テキストフィールド用キャンバス

	//ダンジョンの設定
	private static final int maxRoomSize = 7;
	private static final int maxDungeonSize = 7*3;
	public static MapMaker.DObject[][] dungeon;
	private static MapMaker mapMaker = new MapMaker(maxRoomSize,maxDungeonSize);
	private static DObject[][] map;
	private static int level = 1;//階層
	public static Player player;
	public static Stage stage;
	public static int baseX;
	public static int baseY; 

	//描画関係
	static boolean drawEnd = false;
	static Thread drawer;

	//テキスト関連
	private static String text;

	//メニュー
	public static boolean showMenu = false;//メニューを表示するかどうか
	public static ArrayList<Menu> allMenuList = new ArrayList<Menu>();
	public static ArrayList<Menu> menuList = new ArrayList<Menu>();
	enum MenuKind{
		Main,
		Action,
		ItemList,
		Item,
		Use,
		End,

		Exit,//階段用;
		Down,
		Keep

	}
	public static Menu main = new Menu("main",MenuKind.End,MenuKind.Main);
	public static Menu exit = new Menu("exit",null,MenuKind.Exit);
	public static Menu currentMenu = main;//現在見ているメニューオブジェクト
	public static MenuKind currentMenuType = main.menuType;//現在見ているメニューオブジェクトのタイプ
	public static Menu selectedMenu;//現在選択している次のメニュー
	public static int index = 0;

	//アイテムリスト
	public static HashMap<String,Item> itemList = new HashMap<String,Item>();

	public static void menuInit(){
		//menuの初期化
		allMenuList.removeAll(allMenuList);
		allMenuList.add(main);
		allMenuList.add(new Menu("道具",MenuKind.Main,MenuKind.ItemList));
		//allMenuList.add(new Menu("技",MenuKind.Main,MenuKind.Action));
		allMenuList.add(new Menu("使う",MenuKind.Item,MenuKind.End));
		/*allMenuList.add(new Menu("道具",MenuKind.Main,MenuKind.Item));*/
		allMenuList.add(new Menu("捨てる",MenuKind.Item,MenuKind.End));

		//階段用
		allMenuList.add(exit);
		allMenuList.add(new Menu("降りる",MenuKind.Exit,MenuKind.End));
		allMenuList.add(new Menu("降りない",MenuKind.Exit,MenuKind.End));		
	}

	{
		//アイテムリストの作成
		String br = System.getProperty("line.separator");
		//剣
		itemList.put("短剣",new Sword("短剣",1,"小さな短剣" + br + "攻撃力 +1"));
		itemList.put("脇差し",new Sword("脇差し",2,"短い日本刀" + br + "攻撃力 +2"));
		itemList.put("竹刀",new Sword("竹刀",3,"剣道で使われる竹刀" + br + "攻撃力 +3"));
		itemList.put("釘バット",new Sword("釘バット",4,"見た目からしてゴツい釘バット" + br + "攻撃力 +4"));
		itemList.put("レイピア",new Sword("レイピア",5,"細身で先端の鋭く尖った剣" + br + "攻撃力 +5"));
		itemList.put("星屑の太刀",new Sword("星屑の太刀",6,"星屑から作られた太刀" + br + "攻撃力 +6"));
		itemList.put("ああああの剣",new Sword("ああああの剣",7,"かつて世界を救った勇者の剣" + br + "攻撃力 +7"));

		//防具
		itemList.put("ワンピース",new Armor("ワンピース",1,"探検用のワンピース" + br + "防御力 +1"));
		itemList.put("絹の衣",new Armor("絹の衣",2,"絹で作られたワンピース" + br + "防御力 +2"));
		itemList.put("狼の衣",new Armor("狼の衣",3,"狼の毛皮で作られたワンピース" + br + "防御力 +3"));
		itemList.put("鋼の衣",new Armor("鋼の衣",4,"鋼が編み込まれたワンピース" + br + "防御力 +4"));
		itemList.put("竜の衣",new Armor("竜の衣",5,"竜の皮から作られたワンピース" + br + "防御力 +5"));
		itemList.put("天の衣",new Armor("天の衣",6,"天界のワンピース" + br + "防御力 +6"));
		itemList.put("ああああの鎧",new Armor("ああああの鎧",7,"かつて世界を救った勇者の鎧" + br + "防御力 +7"));


		//呪文書
		itemList.put("癒しの書",new Book("癒しの書","呪文について書かれた本" + br + "HPを30回復する",
				(x,y)->{
					addText("癒しの書 を使った！");
					Game.player.currentHP+=30;
					if(Game.player.currentHP > Game.player.getMaxHP()) {
						Game.player.currentHP = Game.player.getMaxHP();
					}
					Actor.characterStop(700);
					Game.allMenuList.remove(Game.currentMenu);
				}));

		itemList.put("治癒の書",new Book("治癒の書","呪文について書かれた本" + br + "HPを50回復する",
				(x,y)->{
					addText("治癒の書 を使った！");
					Game.player.currentHP+=50;
					if(Game.player.currentHP > Game.player.getMaxHP()) {
						Game.player.currentHP = Game.player.getMaxHP();
					}
					Actor.characterStop(700);
					Game.allMenuList.remove(Game.currentMenu);
				}));

		itemList.put("完治の書",new Book("完治の書","呪文について書かれた本" + br + "HPを全回復する",
				(x,y)->{
					addText("完治の書 を使った！");
					Game.player.currentHP = Game.player.getMaxHP();
					Actor.characterStop(700);
					Game.allMenuList.remove(Game.currentMenu);
				}));

		itemList.put("火の書",new Book("火の書","呪文について書かれた本" + br + "前方の的に小ダメージ",
				(x,y)->{
					addText("火の書 を使った！");
					Actor.characterStop(700);
					switch(Game.player.direction) {
					case N:
						y-=1;
						break;
					case S:
						y+=1;
						break;
					case W:
						x-=1;
						break;
					case E:
						x+=1;
						break;
					default:
						break;
					}
					Enemy target  = player.existsAtOnryEnemy(x, y);
					if(target != null) {
						target.currentHP -= 30;
						addText(target.getName() + "へ 30 のダメージ!");
						Actor.characterStop(700);
						if(target.currentHP < 1) {//死んだら
							target.delete();
							text = target.getName() + "は倒れた";
							Game.addText(text);
							Actor.characterStop(700);
							player.exp += target.level*2;
							if(player.exp >= player.getMaxExp(level)) {//レベルが上がる
								player.levelUP();
							}
							if(target instanceof Boss) {
								Game.clear = true;
							}
						}
					}
					Actor.characterStop(700);
					Game.allMenuList.remove(Game.currentMenu);
				}));



		itemList.put("火炎の書",new Book("火炎の書","呪文について書かれた本" + br + "前方の的に中ダメージ",
				(x,y)->{
					addText("火炎の書 を使った！");
					Actor.characterStop(700);
					switch(Game.player.direction) {
					case N:
						y-=1;
						break;
					case S:
						y+=1;
						break;
					case W:
						x-=1;
						break;
					case E:
						x+=1;
						break;
					default:
						break;
					}
					Enemy target  = player.existsAtOnryEnemy(x, y);
					if(target != null) {
						target.currentHP -= 50;
						addText(target.getName() + "へ 50 のダメージ!");
						Actor.characterStop(700);
						if(target.currentHP < 1) {//死んだら
							target.delete();
							text = target.getName() + "は倒れた";
							Game.addText(text);
							Actor.characterStop(700);
							player.exp += target.level*2;
							if(player.exp >= player.getMaxExp(level)) {//レベルが上がる
								player.levelUP();
							}
							if(target instanceof Boss) {
								Game.clear = true;
							}
						}
					}
					Actor.characterStop(700);
					Game.allMenuList.remove(Game.currentMenu);
				}));

		itemList.put("業火の書",new Book("業火の書","呪文について書かれた本" + br + "前方の的に大ダメージ",
				(x,y)->{
					addText("業火の書 を使った！");
					Actor.characterStop(700);
					switch(Game.player.direction) {
					case N:
						y-=1;
						break;
					case S:
						y+=1;
						break;
					case W:
						x-=1;
						break;
					case E:
						x+=1;
						break;
					default:
						break;
					}
					Enemy target  = player.existsAtOnryEnemy(x, y);
					if(target != null) {
						target.currentHP -= 70;
						addText(target.getName() + "へ 70 のダメージ!");
						Actor.characterStop(700);
						if(target.currentHP < 1) {//死んだら
							target.delete();
							text = target.getName() + "は倒れた";
							Game.addText(text);
							Actor.characterStop(700);
							
							player.exp += target.level*2;
							if(player.exp >= player.getMaxExp(level)) {//レベルが上がる
								player.levelUP();
							}
							
							if(target instanceof Boss) {
								Game.clear = true;
							}
						}
					}
					Actor.characterStop(700);
					Game.allMenuList.remove(Game.currentMenu);
				}));
	}

	//タイトル用
	static Canvas title = new Canvas(PANEL_W/5*8,PANEL_H/5*6);
	static Pane titlePane = new Pane(title);
	static Scene titleScene = new Scene(titlePane);

	//ゲーム画面用
	VBox left;
	VBox right;
	HBox display;
	Scene playScene;



	public static void main(String[] args) {
		launch(args);
	}


	public void start(Stage primaryStage) throws Exception {
		startTime = System.nanoTime();
		Game.game = this;
		dungeonCanvas = new Canvas(Game.PANEL_W, Game.PANEL_H);
		mapCanvas = new Canvas(Game.PANEL_W/5*3, Game.PANEL_H/5*3);
		statusCanvas = new Canvas(Game.PANEL_W/5*3, Game.PANEL_H/5*3);
		textCanvas = new Canvas(Game.PANEL_W, Game.PANEL_H/5);


		left  = new VBox(dungeonCanvas,textCanvas);
		right = new VBox(mapCanvas,statusCanvas);
		display = new HBox(left,right);
		playScene = new Scene(display);
		//root.getChildren().add(dungeonCanvas);

		//メニューリストの取得
		Model.getMenuList();

		this.draw();

		drawer = new Thread(()->{
			synchronized(drawer) {
				while(!drawEnd) {
					Platform.runLater(()->{
						draw();
					});
					try {
						//Thread.currentThread();
						Thread.sleep(100/6);
					} catch (InterruptedException e1) {
					}
				}
			}
		});
		drawer.start();

		playScene.setOnKeyPressed((e) -> {//keyの設定
			Model.keyPressed(e.getCode());
		});

		titleScene.setOnKeyPressed((e)->{
			gameScene = GameScene.GAME;
			Game.stage.setScene(playScene);
		});

		primaryStage.setTitle("NE30-0035 林 恭史");
		primaryStage.sizeToScene();
		primaryStage.setScene(titleScene);
		primaryStage.show();
		Game.stage = primaryStage;
	}


	synchronized void drawDungeon() {
		if (Game.dungeonCanvas != null) {
			GraphicsContext gc = Game.dungeonCanvas.getGraphicsContext2D();
			gc.setFill(Color.rgb(41, 41, 45));
			gc.fillRect(0, 0, PANEL_W, PANEL_H);
			//キャラクターの描画
			gc.save();
			//床の描画

			gc.translate(-Game.baseX * TILESIZE, -Game.baseY * TILESIZE);

			for(int i = 0;i < dungeon.length; i++) {
				for(int j = 0; j < dungeon[i].length; j++) {
					Image image = null;
					switch(map[i][j]) {
					case YUKA:
						image = imageMap.get("YUKA.bmp");
						break;
					case OUT:
						image = imageMap.get("OUT.bmp");
						break;
					case N_WALL:
						image = imageMap.get("N_WALL.bmp");
						break;
					case S_WALL:
						image = imageMap.get("S_WALL.bmp");
						break;
					case E_WALL:
						image = imageMap.get("E_WALL.bmp");
						break;
					case W_WALL:
						image = imageMap.get("W_WALL.bmp");
						break;
					case EW_WALL:
						image = imageMap.get("EW_WALL.bmp");
						break;
					case NRC_WALL://
						image = imageMap.get("NRC_WALL.png");
						break;
					case NLC_WALL://
						image = imageMap.get("NLC_WALL.png");
						break;
					case SRC_WALL://
						image = imageMap.get("SRC_WALL.png");
						break;
					case SLC_WALL://
						image = imageMap.get("SLC_WALL.png");
						break;
					case EW_NC:
						image = imageMap.get("EW_NC.bmp");
						break;
					case EW_SC:
						image = imageMap.get("EW_SC.bmp");
						break;
					case LRC:
						image = imageMap.get("LRC.bmp");
						break;
					case RRC:
						image = imageMap.get("RRC.bmp");
						break;
					case LRC_RWW://
						image = imageMap.get("LRC_RWW.png");
						break;
					case LEW_RLC://
						image = imageMap.get("LEW_RLC.png");
						break;
					case LRC_RRC://
						image = imageMap.get("LRC_RRC.png");
						break;
					case EXIT:
						image = imageMap.get("EXIT.png");
						break;
					}
					gc.drawImage(image,0,0,image.getWidth(),image.getHeight(),i * Game.TILESIZE, j * Game.TILESIZE,Game.TILESIZE,Game.TILESIZE);
				}
			}
			Actor.paintActors(gc); // キャラクターの描画
			gc.restore();
		}
	}


	synchronized void drawMap() {//マップの出力
		upDateMap();
		double c_size = PANEL_W/5*3;
		double mapTileSize = c_size/map.length-(4*c_size/map.length)/map.length;
		GraphicsContext gc = Game.mapCanvas.getGraphicsContext2D();

		gc.setFill(Color.DARKBLUE);
		gc.fillRect(0, 0, c_size, c_size);
		gc.setStroke(Color.PERU.darker().darker());//縁取り
		gc.setLineWidth(mapTileSize/2);
		gc.strokeRect(0, 0, c_size, c_size);
		gc.setLineWidth(1);

		double textSize = c_size/15;
		gc.setFill(Color.WHITE);//文字
		gc.fillText("BF "+level,textSize/3,textSize+textSize/3);

		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[0].length; j++) {
				if(map[i][j] == DObject.YUKA) {
					gc.setFill(Color.PERU.darker());//部屋の色
				}else if(map[i][j] == DObject.EXIT) {
					gc.setFill(Color.GREEN.brighter());//階段の色
				}else if(map[i][j] == DObject.OUT) {
					gc.setFill(Color.DARKBLUE);//その他の色
				}else {
					gc.setFill(Color.PERU.darker().darker());//その他の色
				}
				gc.fillRect(i*mapTileSize+c_size/map.length*2, j*mapTileSize+c_size/map.length*2, mapTileSize, mapTileSize);
			}
		}

		gc.setFill(Color.AQUA);//プレイヤーの色
		gc.fillRect(player.posX*mapTileSize+c_size/map.length*2, player.posY*mapTileSize+c_size/map.length*2, mapTileSize, mapTileSize);	
	}


	public void upDateMap() {
		for(int i = baseX; i < baseX + NUMTILE_X; i++) {
			for(int j = baseY; j < baseY + NUMTILE_Y; j++) {
				try {
					map[i][j] = dungeon[i][j];
				}catch(ArrayIndexOutOfBoundsException e){

				}

			}
		}
	}


	synchronized void drawStatus() {
		GraphicsContext gc = Game.statusCanvas.getGraphicsContext2D();
		double c_size = PANEL_W/5*3;
		double textSize = c_size/15;
		double grid = (c_size - (c_size/2+textSize))/2;
		double textBotomLine =c_size - grid;
		gc.setFill(Color.BEIGE);
		gc.fillRect(0, 0, c_size, c_size);
		gc.setFill(Color.BLACK);//文字色
		gc.setFont(new Font("MSゴシック",textSize));

		gc.fillText("レベル : " + player.getLevel(),grid,textBotomLine - c_size/10*5);

		//HPが現象していたら色を変える
		if((double)player.getCurrentHP()/(double)player.getMaxHP() < 0.3) {
			gc.setFill(Color.RED);//文字色
		}
		gc.fillText("HP       : " + player.getCurrentHP() + "/" + player.getMaxHP() ,grid,textBotomLine - c_size/10*4);

		gc.setFill(Color.BLACK);//文字色
		gc.fillText("攻撃力 : " + player.getAttackPoint(),grid,textBotomLine - c_size/10*3);
		gc.fillText("防御力 : " + player.getDifencePoint(),grid,textBotomLine- c_size/10*2);
		gc.fillText("武器　 : " + player.getSword(),grid,textBotomLine - c_size/10*1);
		gc.fillText("防具　 : " + player.getArmor(),grid,textBotomLine);

		gc.setStroke(Color.PERU.darker().darker());//縁取り
		gc.setLineWidth(c_size/map.length/2);
		gc.strokeRect(0, 0, c_size, c_size);
		gc.setLineWidth(1);

	}


	synchronized void drawText() {
		GraphicsContext gc = Game.textCanvas.getGraphicsContext2D();
		double c_h = PANEL_H/5;
		gc.setFill(Color.BEIGE);
		gc.fillRect(0, 0, PANEL_W, c_h);

		double textSize = PANEL_W/18;
		double grid = (c_h - textSize*2)/2*0.8;

		gc.setFill(Color.BLACK);//文字色
		gc.setFont(new Font("MSゴシック",textSize));

		gc.fillText(text,grid,grid+textSize);

		gc.setStroke(Color.PERU.darker().darker());//縁取り
		gc.setLineWidth(c_h/map.length/2);
		gc.strokeRect(0, 0, PANEL_W, c_h);
		gc.setLineWidth(1);
	}


	public static void addText(String s) {
		text = s;
	}


	synchronized public void draw() {
		switch(gameScene) {
		case GAME :
			drawMap();
			drawStatus();
			drawText();
			drawDungeon();
			if(showMenu) {
				drawMenu();
			}
			break;
		case TITLE:
			drawTitle();
			break;
		}

	}


	public void drawTitle() {
		double c_w = PANEL_W/5*8;
		double c_h = PANEL_W/5*6;
		double textSize = 30;
		GraphicsContext gc = Game.title.getGraphicsContext2D();
		gc.setFill(Color.rgb(0,90,150));
		gc.fillRect(0, 0, c_w, c_h);
		
		textSize = textSize*2;
		gc.setFont(new Font("MSゴシック",textSize));

		gc.setFill(Color.rgb(230,230,230));//文字色
		gc.fillText("不可思議なダンジョン",c_w/2-textSize*5,textSize+c_h/4);
		
		textSize = textSize/2;
		gc.setFont(new Font("MSゴシック",textSize));
		gc.setFill(Color.rgb(240,180,50));//文字色
		gc.fillText("Please Push Key",c_w/2-(textSize*4),textSize+c_h/2);
		
		gc.setFont(new Font("MSゴシック",textSize*0.8));
		gc.setFill(Color.rgb(240,100,220));//文字色
		gc.fillText("©︎ ︎️2019 Software Development",c_w/2-(textSize*0.8*14.5/2),c_h-textSize);

	}


	synchronized public void drawMenu() {
		GraphicsContext gc = Game.dungeonCanvas.getGraphicsContext2D();

		gc.setFill(Color.BLUE);
		gc.setGlobalAlpha(0.6);
		double menuW,menuH;

		if(currentMenuType == MenuKind.ItemList) {
			menuW = (NUMTILE_X-1)*TILESIZE;
			menuH = (NUMTILE_Y-1)*TILESIZE;
		}else {
			menuW = (NUMTILE_X-1)*TILESIZE/2;
			menuH = ((NUMTILE_Y-1)*TILESIZE)/16*(menuList.size()+1);
		}
		gc.fillRect(TILESIZE/2, TILESIZE/2, menuW, menuH);
		gc.setStroke(Color.PERU.darker().darker());//縁取り
		gc.setGlobalAlpha(1);
		gc.strokeRect(TILESIZE/2, TILESIZE/2, menuW, menuH);

		int i = 0;
		double textSize = ((NUMTILE_Y-1)*TILESIZE)/16;
		for(Menu m : menuList){
			gc.setFill(Color.WHITE);//文字色
			gc.setFont(new Font("MSゴシック",textSize));
			gc.fillText(m.name,(NUMTILE_X*TILESIZE)/2*(int)(i/15) + TILESIZE/2+ textSize,(TILESIZE/2+textSize*(i%15+1)+textSize/2));
			i++;	
		};

		gc.fillText("・",(NUMTILE_X*TILESIZE)/2*(int)(index/15)+ TILESIZE/2 + textSize/2,TILESIZE/2+textSize*((index)%15+1)+textSize/2);
		if(selectedMenu instanceof ItemMenu) {
			addText(itemList.get(((ItemMenu)selectedMenu).itemID).text);
		}

	}


	public void init() throws Exception {//Gameの初期化
		menuInit();
		mapMaker.makeDungeon();
		mapMaker.makeExit();
		dungeon = mapMaker.getMap();
		

		map = new DObject[dungeon.length][dungeon[0].length];
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[0].length; j++) {
				map[i][j] = DObject.OUT;//非表示
			}
		}
		synchronized (Actor.actors) {
			// ... 他のActor、Character生成は省略 ...
			int[] point = mapMaker.getRandomPoint(); 
			Game.player = new Player(point[0], point[1]);
			baseX = player.posX-NUMTILE_X/2;
			baseY = player.posY-NUMTILE_Y/2;

			//初期装備を装備
			ItemMenu sword = new ItemMenu("短剣");//短剣
			ItemMenu armor = new ItemMenu("ワンピース");//ワンピース
			/*ItemMenu test = new ItemMenu("火の書");
			allMenuList.add(test);*/

			allMenuList.add(sword);
			allMenuList.add(armor);
			sword.isEquipmented = true;
			armor.isEquipmented = true;
		}
		mapMaker.makeItems(level,player.posX,player.posY);
	}


	public static void call() {//終了判定
		if (clear) {
			end("GameClear");
		}else if(player.currentHP < 1) {
			end("GameOver");
		}
	}


	private static void end(String s) {
		if(clear) {
			Model.GameEND();
		}else {
			addText("ゲームオーバー");
			Model.GameEND(); 
		}
		initalize();
		return;
	}

	synchronized public static void initalize() {
		level = 1;
		clear = false;
		menuInit();
		Enemy.killAll();
		Actor.actors.removeAll(Actor.actors);
		mapMaker = new MapMaker(maxRoomSize,maxDungeonSize);
		mapMaker.makeDungeon();
		mapMaker.makeExit();
		dungeon = mapMaker.getMap();
		addText("");
		Model.canAccept();

		map = new DObject[maxDungeonSize][maxDungeonSize];
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[0].length; j++) {
				map[i][j] = DObject.OUT;//非表示
			}
		}
		synchronized (Actor.actors) {
			// ... 他のActor、Character生成は省略 ...
			int[] point = mapMaker.getRandomPoint(); 
			Game.player = new Player(point[0], point[1]);
			baseX = player.posX-NUMTILE_X/2;
			baseY = player.posY-NUMTILE_Y/2;

			//初期装備を装備
			ItemMenu sword = new ItemMenu("短剣");//短剣
			ItemMenu armor = new ItemMenu("ワンピース");//ワンピース

			/*ItemMenu test = new ItemMenu("火の書");
			allMenuList.add(test);*/

			allMenuList.add(sword);
			allMenuList.add(armor);
			sword.isEquipmented = true;
			armor.isEquipmented = true;
		}
		mapMaker.makeItems(level,player.posX,player.posY);
	}
	public void stop() throws Exception {
		drawEnd = true;
		Enemy.killAll();
	}


	synchronized public static void stepDown() {
		level++;
		Enemy.killAll();
		Actor.actors.removeAll(Actor.actors);
		mapMaker.makeDungeon();
		Actor.actors.add(player);
		int[] point = mapMaker.getRandomPoint();
		player.posX = point[0];
		player.posY = point[1];
		baseX = player.posX-NUMTILE_X/2;
		baseY = player.posY-NUMTILE_Y/2;
		if(level == 7) {
			mapMaker.makeBoss();
		}else {
			mapMaker.makeExit();
			mapMaker.makeItems((level-1)*2,player.posX,player.posY);
		}



		map = new DObject[dungeon.length][dungeon[0].length];//マップの初期化
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[0].length; j++) {
				map[i][j] = DObject.OUT;//非表示
			}
		}
	}
}
