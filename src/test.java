

public class test {

	public static void main(String[] args) {
		//Sorti 9143 135 -222 -162 0
		player1.Pod p=new player1.Pod();
		p.x=400;
		p.y=7412;
		p.vx=38;
		p.vy=165;
		player1.Action a = new player1.Action(2.96759828703572,100);
		
		p.move(a, null,null);
	}

}
