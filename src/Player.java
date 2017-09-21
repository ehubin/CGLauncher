import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse the standard input
 * according to the problem statement.
 **/
class Player {

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		State st = new State();
		st.readEdges(in);

		// game loop
		while (true) {
			for (int i = 0; i < st.nbP; i++) {
				st.planets[i].update(in);
			}
			MonteCarlo mc = new MonteCarlo(st);
			Action best = mc.run(49000000); //49 msec run in nanosecs
			System.out.println(best);

		}
	}

	static class State implements Cloneable{
		static Random rnd=new Random();
		Planet[] planets;
		int nbP, nbE;
		int turn = 0;
		int[][] edges;

		public Object clone() {
			State res= new State();
			res.nbP=nbP;
			res.nbE=nbE;
			res.edges=edges;// no deep clone needed
			res.turn=turn;
			res.planets=new Planet[planets.length] ;
			for(int i=0;i<planets.length;++i)
				res.planets[i]= new Planet(planets[i]);
			return res;
		}
		// returns -1 if undecided 0 or 1 if one of the two players wins
		int whoWins() {
			if(turn < nbP/2) return -1;
			int res=0;
			for(Planet p:planets) {
				if(p.unit[0]>p.unit[1]) res++;
				if(p.unit[0]<p.unit[1]) res--;
			}
			return res>0? 0: (res<0? 1:-1);
		}

		void readEdges(Scanner in) {

			nbP = in.nextInt();
			planets = new Planet[nbP];
			ArrayList<ArrayList<Planet>> links = new ArrayList<ArrayList<Planet>>();
			for (int i = 0; i < nbP; ++i) {
				planets[i] = new Planet();
				links.add(new ArrayList<Planet>());
			}
			nbE = in.nextInt();
			edges = new int[nbE][2];
			for (int i = 0; i < nbE; ++i) {
				edges[i][0] = in.nextInt();
				edges[i][1] = in.nextInt();
				links.get(edges[i][0]).add(planets[edges[i][1]]);
				links.get(edges[i][1]).add(planets[edges[i][0]]);
			}
			Planet[] empty = new Planet[0];
			int i = 0;
			for (ArrayList<Planet> a : links)
				planets[i++].adj = a.toArray(empty);
		}

		/**
		 * Every turn: You must affect 5 units to planets you can reach. You can reach
		 * planets you already have at least one unit on and the neighbors of planets
		 * you control. You may choose to sacrifice 5 units from a planet you control in
		 * order to spawn 1 unit on each of that planet's neighbors, regardless of the
		 * amount of neighbors. This is called unit spread.
		 * 
		 * @param a
		 * @param player
		 * @return
		 */
		
		void applyForOnePlayer(Action a, int player) {
			if (a.spreadPlanet > -1) {
				Planet spreadFrom = planets[a.spreadPlanet];
				if (spreadFrom.unit[player] > 4) {
					for (Planet planet : spreadFrom.adj) {
						incrementUnitForPlanet(planet, player);
					}
					spreadFrom.unit[player] -= 5;
				}
			}

			for (int i : a.target) {
				if (canAssign(planets[i], player))
					incrementUnitForPlanet(planets[i], player);
			}
		}
		
		
		State apply(Action a1, Action a2) {
		  applyForOnePlayer(a1, 0);
		  applyForOnePlayer(a2, 1);
		  
		  boolean[] decrementFirstPlayer = new boolean[nbP];
		  boolean[] decrementSecondPlayer = new boolean[nbP];
		  int counter = -1;
		  for (Planet planet: planets) {
		    counter++;
		    int drawCount = 0;
		    int player0WinsCount = 0;
		    int adjCount = planet.adj.length;
		    for (Planet adjPlanet: planet.adj) {
		      if (adjPlanet.draw()) {
		        drawCount++;
		      }
		      else if (adjPlanet.firstPlayerWins()) {
		        player0WinsCount++;
		      }
		    }
		    if (player0WinsCount == adjCount - drawCount) {
		      // this is a perfect draw .. do nothing
		      continue;
		    }
		    if (player0WinsCount > adjCount - drawCount) {
		      decrementFirstPlayer[counter] = true;
		    } else {
		      decrementSecondPlayer[counter] = true;
		    }
		  }
		  
		  for (int i = 0; i < nbP; i++) {
		    if (decrementFirstPlayer[i]) {
		      decrementUnit(planets[i],0);
		    }
		    if (decrementSecondPlayer[i]) {
          decrementUnit(planets[i],1);
        }
		  }
		  
		  turn+=2;
		  return this;
		}
		
		private void decrementUnit(Planet planet, int player) {
		  if (planet.unit[player] > 0) {
		    planet.unit[player]--;
		  }
		}


		private void incrementUnitForPlanet(Planet planet, int player) {
			if (planet.tolerance[player] > 0) {
				planet.tolerance[player]--;
				planet.unit[player]++;
			}
		}

		boolean canAssign(Planet p, int player) {
			if (p.tolerance[player] <= 0)
				return false;
			if (p.unit[player] > 0)
				return true;
			for (Planet pl : p.adj)
				if (pl.unit[player] > 0)
					return true;
			return false;
		}

		boolean canSpreadFrom(int p, int player) {
			return planets[p].unit[player] > 4;
		}
		
		
		Action getRandomAction(int player) {
			
			return new Action();
		}
	}

	static class Planet {
		int[] unit = new int[2];
		int[] tolerance = new int[] { 5, 5 };
		Planet[] adj;

		Planet() {
		}
		Planet(Planet p) {
			unit[0]=p.unit[0];
			unit[1]=p.unit[1];
			tolerance[0]=p.tolerance[0];
			tolerance[1]=p.tolerance[1];
		}

		void update(Scanner in) {
			unit[0] = in.nextInt();
			tolerance[0] = in.nextInt();
			unit[1] = in.nextInt();
			tolerance[1] = in.nextInt();
			in.nextInt(); // ignore canAssign
		}
		
		boolean draw() {
		  return (unit[0] == unit[1]);
		}
		
		boolean firstPlayerWins() {
		  return (unit[0] > unit[1]);
		}
	}

	static class Action {
		int spreadPlanet = -1;
		int[] target = new int[5];
		int score=0;
		
		Action() {};
		Action(Scanner s) {
			for (int i = 0; i < target.length; ++i)
				target[i] = s.nextInt();
			String spread = s.next();
			if (spread.equals("NONE"))
				spreadPlanet = -1;
			else
				spreadPlanet = Integer.parseInt(spread);
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i : target)
				sb.append(i + "\n");
			sb.append((spreadPlanet == -1 ? "NONE" : spreadPlanet) + "\n");
			return sb.toString();
		}
	}
	
	static class MonteCarlo {
		State init;
		Random rnd=new Random();
		MonteCarlo(State s) {
			init=s;
		}
		Action run(long time) {
			long start = System.nanoTime();
			HashSet<Action> possible=new HashSet<>();
			do {
				State a=(State)(init.clone());
				Action a0=null,a1,chosen=null;
				  do{
					  if(a0==null) {
						  a0=a.getRandomAction(0);
						  chosen=a0;
					  } else {
						  a0=a.getRandomAction(0);
					  }
					  a1= a.getRandomAction(1);
					  a.apply(a0,a1);
				  } while(a.turn < a.nbP*2);
				chosen.score += (a.whoWins()==0?1 :(a.whoWins()==1?-1:0));
			} while (System.nanoTime()-start < time);
			int  max=Integer.MIN_VALUE;
			Action best=null;
			for(Action a:possible) {
				if(a.score >max) {
					best=a;
					max=a.score;
				}
			}
			return best;
		}
	}

}