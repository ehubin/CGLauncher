
public class player1 {

	public static void main(String[] args) {
		while(true) {
		try {
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("player1");
		}
	}

}
