package minigame;

import java.util.ArrayList;
import java.util.List;

public class MapMaker {

	private static int maxRoomSize;
	private static int dSize;
	private static DObject[][] map;//実際のマップを格納
	private static int[][] intMap;//数値でダンジョンを表現
	private static ArrayList<Room> roomList = new ArrayList<Room>();
	public MapMaker(int maxRoomSize,int maxDungeonSize){
		this.maxRoomSize = maxRoomSize;
		if(maxRoomSize < 2) {
			this.maxRoomSize = 2;
		}
		if(maxRoomSize+2 >= maxDungeonSize) {
			maxDungeonSize = (maxRoomSize+1)*4+1;
		}
		this.dSize = maxDungeonSize;
		map = new DObject[dSize][dSize];
		intMap = new int[dSize][dSize];
	}


	public enum DObject{
		OUT,//*
		N_WALL,//*
		S_WALL,//*
		E_WALL,//*
		W_WALL,//*
		EW_WALL,//*
		EW_NC,//*
		EW_SC,//*
		NLC_WALL,//*
		NRC_WALL,//*
		SRC_WALL,//*
		SLC_WALL,//*
		YUKA,//*
		LRC,//*
		RRC,//*
		LRC_RWW,//左が部屋のすみ右側西壁*
		LEW_RLC,//左が東壁、右側部屋のすみ*
		LRC_RRC,//両方とも部屋のすみ*
		EXIT//*
	}


	enum Direction{
		N,
		S,
		W,
		E,
	}


	public static void initalize() {
		roomList = new ArrayList<Room>();
		for(int i = 0; i < dSize; i++) {
			for(int j = 0; j < dSize; j++) {
				intMap[i][j] = 1;//壁
				map[i][j] = DObject.OUT;
			}
		}
	}


	public DObject[][] makeDungeon() {
		ArrayList<Room> makeAbleRoomList = new ArrayList<Room>();
		initalize();//初期化

		//最初に部屋を1つ作成
		int x = maxRoomSize/2+1 + (int)(Math.random()*(dSize-maxRoomSize-maxRoomSize/2-1)); //部屋が増えることのできる限界の範囲
		int y = maxRoomSize/2+1 + (int)(Math.random()*(dSize-maxRoomSize-maxRoomSize/2-1));
		int w = (int)(Math.random()*(maxRoomSize-1)+2);//部屋の最低のサイズ2*2;
		int h = (int)(Math.random()*(maxRoomSize-1)+2);//部屋の最低のサイズ2*2;
		Room room = new Room(x,y,w,h);
		roomList.add(room);
		makeAbleRoomList.add(room);

		//int n = 10;
		//他の部屋をランダムに作成
		while(makeAbleRoomList.size() > 0 /*&& n > 0*/) {//作れる部屋がなくなるまで
			int index = (int)(Math.random()*makeAbleRoomList.size());
			Room newRoom = makeArea(makeAbleRoomList.get(index));//作ったRoomを取得
			if(newRoom == null) {
				makeAbleRoomList.remove(index);
			}else {
				roomList.add(newRoom);
				makeAbleRoomList.add(newRoom);
				//n--;
				intMapNormalization();
			}
		}


		intMapNormalization();
		mapIntToDObject();//intMapから実際のダンジョンを生成
		//System.out.println(this);
		/*for(int i = 0; i < dSize; i++) {
			for(int j = 0; j < dSize; j++) {
				System.out.print(intMap[i][j]);
			}
			System.out.println("");
		}*/
		return this.map;
	}


	public void makeExit() {
		int index = (int)(Math.random()*roomList.size());
		for(int i = 0; i < roomList.size(); i++) {
			if(roomList.get((index+i)%roomList.size()).isRoom()){
				int x = roomList.get((index+i)%roomList.size()).getX() + (int)(Math.random()*roomList.get((index+i)%roomList.size()).getW());
				int y = roomList.get((index+i)%roomList.size()).getY() + (int)(Math.random()*roomList.get((index+i)%roomList.size()).getH());
				map[x][y] = DObject.EXIT;
				break;
			}
		}
	}


