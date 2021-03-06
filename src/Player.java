import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;


/**
 * Auto-generated code below aims at helping you parse the standard input
 * according to the problem statement.
 **/
class Player {
	// input/output magic for local invocation 	
 	static InputStream in=System.in;
 	static PrintStream out=System.out;
 	
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		State st = new State();
		
		st.readEdges(in);
		System.err.println("Nb of planets " + st.nbP);

		// game loop
		while (true) {
			for (int i = 0; i < st.nbP; i++) {
				st.planets[i].update(in);
			}
	    //System.err.println("After while");
			Set<Planet> validPlanets = st.getValidPlanets(0);
			ArrayList<Planet> listPlanets = new ArrayList<Planet>(validPlanets);
			Collections.sort(listPlanets, (Planet p1,Planet p2)->{return p1.getScore(0)-p2.getScore(0);});
			System.err.println(listPlanets);
			
			MonteCarlo mc = new MonteCarlo(st);
			//System.err.println("After MC");

			Action best = mc.run(20000000); //49 msec run in nanosecs
			//System.err.println("After MC RUN");
			System.out.println(best.toCommand());

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
				planets[i] = new Planet(i);
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
				
			//for(Planet p:planets) //System.err.println(p);
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
				if (i>=0 && canAssign(planets[i], player))
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
			Action res = new Action();
						
			Set<Planet> topInteresting = getTopInterestingPlanets(player);
			List<Planet> topList=new ArrayList<>(topInteresting);
			int [] topChosenCount= new int[nbP];
			for(int i=0;i<res.target.length;++i) {
				if(topList.size()==0) res.target[i]= -1; 
				else {
					Planet p=topList.get(rnd.nextInt(topList.size()));
					res.target[i]= p.idx;
					topChosenCount[p.idx]++;
					if(topChosenCount[p.idx]>=p.tolerance[player]) topList.remove(p); 
				}
			}		
						
			Set<Planet> interesting = getInterestingPlanets(player);
			List<Planet> list=new ArrayList<>(interesting);
			////System.err.println("Interesting planets:");
	    //for(Planet p:interesting) //System.err.println(p);
			int [] chosenCount= new int[nbP];
			for(int i=0;i<res.target.length;++i) {
				if (res.target[i] != -1)
				{
					continue;
				}
				if(list.size()==0) res.target[i]= -1; 
				else {
					Planet p=list.get(rnd.nextInt(list.size()));
					res.target[i]= p.idx;
					chosenCount[p.idx]++;
					if(chosenCount[p.idx]>=p.tolerance[player]) list.remove(p); 
				}
			}
			
			// replace target -1 by remaining valid planets
			Set<Planet> validPlanets = getValidPlanets(player);
			
			List<Planet> listValidPlanets = new ArrayList<>(validPlanets);
			int [] chosenCountValid= new int[nbP];
			for(int i=0;i<res.target.length;++i) {
				if (res.target[i] != -1)
				{
					continue;
				}
				// in the worst case, set arbitrarily 0 instead of -1 as valid input
				if(listValidPlanets.size()==0) res.target[i]= 0; 
				else {
					Planet p=listValidPlanets.get(rnd.nextInt(listValidPlanets.size()));
					res.target[i]= p.idx;
					chosenCountValid[p.idx]++;
					if(chosenCountValid[p.idx]>=p.tolerance[player]) listValidPlanets.remove(p); 
				}
			}
				
			// add interesting spread with 66% chance
			res.spreadPlanet = addOptionalSpread(res, player);
			return res;
		}

		// add spread if interesting
		// if 1 planet has more than 5 units more than the other player
		// and on some neighbours the other player has more units or only a few less
		int addOptionalSpread(Action a, int player) {
			int other = player ^ 1;
			Set<Planet> spreadCandidates = new HashSet<Planet>();
			for (int i = 0; i < nbP; i++) {
				if (planets[i].unit[player] > 5) {
					if (planets[i].unit[player] - planets[i].unit[other] < 5) {
						spreadCandidates.add(planets[i]);
					}
				}
			}
			if (spreadCandidates.isEmpty()) {
				return -1;
			}

			Random rnd = new Random();
			// 66% chance do a spread
//			if (rnd.nextInt(3) > 0) {
			// debug 100% instead
			if (rnd.nextInt(3) > 0) {
				// select randomly one candidate
				int choice = rnd.nextInt(spreadCandidates.size());
				Planet[] array = spreadCandidates.toArray(new Planet[spreadCandidates.size()]);
				return array[choice].idx;
			}
			
			return -1;
		}
		
