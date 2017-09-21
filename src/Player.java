import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        State st=new State();
        st.readEdges(in);

        // game loop
        while (true) {
            for (int i = 0; i < st.nbP; i++) {
                st.planets[i].update(in);
            }
            Action a=null;
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println(a);
            
        }
    }
    
    static class State {
    	Planet[] planets;
    	int nbP,nbE;
    	int turn = 0;
    	int [][] edges;
    	void readEdges(Scanner in) {
    		nbP=in.nextInt();
    		planets=new Planet[nbP];
    		for(int i=0;i<nbP;++i) planets[i]=new Planet();
    		nbE=in.nextInt();
    		edges = new int[nbE][2];
    		for(int i=0;i<nbE;++i) {
    			edges[i][0]=in.nextInt();
    			edges[i][1]=in.nextInt();
    		}
    	}
    	State apply(Action a) {
    		return null;
    	}
    	int[] getValidPlanets() {return null;}
    	boolean canSpreadFrom(int p,int player) {return planets[p].unit[player]>4;}
    }
    
    static class Planet {
    	int[] unit= new int[2];
		int[] tolerance=new int[2];
    	Planet() {
    	}
    	void update(Scanner in) {
    		unit[0]=in.nextInt();
    		tolerance[0]=in.nextInt();
    		unit[1]=in.nextInt();
    		tolerance[1]=in.nextInt();
    		in.nextInt(); //ignore canAssign
    	}
    }
    
    static class Action {
    	int spreadPlanet=-1;
    	int[] target=new int[5];
    	
    	public String toString() {
    		StringBuilder sb= new StringBuilder();
    		for(int i:target) sb.append(i+"\n");
    		sb.append((spreadPlanet == -1 ?"NONE":spreadPlanet)+"\n");
    		return sb.toString();
    	}
    }
}
