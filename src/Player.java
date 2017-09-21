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

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println("0");
            System.out.println("0");
            System.out.println("0");
            System.out.println("0");
            System.out.println("0");
            System.out.println("0");
        }
    }
    
    static class State {
    	Planet[] planets;
    	int nbP,nbE;
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
}