	public void makeBoss() {
		int index = (int)(Math.random()*roomList.size());
		for(int i = 0; i < roomList.size(); i++) {
			if(roomList.get((index+i)%roomList.size()).isRoom()){
				int x = roomList.get((index+i)%roomList.size()).getX() + (int)(Math.random()*roomList.get((index+i)%roomList.size()).getW());
				int y = roomList.get((index+i)%roomList.size()).getY() + (int)(Math.random()*roomList.get((index+i)%roomList.size()).getH());
				
				if(x != Game.player.posX && y != Game.player.posX) {
					MobFactory.makeBoss(x,y);
					break;

				}
				
			}
		}
		
	}


	public void makeItems(int level,int px,int py){//ランダムにItem,Enemyを作成する
		List<String> keyList =  new ArrayList<>(Game.itemList.keySet());
		roomList.forEach(r -> {
			if(r.isRoom()) {
				int n = (int)(Math.random()*((r.getW()*r.getH()/16)))+1;//4*4に一体くらい
				for(int i = 1;  i <= n; i++) {
					int x = r.getX();
					int y = r.getY();
					if(r.getH() > r.getW()) {
						x += (int)(Math.random()*r.getW());
						y += (int)(Math.random()*(r.getH()/n*i));
					}else {
						x += (int)(Math.random()*(r.getW()/n*i));
						y += (int)(Math.random()*r.getH());
					}
					if(x != px && y != py && map[x][y] != DObject.EXIT) {
						if(Math.random()*3 < 1) {
							int index = (int)(Math.random()*keyList.size());
							ItemFactory.makeItem(keyList.get(index), x, y);
						}else {
							MobFactory.makeMob1(level,x, y);
						}

					}

					//new Character(new Image(this.getClass().getResourceAsStream("Material/Mob.png")),x,y).start();
				}

			}
			//System.out.println(count);
		});
	}


	public int[] getRandomPoint() {//ランダムな部屋のランダムな座標を取得
		int[] result = new int[2];
		int index = (int)(Math.random()*roomList.size());
		result[0] = roomList.get(index%roomList.size()).getX() + (int)(Math.random()*roomList.get(index%roomList.size()).getW());
		result[1] = roomList.get(index%roomList.size()).getY() + (int)(Math.random()*roomList.get(index%roomList.size()).getH());
		return result;
	}


	private Room makeArea(Room rootRoom) {
		Room newRoom = null;
		//部屋を作る方向を決定
		int index = (int)(Math.random()*Direction.values().length);//ランダムな方向を決定
		for(int i = 0; i < Direction.values().length;i++) {//全方向確認
			Room branchPoint = makeBranchPoint(Direction.values()[index],rootRoom);
			if(branchPoint != null) {
				newRoom =  branchPoint;
				break;
			}
			index = (index+1)%Direction.values().length;

		}
		return newRoom;
	}


	private void intMapNormalization() {
		roomList.forEach(e->{
			intRoomNormalizaton(e);
		});
	}


	private void intRoomNormalizaton(Room room) {
		for(int i = room.getX(); i < room.getX()+room.getW(); i++) {
			for(int j = room.getY(); j < room.getY()+room.getH(); j++) {
				intMap[i][j] = 0;
			}
		}
	}


