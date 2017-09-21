import java.util.*;


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
    		ArrayList<ArrayList<Planet>> links = new ArrayList<ArrayList<Planet>>();
    		for(int i=0;i<nbP;++i) {
    			planets[i]=new Planet();
    			links.add(new ArrayList<Planet>());
    		}
    		nbE=in.nextInt();
    		edges = new int[nbE][2];
    		for(int i=0;i<nbE;++i) {
    			edges[i][0]=in.nextInt();
    			edges[i][1]=in.nextInt();
    			links.get(edges[i][0]).add(planets[edges[i][1]]);
    			links.get(edges[i][1]).add(planets[edges[i][0]]);    			
    		}
    		Planet[] empty=new Planet[0];
    		int i=0;
    		for(ArrayList<Planet> a:links) planets[i++].adj=a.toArray(empty);
    	}
    	State apply(Action a) {
    		return null;
    	}
    	
    	int[] getValidPlanets(int player) {
    		int[] validPlanets = null;
    		for(int i=0;i<nbP;++i) {
    			if (canAssign(planets[i],player)) {
    				validPlanets[i]=i;
    			}
    		}
    		return validPlanets;
    	}
    	
    	boolean canSpreadFrom(int p,int player) {return planets[p].unit[player]>4;}
    	boolean canAssign(Planet p,int player) {
    		if(p.tolerance[player] <=0 ) return false;
    		if(p.unit[player] >0) return true;
    		for(Planet pl:p.adj) if(pl.unit[player] >0) return true;
    		return false;
    	}
    	// return -1 for undecided 0 or 1 if player 0 or 1 wins
    	int whoWins() {
    		if((turn/2) != nbP) return -1;
    		else {
    			int res=0;
    			for(Planet p:planets) {
    				if(p.unit[0]>p.unit[1]) res+=1;
    				else if(p.unit[1]>p.unit[0]) res-=1;
    			}
    			return res>0 ? 0:(res<0?1:-1);
    		}
    	}
    }
    
    static class Planet {
    	int[] unit= new int[2];
		int[] tolerance=new int[] {5,5};
		Planet[] adj;
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
    	Action(Scanner s) {
    		for(int i=0;i<target.length;++i) target[i]=s.nextInt();
    		String spread=s.next();
    		if(spread.equals("NONE")) spreadPlanet=-1;
    		else spreadPlanet=Integer.parseInt(spread);
    	}
    	public String toString() {
    		StringBuilder sb= new StringBuilder();
    		for(int i:target) sb.append(i+"\n");
    		sb.append((spreadPlanet == -1 ?"NONE":spreadPlanet)+"\n");
    		return sb.toString();
    	}
    }
}
