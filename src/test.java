

public class test {

	public static void main(String[] args) {
		//Sorti 9143 135 -222 -162 0
		UnleashTheGeek.Pod p=new UnleashTheGeek.Pod();
		p.x=400;
		p.y=7412;
		p.vx=38;
		p.vy=165;
		UnleashTheGeek.Action a = new UnleashTheGeek.Action(2.96759828703572,100);
		
		p.move(a, null);
	}

}
