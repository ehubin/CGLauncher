import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;


public class Hypersonic implements GameState {
	
	final int width=13;
	final int height=11;
	int[][] board= new int[width][height]; //floor=0 (.) box=1(0) wall=2(X)
	char[] boardSymbol= new char[] {'.','0','X'};
	List<Entity> entities=new ArrayList<Entity>();
	
	public Hypersonic() {}
	@Override
	public int getResult() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void init() {
		entities.add(Entity.createPlayer(0,0,0));
		entities.add(Entity.createPlayer(1,10,10));
	}
	@Override
	public String getStateStr(int id) {
		StringBuffer sb=new StringBuffer();
        for (int y = 0; y < height; y++) {
        	for(int x=0;x<width;++x) {
                sb.append(boardSymbol[board[x][y]]);
        	}
        	sb.append("\n");
        }
                   
        sb.append(entities.size()+"\n");
        for (Entity e:entities) {
        	sb.append(e+"\n");
        }
        //sb.append("\n");
		return sb.toString();
	}
	
	@Override
	public boolean setPlayerAction(int id, String s) {
		return true;
		
	}
	
	

	@Override
	public String getInitStr(int id) {
		return width+" "+height+" "+id+"\n";
	}
	
	public static class Entity {
		int type;
		int owner;
		int x;
		int y;
		int param1;
		int param2;
		static Entity createPlayer(int id,int x,int y) {
			Entity res=new Entity();
			res.type=0; res.owner=id; res.x=x;res.y=y; 
			res.param1=1; //one bomb initially;
			res.param2=3; //range=3 initially
			return res;
		}
		public String toString() {return type+" "+owner+" "+x+" "+y+" "+param1+" "+param2;}
	}

	@Override
	public void startTurn() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void draw(Graphics2D g) {
		// TODO Auto-generated method stub
		
	}
}