	private void mapIntToDObject() {
		for(int i = 0; i < dSize; i++) {
			for(int j = 0; j < dSize; j++) {
				map[i][j] = DObject.OUT;//初期化
				int n;
				if(i%(dSize-1) == 0 && j%(dSize-1) == 0) {
					n = 4;
				}else if((i%(dSize-1))*(j%(dSize-1)) == 0){
					if(i == 0 ) {
						n = intMap[i+1][j] + intMap[i][j+1] + intMap[i][j-1];
					}else if(j == 0){
						n = intMap[i+1][j] + intMap[i-1][j] + intMap[i][j+1];
					}else if(i == dSize-1) {
						n = intMap[i-1][j] + intMap[i][j+1] + intMap[i][j-1];
					}else {
						n = intMap[i+1][j] + intMap[i-1][j] + intMap[i][j-1];
					}
				}else {
					if(intMap[i][j] == 0) {
						n = 0;
					}else {
						n = intMap[i+1][j] + intMap[i-1][j] + intMap[i][j-1] + intMap[i][j+1];
					}
				}

				if(n == 4) {//nの数からブロックへ変換
					if(i%(dSize-1) == 0 && j%(dSize-1) == 0) {//隅
						if(i == 0 && j == 0) {
							if(intMap[i+1][j+1] == 0) {
								map[i][j] = DObject.LRC;
							}else {
								map[i][j] = DObject.OUT;
							}
						}else if(i == 0 && j == dSize-1){
							if(intMap[i+1][j-1] == 0) {
								map[i][j] = DObject.S_WALL;
							}else {
								map[i][j] = DObject.OUT;
							}
						}else if(i == dSize-1 && j == 0){
							if(intMap[i-1][j+1] == 0) {
								map[i][j] = DObject.RRC;
							}else {
								map[i][j] = DObject.OUT;
							}
						}else if(i == dSize-1 && j == dSize-1){
							if(intMap[i-1][j-1] == 0) {
								map[i][j] = DObject.S_WALL;
							}else {
								map[i][j] = DObject.OUT;
							}
						}
					}else if(intMap[i-1][j+1] == 0 && intMap[i+1][j+1] == 0) {//南側斜めが空いている
						map[i][j] = DObject.LRC_RRC;
					}else if(intMap[i-1][j+1] == 0) {//西側下斜めが空いている
						map[i][j] = DObject.RRC;
					}else if(intMap[i+1][j+1] == 0) {//東側斜めが空いている
						map[i][j] = DObject.LRC;
					}else if(intMap[i-1][j-1] == 0 || intMap[i+1][j-1] == 0){//南の隅
						map[i][j] = DObject.OUT;
					}else {
						map[i][j] = DObject.OUT;
					}

				}if(n == 3) {
					//隅なら
					if((i%(dSize-1))*(j%(dSize-1)) == 0){
						if(i == 0) {
							if(intMap[i+1][j+1] == 0) {
								map[i][j] = DObject.LRC;
							}else if(intMap[i+1][j-1] == 0) {
								map[i][j] = DObject.S_WALL;
							}else {
								map[i][j] = DObject.OUT;
							}
						}else if(i == dSize-1){
							if(intMap[i-1][j+1] == 0) {
								map[i][j] = DObject.RRC;
							}else if(intMap[i-1][j-1] == 0) {
								map[i][j] = DObject.S_WALL;
							}else {
								map[i][j] = DObject.OUT;
							}
						}else if(j == 0){
							if(intMap[i-1][j+1] == 0 && intMap[i+1][j+1] == 0) {
								map[i][j] = DObject.LRC_RRC;
							}else if(intMap[i-1][j+1] == 0) {
								map[i][j] = DObject.RRC;
							}else if(intMap[i+1][j+1] == 0) {
								map[i][j] = DObject.LRC;
							}else {
								map[i][j] = DObject.OUT;
							}
						}else if(j == dSize-1){
							if(intMap[i-1][j-1] == 0||intMap[i+1][j-1] == 0) {
								map[i][j] = DObject.OUT;
							}else {
								map[i][j] = DObject.OUT;
							}
						}else {
							map[i][j] = DObject.OUT;
						}

					}else {
						if(intMap[i+1][j] == 0 ) {//西壁
							if(intMap[i-1][j+1] == 0) {
								map[i][j] = DObject.LRC_RWW;
							}else {
								map[i][j] = DObject.W_WALL;
							}

						}else if(intMap[i][j+1] == 0){//北壁

							map[i][j] = DObject.N_WALL;
						}else if(intMap[i-1][j] == 0) {//東壁
							if(intMap[i+1][j+1] == 0) {
								map[i][j] = DObject.LEW_RLC;
							}else {
								map[i][j] = DObject.E_WALL;
							}
						}else {//南壁
							if(intMap[i-1][j+1] == 0 && intMap[i+1][j+1] == 0) {//南側斜めが空いている
								map[i][j] = DObject.LRC_RRC;
							}else if(intMap[i+1][j+1] == 0) {
								map[i][j] = DObject.LRC;
							}else if(intMap[i-1][j+1] == 0){
								map[i][j] = DObject.RRC;
							}else {
								map[i][j] = DObject.S_WALL;
							}
						}
					}

				}if(n == 2) {
					//端なら
					if((i%(dSize-1))*(j%(dSize-1)) == 0){
						if(i == 0 ) {//西壁
							map[i][j] = DObject.W_WALL;
						}else if(j == 0){//北壁
							map[i][j] = DObject.N_WALL;
						}else if(i == dSize-1) {//東
							map[i][j] = DObject.E_WALL;
						}else if(j == dSize-1) {//東
							map[i][j] = DObject.S_WALL;
						}
					}else {
						if(intMap[i+1][j] == 0 && intMap[i-1][j] == 0) {//左右が空いている
							map[i][j] = DObject.EW_WALL;
						}else if(intMap[i][j-1] == 0 && intMap[i][j+1] == 0){//上下が空いている
							map[i][j] = DObject.N_WALL;
						}else if(intMap[i][j+1] == 0 && intMap[i+1][j] == 0){//北入り口左のすみ
							map[i][j] = DObject.NLC_WALL;
						}else if(intMap[i][j+1] == 0 && intMap[i-1][j] == 0){//北入り口右のすみ
							map[i][j] = DObject.NRC_WALL;
						}else if(intMap[i][j-1] == 0 && intMap[i-1][j] == 0){//南入り口右のすみ
							if(intMap[i+1][j+1] == 0) {
								map[i][j] = DObject.LEW_RLC;
							}else {
								map[i][j] = DObject.SRC_WALL;
							}
						}else if(intMap[i][j-1] == 0 && intMap[i+1][j] == 0){//南入り口左のすみ
							if(intMap[i-1][j+1] == 0) {
								map[i][j] = DObject.LRC_RWW;
							}else {
								map[i][j] = DObject.SLC_WALL;
							}
						}

						else {
							map[i][j] = DObject.OUT;
						}
					}
				}if(n == 1) {
					if(intMap[i-1][j] == 1 ) {//西壁
						map[i][j] = DObject.NLC_WALL;
					}else if(intMap[i][j-1] == 1){//北壁
						map[i][j] = DObject.EW_NC;
					}else if(intMap[i+1][j] == 1) {//東
						map[i][j] = DObject.NRC_WALL;
					}else {//南
						map[i][j] = DObject.EW_SC;
					}
				}if(n == 0) {
					map[i][j] = DObject.YUKA;
				}
			}
		}
	}