		Set<Planet> getTopInterestingPlanets(int player)
		{
			int other = player^1;
			Set<Planet> res = new HashSet<Planet>();
			for (int i = 0; i < nbP; i++) {
				// forget planet without any unit
				if (planets[i].unit[player] <= 0)
					continue;
				// select empty planets close to this planet
				for (int j = 0; j < planets[i].adj.length; j++) {
					Planet p = planets[i].adj[j];
					if (p.tolerance[player] > 0 && ((p.unit[player] == 0) || (p.unit[player] <= p.unit[other]))) {
						res.add(p);
					}
				}
			}
			return res;
		}
		// possible planets are those where player have at least 1 unit and the adj
		// and where tolerance is > 0
		Set<Planet> getInterestingPlanets(int player)
		{
			int other = player^1;
			Set<Planet> res = new HashSet<Planet>();
			for (int i = 0; i < nbP; i++) {
				// forget planet without any unit
				if (planets[i].unit[player] <= 0)
					continue;
				// planet can support 1 additional unit
				if (planets[i].tolerance[player] > 0) {
					// planet has enemies nearby
					for (int j = 0; j < planets[i].adj.length; j++) {
						if (planets[i].adj[j].unit[other] > 0) {
							res.add(planets[i]);
							break;
						}
					}
				}
				// select empty planets close to this planet
				for (int j = 0; j < planets[i].adj.length; j++) {
					Planet p = planets[i].adj[j];
					if (p.tolerance[player] > 0 && p.unit[player] == 0) {
						res.add(p);
					}
				}
			}
			return res;
		}
		
		// valid planets are :
		// those with units with enough tolerance
		// and their neighbourgs with enough tolerance
		Set<Planet> getValidPlanets(int player)
		{
			int other = player^1;
			Set<Planet> res = new HashSet<Planet>();
			for (int i = 0; i < nbP; i++) {
				// forget planet without unit
				if (planets[i].unit[player] <= 0)
					continue;
				// add planet with units with enough tolerance
				if (planets[i].tolerance[player] > 0) {
					res.add(planets[i]);
					}
				// add neighbourgs with enough tolerance
				for (int j = 0; j < planets[i].adj.length; j++) {
					Planet p = planets[i].adj[j];
					if (p.tolerance[player] > 0) {
						res.add(p);
					}
				}
			}
			return res;
		}
	}

	static class Planet {
		int[] unit = new int[2];
		int[] tolerance = new int[] { 5, 5 };
		int idx;
		Planet[] adj;
		public boolean equals(Object o) {
			return ((Planet)o).idx == idx;
		}
		Planet(int i) {
			idx=i;
		}
		
		int getScore( int player)
		{
			int other = player^1;
			// if empty
			if ( (unit[player] == 0) && (unit[other] == 0))
			{
				return 2;
			}
			if ( (unit[player] > 0) && (unit[other] == 0))
			{
				return -2;
			}
			else
			{
				return 1;
			}
		}
		Planet(Planet p) {
			unit[0]=p.unit[0];
			unit[1]=p.unit[1];
			tolerance[0]=p.tolerance[0];
			tolerance[1]=p.tolerance[1];
			adj = p.adj;
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
		
		public String toString() {
		    return idx+": "+unit[0]+" "+tolerance[0]+" "+unit[1]+" "+tolerance[1]+" "+adj;
		}
	}

	static class Action {
		int spreadPlanet = -1;
		int[] target = new int[5];
		int score=0;
		boolean sorted = false;
		
		Action() {};
		Action(Scanner s) {
			System.err.println("target.length = "+target.length);
			for (int i = 0; i < target.length; ++i)
			{
				target[i] = s.nextInt();
				System.err.println("target["+i+"] = "+target[i]);
			}
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
			sb.append((spreadPlanet == -1 ? "NONE" : spreadPlanet) + "\n" + "Score: "+score);
			return sb.toString();
		}
		
    public String toCommand() {
      StringBuilder sb = new StringBuilder();
      for (int i : target)
        sb.append(i + "\n");
      sb.append((spreadPlanet == -1 ? "NONE" : spreadPlanet));
      return sb.toString();
    }
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + score;
      result = prime * result + spreadPlanet;
      result = prime * result + Arrays.hashCode(target);
      return result;
    }
    
    public boolean equals(Object o)
    {
      Action a = (Action) o;
      if (a.spreadPlanet != spreadPlanet)
      {
        return false;
      }
      if (!sorted)
      {
        Arrays.sort(target);
      }
      if (!a.sorted)
      {
        Arrays.sort(a.target);
      }
      for (int i=0;i<target.length;++i)
      {
        if (a.target[i] != target[i]) return false;
      }
      return true;
    }
    
    

		
	}
	
	static class MonteCarlo {
		State init;
		Random rnd=new Random();
		MonteCarlo(State s) {
			init=s;
		}
		
		public 
		int nbtentative = 0;
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
				possible.add(chosen);
				nbtentative++;
				////System.err.println("chosen score");
			} while (System.nanoTime()-start < time);
			System.err.println("Nb iter MC: " + nbtentative);
			int  max=Integer.MIN_VALUE;
			Action best=null;
			for(Action p:possible) {
			  //System.err.println(a);
				if(p.score >max) {
					best=p;
					max=p.score;
				}
			}
			return best;
		}
	}

}