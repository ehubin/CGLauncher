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
	}
	@Override
	public String getStateStr() {
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
		return sb.toString();
	}
	
	@Override
	public boolean setPlayerAction(int id, String s) {
		return true;
		
	}
	
	

	@Override
	public String getInitStr(int id) {
		return width+" "+height+" "+id;
	}
	
	public static class Entity {
		int type;
		int owner;
		int x;
		int y;
		int param1;
		int param2;
		public String toString() {return type+" "+owner+" "+x+" "+y+" "+param1+" "+param2;}
	}
}