	public ArrayList<Room> getRoomList() {
		return this.roomList; 
	}


	public DObject[][] getMap(){
		return this.map;
	}


	private Room makeBranchPoint(Direction d,Room room){
		Room bp = null;
		int index;//ランダムな地点から
		int maxSize;
		int x,y,w,h;

		switch(d) {
		case N:
			index = (int)(Math.random()*room.getW());
			for(int i = 0; i < room.getW(); i++) {
				x = room.getX()+((i+index)%room.getW());
				y = room.getY()-1;
				for(int j = maxRoomSize; j > 1; j--) {
					if(checkArea(x-j/2,y-j,j)) {
						intMap[x][y] = 0;
						bp = makeRoomOrWay(x,y,d,room,j);
						break;
					}
				}
				if(bp != null) {
					break;
				}

			}
			break;
		case S:
			index = (int)(Math.random()*room.getW());
			for(int i = 0; i < room.getW(); i++) {
				x = room.getX()+((i+index)%room.getW());
				y = room.getY()+room.getH();
				for(int j = maxRoomSize; j > 1; j--) {
					if(checkArea(x-j/2,y+1,j)) {
						intMap[x][y] = 0;
						bp = makeRoomOrWay(x,y,d,room,j);
						break;
					}
				}
				if(bp != null) {
					break;
				}
			}
			break;
		case E:
			index = (int)(Math.random()*room.getH());
			for(int i = 0; i < room.getH(); i++) {
				x = room.getX()+room.getW();
				y = room.getY()+((i+index)%room.getH());
				for(int j = maxRoomSize; j > 1; j--) {
					if(checkArea(x+1,y-j/2,j)) {
						intMap[x][y] = 0;
						bp = makeRoomOrWay(x,y,d,room,j);
						break;
					}
				}
				if(bp != null) {
					break;
				}
			}
			break;
		case W:
			index = (int)(Math.random()*room.getH());
			for(int i = 0; i < room.getH(); i++) {
				x = room.getX()-1;
				y = room.getY()+(int)((i+index)%room.getH());
				for(int j = maxRoomSize; j > 1; j--) {
					if(checkArea(x-j,y-j/2,j)) {
						intMap[x][y] = 0;
						bp = makeRoomOrWay(x,y,d,room,j);
						break;
					}
				}
				if(bp != null) {
					break;
				}
			}
			break;
		}
		return bp;
	}

