static class Actions implements Comparable<Actions> {
	double score;
	List<Action[]> actions = null;

	public Actions(double score, List<Action[]> actions) {
		this.score = score;
		this.actions = actions;
	}

	public int compareTo(Actions o) {
		if (o.score == score)
			return 0;
		else if (o.score > score) {
			return -1;
		} else
			return 1;
	};

	public void removeFirstDupeLast() {
		// get the last element add it again and remove first element

		Action[] last = actions.get(actions.size() - 1);
		actions.add(last);
		actions.remove(0);

	}

	}

	static List<Action[]> SimulateAllTurns(State initialState) {

		State currentState = new State(initialState);

		long timer = System.currentTimeMillis();

		double maxScore = Double.NEGATIVE_INFINITY;
		int maxIndex = 0;

		Vector<Actions> poolOfSolutions = new Vector<Actions>();
		poolOfSolutions.setSize(sizeOfPool);

		double score = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < sizeOfPool; i++) {
			currentState = new State(initialState);
			List<Action[]> actions = generateInitialAction(currentState);
			if (initialMutate) {
				actions = mutate(currentState, actions);
			}

			for (Action[] action : actions) {
				// System.out.println(action[1].angle+" "+action[1].thrust);
				currentState.simulate(action);
			}
			score = currentState.score();
			if (score > maxScore) {
				// System.out.println("MAAAAAX "+score+" "+currentState.p1.p2);
				maxScore = score;
				maxIndex = i;
			}
			poolOfSolutions.set(i, new Actions(score, actions));
		}
		// System.out.println("On mute a fond");
		int randomInPool = 0;
		List<Action[]> currentSolution = null;
		int count = 0;
		while (System.currentTimeMillis() - timer < maxTime) {
			count++;
			currentState = new State(initialState);
			randomInPool = (int) (Math.random() * sizeOfPool);
			// System.out.println(randomInPool);
			currentSolution = (poolOfSolutions.get(randomInPool)).actions;
			currentSolution = mutate(currentState, currentSolution);

			for (Action[] action : currentSolution) {
				// System.out.println(action[1].angle+" "+action[1].thrust);
				currentState.simulate(action);
			}

			score = currentState.score();
			// System.out.println("score="+score);
			// find the min in the vector and replace it
			double minScore = Double.POSITIVE_INFINITY;
			int minIndex = 0;
			double currScore = 0;
			for (int i = 0; i < sizeOfPool; i++) {
				currScore = poolOfSolutions.get(i).score;

				if (currScore < minScore) {

					minScore = currScore;
					// System.out.println("minScore:"+minScore+" "+score);
					minIndex = i;
				}
			}
			// System.out.println("score="+score);
			// System.out.println("minscore="+minScore);

			// min found replace it if new score is better
			if (minScore < score) {
				// System.out.println("BetterMin:"+score);
				poolOfSolutions.set(minIndex, new Actions(score, currentSolution));
				if (score > maxScore) {
					maxScore = score;
					// System.out.println("MAAAAAX "+score+"
					// "+currentState.p1.p2);
					maxIndex = minIndex;
				}
			}
		}
		// System.out.println("Nb Iteration=" + count +"
		// "+poolOfSolutions.get(maxIndex).score);
		return poolOfSolutions.get(maxIndex).actions;
	}

	static Vector<Actions> poolOfSolutions2 = null;
	static long maxTime = 70; // to adjust\
	static int sizeOfPool = 20; // 40; // sizeOfPool
	static boolean initialMutate = true;

	static List<Action[]> SimulateAllTurns2(State initialState) {

              // Adust here


              State currentState = new State(initialState);

              long timer = System.currentTimeMillis();

              double maxScore = Double.NEGATIVE_INFINITY;
              int maxIndex = 0;

              boolean firstTime = false;
              int beg = 0;
              if (poolOfSolutions2 == null)
              {
                     firstTime = true;
                     poolOfSolutions2 = new Vector<Actions>();
                     poolOfSolutions2.setSize(sizeOfPool);
              }
              else
              {
                     Collections.sort(poolOfSolutions2);
                     beg = sizeOfPool / 2;
              }



              double score = Double.NEGATIVE_INFINITY;
              // start at beg in case we restart 
              // first half is the best from previous path to which we remove consumed element from previous turn and add a new turn at the end
              // and then created from scratch second set of elements
              
              for (int i = 0; i < beg; i++)
              {
                     poolOfSolutions2.get(i).removeFirstDupeLast();
              }
              for (int i = beg; i < sizeOfPool; i++) {
                     currentState = new State(initialState);
                     List<Action[]> actions = generateInitialAction(currentState);
                     if (initialMutate) {
                           actions = mutate(currentState, actions);
                     }

                     for (Action[] action : actions) {
                           //System.out.println(action[1].angle+" "+action[1].thrust);
                           currentState.simulate(action);
                     }
                     score = currentState.score();
                     if (score > maxScore) {
                           //System.out.println("MAAAAAX "+score+" "+currentState.p1.p2);
                           maxScore = score;
                           maxIndex = i;
                     }
                     poolOfSolutions2.set(i, new Actions(score, actions));
              }
              //System.out.println("On mute a fond");
              int randomInPool = 0;
              List<Action[]> currentSolution = null;
              int count = 0;
              while (System.currentTimeMillis() - timer < maxTime) {
                     count++;
                     currentState = new State(initialState);
                     randomInPool = (int) (Math.random() * sizeOfPool);
                     //System.out.println(randomInPool);
                     currentSolution = (poolOfSolutions2.get(randomInPool)).actions;
                     currentSolution = mutate(currentState, currentSolution);

                     for (Action[] action : currentSolution) {
                           //System.out.println(action[1].angle+" "+action[1].thrust);
                           currentState.simulate(action);
                     }
                     
                     score = currentState.score();
                     //System.out.println("score="+score);
                     // find the min in the vector and replace it
                     double minScore = Double.POSITIVE_INFINITY;
                     int minIndex = 0;
                     double currScore = 0;
                     for (int i = 0; i < sizeOfPool; i++) {
                           currScore = poolOfSolutions2.get(i).score;

                           if (currScore < minScore) {
                                  
                                  minScore = currScore;
                                  //System.out.println("minScore:"+minScore+" "+score);
                                  minIndex = i;
                           }
                     }
                     //System.out.println("score="+score);
                     //System.out.println("minscore="+minScore);
                     
                     // min found replace it if new score is better
                     if (minScore < score) {
                           //System.out.println("BetterMin:"+score);
                           poolOfSolutions2.set(minIndex, new Actions(score, currentSolution));
                           if (score > maxScore) {
                                  maxScore = score;
                                  //System.out.println("MAAAAAX "+score+" "+currentState.p1.p2);
                                  maxIndex = minIndex;
                           }
                     }
              }
              //System.out.println("Nb Iteration=" + count +" "+poolOfSolutions2.get(maxIndex).score);
              return poolOfSolutions2.get(maxIndex).actions;
       }