	private Room makeRoomOrWay(int x,int y,Direction d,Room room,int maxSize) {
		int w = (int)(Math.random()*(maxSize-1)+2);
		int h = (int)(Math.random()*(maxSize-1)+2);
		int rx = 0;
		int ry = 0;
		switch(d) {
		case N:
			rx = x-w/2;
			ry = y-h;
			break;
		case S:
			rx = x-w/2;
			ry = y+1;
			break;
		case E:
			rx = x+1;
			ry = y-h/2;
			break;
		case W:
			rx = x-w;
			ry = y-h/2;
			break;
		}

		/*if(room.isRoom()) {//部屋なら
			w = (int)(Math.random()*((maxSize/2)-1)+1);
			return makeWay(x,y,w,d);
		}else {*/
		if(Math.random()*2 > 1) {
			return new Room(rx,ry,w,h);
		}else {
			w = (int)(Math.random()*((maxSize/2)-1)+1);
			return makeWay(x,y,w,d);
		}
		//}
	}
	private boolean checkArea(int x,int y,int maxSize){
		if(x-1 < 0 || y-1 < 0) {
			return false;
		}else if(x+maxSize >= dSize || y+maxSize >= dSize) {//壁込みではみ出していたら
			return false;
		}

		for(Room r : roomList){
			if(
					Math.abs((x+(double)maxSize/2)-(r.getX()+(double)r.getW()/2)) < ((double)(maxSize+2)/2+(double)r.getW()/2) && 
					Math.abs((y+(double)maxSize/2)-(r.getY()+(double)r.getH()/2)) < ((double)(maxSize+2)/2+(double)r.getH()/2) 
					){
				return false;
			}
		}

		return true;
	}


	private Room makeWay(int x,int y,int l,Direction d) {
		int w = 1;
		int h = 1;
		int flag = (int)(Math.random()*2);
		switch(d) {
		case N:
			if(flag == 1) {//縦
				h = l;
				y = y-l;
			}else {
				w = l;
				x = x-((l-1)*(int)(Math.random()*2));	
				y = y-1;
			}
			break;
		case S:
			if(flag == 1) {//縦
				h = l;
				y = y+1;
			}else {
				w = l;
				x = x-((l-1)*(int)(Math.random()*2));	
				y = y+1;
			}
			break;
		case E:
			if(flag == 1) {//横
				w = l;
				x = x+1;
			}else {
				h = l;
				x = x+1;	
				y = y-((l-1)*(int)(Math.random()*2));
			}
			break;
		case W:
			if(flag == 1) {//横
				w = l;
				x = x-w;
			}else {
				h = l;
				x = x-1;	
				y = y-((l-1)*(int)(Math.random()*2));
			}
		}
		return new Room(x,y,w,h);
	}


	public String toString() {
		String result = "";
		String br = System.getProperty("line.separator");
		for(int i = 0; i < dSize; i++) {
			for(int j = 0; j < dSize; j++) {
				switch(map[j][i]) {
				case YUKA:
					result+= "  ";
					break;
				case OUT:
					result+= "[]";
					break;
				case N_WALL:
					result+= "__";
					break;
				case S_WALL:
					result+= "[]";
					break;
				case E_WALL:
					result+= "| ";
					break;
				case W_WALL:
					result+= " |";
					break;
				case EW_WALL:
					result+= "||";
					break;
				case NRC_WALL:
					result+= "|_";
					break;
				case NLC_WALL:
					result+= "_|";
					break;
				case SRC_WALL:
					result+= "|-";
					break;
				case SLC_WALL:
					result+= "-|";
					break;
				case EW_NC:
					result+= "[|";
					break;
				case EW_SC:
					result+= "|]";
					break;
				case LRC:
					result+= "\\ ";
					break;
				case RRC:
					result+= " /";
					break;
				case LRC_RWW:
					result+= "/|";
					break;
				case LEW_RLC:
					result+= "|\\";
					break;
				case LRC_RRC:
					result+= "/\\";
					break;
				case EXIT:
					result+= "E ";
					break;

				}
				//result += map[i][j];
			}
			result += br;
		}
		return result;
	}

	public static String test() {
		String result = "";
		String br = System.getProperty("line.separator");
		for(int i = 0; i < dSize; i++) {
			for(int j = 0; j < dSize; j++) {
				switch(map[j][i]) {
				case YUKA:
					result+= "  ";
					break;
				default:
					result+= "[]";
					break;
				}
				//result += map[i][j];
			}
			result += br;
		}
		return result;
	}

	public static void main(String[] args) {
		MapMaker map = new MapMaker(5,5*5);
		map.makeDungeon();
		map.makeDungeon();
		System.out.println(test());

	}
}
