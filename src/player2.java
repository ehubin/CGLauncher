import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class player2 {
    static Scanner in = new Scanner(System.in);
    static State current= new State(),future=null;
    
    public static void main(String args[]) {
        

        // game loop
        while (true) {
            current.readInput(in);
           //System.err.println(current);
            List<Action[]> a=SimulateAllTurns(current);
            if (future != null) {
               //if(!current.p1.equals(future.p1)) {System.err.println("CURRENT\n"+current.p1+"FUTURE\n"+future.p1);}
            } 
            //future=new State(current);
            //future.simulate(a);
            Action[] a1=a.get(0);
            System.out.println(a1[0].toString(current.p1.p1)+"\n"+a1[1].toString(current.p1.p2));
        }
    }
    
    static class PlayerState {
        
        static final int DIAG =10000*10000+8000*8000;
        int nbFlagsCaptured=0;
        int myBase=-1;
        Pod p1,p2;
        int flagx,flagy;
        PlayerState() { p1=new Pod();p2=new Pod();}
        PlayerState(PlayerState s) {
            nbFlagsCaptured=s.nbFlagsCaptured;
            p1= new Pod(s.p1);
            p2= new Pod(s.p2);
            flagx=s.flagx; flagy=s.flagy;
            myBase=s.myBase;
        }
        double score(PlayerState other) {
            double res=nbFlagsCaptured*2*DIAG;
            Pod FlagPod=null,ChasePod=null;
            if(p1.hasFlag) {
                FlagPod=p1;
                ChasePod=p2;
            } else if (p2.hasFlag) {
                FlagPod=p2;
                ChasePod=p1;
            }
            if(FlagPod != null) {
                res += 2*DIAG-(myBase-FlagPod.x)*(myBase-FlagPod.x);
            } else {
                if(dist2(p1,flagx,flagy) > dist2(p2,flagx,flagy)) {
                    FlagPod=p2; ChasePod=p1;
                } else {
                    FlagPod=p1; ChasePod=p2;
                }
                res+=DIAG-dist2(FlagPod,flagx,flagy); // get close to enemy flag
                
            }
            if(other.p1.hasFlag) {
                res+=DIAG-dist2(ChasePod,other.p1);
            } else if(other.p2.hasFlag) {
                res+=DIAG-dist2(ChasePod,other.p2);
            } else {
                res+=DIAG-dist2(ChasePod,other.flagx,other.flagy);
            }

               
            //System.err.println("Score="+res);
            return res;  
        }
        public boolean equals(Object o) {
            PlayerState ps= (PlayerState)o;
            return p1.equals(ps.p1) && p2.equals(ps.p2) && myBase==ps.myBase;
        }
        public String toString() { 
            StringBuffer sb=new StringBuffer();
            sb.append("nbFlags="+nbFlagsCaptured+",myBase="+myBase+"\n");
            sb.append("Flagx="+flagx+",flagy="+flagy+"\n");
            sb.append(p1+"\n");
            sb.append(p2+"\n");
            return sb.toString();
        }
    }
    static class State {
        static final int BASE_LEFT=0,BASE_RIGHT=10000;
        PlayerState p1,p2;
        double score() {
            return p1.score(p2)-p2.score(p1);
        }
        State() {p1=new PlayerState(); p2=new PlayerState();}
        State(State s)  {p1=new PlayerState(s.p1); p2=new PlayerState(s.p2);}
            
        void readInput(Scanner s) {
            p2.flagx=s.nextInt();
            p2.flagy=s.nextInt();
            p1.flagx=in.nextInt();
            p1.flagy=in.nextInt();
            p1.p1.initPod(s);
            p1.p2.initPod(s);
            p2.p1.initPod(s);
            p2.p2.initPod(s);
            
            //TODO init base
            if(p1.myBase==-1) {p1.myBase=(p2.flagx>5000?BASE_RIGHT:BASE_LEFT); p2.myBase= (p1.myBase==BASE_LEFT?BASE_RIGHT:BASE_LEFT);}
        }
        void simulate(Action[] a) {
            p1.p1.move(a[0],this);
            p1.p2.move(a[1],this);
            p2.p1.move(a[2],this);
            p2.p2.move(a[3],this);
        }
        public boolean equals(Object o) { State s=(State)o; return p1.equals(s.p1)&&p2.equals(s.p2);}
        public String toString() { return p1.toString()+"\n"+p2.toString();}
        
    }
    static class Pod extends Point{
        double vx;
        double vy;
        int boost=0;
        boolean hasFlag=false;
        boolean canBoost() { return !hasFlag && boost==0;}
        void initPod(Scanner s) {
            x=s.nextInt();
            y=s.nextInt();
            vx=s.nextInt();
            vy=s.nextInt();
            hasFlag=in.nextInt()==1;
        }
        Pod() {}
        Pod(Pod p) {
            x=p.x;
            y=p.y;
            vx=p.vx;
            vy=p.vy;
            hasFlag=p.hasFlag;
        }
        void move(Action a,State s) {
           
           
            double vtx=a.thrust*Math.cos(a.angle);
            double vty=a.thrust*Math.sin(a.angle);
            vx+=vtx;vy+=vty;
            x+=vx;y+=vy;
            vx*=0.9;vy*=0.9;
            x = Math.rint(x);
			y = Math.rint(y);
            vx=(int)vx;vy=(int)vy;
        }
        public String toString() {
            return (int)x+" "+(int)y+" "+(int)vx+" "+(int)vy+" "+hasFlag;
        }
        public boolean equals(Object o) {
            Pod p=(Pod)o;
            return p.x==x && p.y==y && p.vx==vx && p.vy==vy && p.hasFlag==hasFlag;
        }
        final static int SR=2*400*400;
        
        double wallCollision() { // return -1 if no wall collision or time of wall collision
            return 0;
        }
        Collision collision(Pod u) {
            // Distance carré
            double dist = dist2(this,u);
        
        
            // On prend tout au carré pour éviter d'avoir à appeler un sqrt inutilement. C'est mieux pour les performances
        
            if (dist < SR) {
                // Les objets sont déjà l'un sur l'autre. On a donc une collision immédiate
                return new Collision(this, u, 0.0);
            }
        
            // Optimisation. Les objets ont la même vitesse ils ne pourront jamais se rentrer dedans
            if (this.vx == u.vx && this.vy == u.vy) {
                return null;
            }
        
            // On se met dans le référentiel de u. u est donc immobile et se trouve sur le point (0,0) après ça
            double x = this.x - u.x;
            double y = this.y - u.y;
            Point myp = new Point(x, y);
            double vx = this.vx - u.vx;
            double vy = this.vy - u.vy;
            Point up = new Point(0, 0);
        
            // On cherche le point le plus proche de u (qui est donc en (0,0)) sur la droite décrite par notre vecteur de vitesse
            Point p = up.closest(myp, new Point(x + vx, y + vy));
        
            // Distance au carré entre u et le point le plus proche sur la droite décrite par notre vecteur de vitesse
            double pdist = up.distance2(p);
        
            // Distance au carré entre nous et ce point
            double mypdist = this.distance2(p);
        
            // Si la distance entre u et cette droite est inférieur à la somme des rayons, alors il y a possibilité de collision
            if (pdist < SR) {
                // Notre vitesse sur la droite
                double length = Math.sqrt(vx*vx + vy*vy);
        
                // On déplace le point sur la droite pour trouver le point d'impact
                double backdist = Math.sqrt(SR - pdist);
                p.x = p.x - backdist * (vx / length);
                p.y = p.y - backdist * (vy / length);
        
                // Si le point s'est éloigné de nous par rapport à avant, c'est que notre vitesse ne va pas dans le bon sens
                if (myp.distance2(p) > mypdist) {
                    return null;
                }
        
                pdist = p.distance(myp);
        
                // Le point d'impact est plus loin que ce qu'on peut parcourir en un seul tour
                if (pdist > length) {
                    return null;
                }
        
                // Temps nécessaire pour atteindre le point d'impact
                double t = pdist / length;
        
                return new Collision(this, u, t);
            }
        
            return null;
        }
    }
    static class Point {
        double x,y;
        Point() {};
        Point(double x1,double y1) {x=x1;y=y1;}
        double distance2(Point p) {double x1=x-p.x,y1=y-p.y; return x1*x1+y1*y1;}
        double distance(Point p) { return Math.sqrt(distance2(p));}
        Point closest(Point a, Point b) {
            double da = b.y - a.y;
            double db = a.x - b.x;
            double c1 = db*a.x + db*a.y;
            double c2 = -db*this.x + da*this.y;
            double det = da*da + db*db;
            double cx = 0;
            double cy = 0;
        
            if (det != 0) {
                cx = (da*c1 - db*c2) / det;
                cy = (da*c2 + db*c1) / det;
            } else {
                // Le point est déjà sur la droite
                cx = this.x;
                cy = this.y;
            }
        
            return new Point(cx, cy);
        }
    }
    static class Collision {
        Pod p1,p2;
        double t;
        Collision(Pod p1,Pod p2,double t) { this.p1=p1;this.p2=p2;this.t=t;}
    }
    
    static class Action {
        double angle;
        int thrust;
        boolean boost=false;
        public Action(double angle,int t) {this.angle=angle;this.thrust=t;}
        public String toString(Pod p) {
            return (int)(p.x+10000*Math.cos(angle))+" "+(int)(p.y+10000*Math.sin(angle))+" "+(thrust==500?"BOOST":thrust);
        }
    }
    
    static class Actions {
        double score;
        List<Action[]> actions = null;
        

        public Actions(double score, List<Action[]> actions)
        {this.score=score;this.actions=actions;}

    }
    
 
    
  
static List<Action[]> SimulateAllTurns(State initialState)
    {

     // Adust here 
     long maxTime = 70; // to adjust\
     int sizeOfPool = 40; // sizeOfPool
     boolean initialMutate = true;
     
     
     State currentState = new State(initialState);
     
     long timer= System.currentTimeMillis();

     
	double maxScore = Double.MIN_VALUE;
	int maxIndex = 0;

     Vector<Actions> poolOfSolutions = new Vector<Actions>();
     poolOfSolutions.setSize(sizeOfPool);
     
     double score = Double.MIN_VALUE;
     for (int i =0; i<sizeOfPool; i++)
     {
     List<Action[]> actions = generateInitialAction(initialState);
     if (initialMutate)
     {
     actions = mutate(initialState, actions);
     }
     
       for (Action[] action : actions)
     {
       currentState.simulate(action);
     }
     score = currentState.score();
     if (score>maxScore)
     {
     maxScore=score;
     maxIndex=i;
     }
     poolOfSolutions.set(i, new Actions(score, actions));
     }
   
     int randomInPool = 0;
     List<Action[]> currentSolution = null;
     int count=0;
     while (System.currentTimeMillis() - timer < maxTime)
     {
         count++;
     currentState = new State(initialState);
     randomInPool = (int) (Math.random() * sizeOfPool);
     currentSolution = (poolOfSolutions.get(randomInPool)).actions;
     currentSolution = mutate(currentState, currentSolution);
     
     for (Action[] action : currentSolution)
     {
     currentState.simulate(action);
     }     
     score = currentState.score();
     // find the min in the vector and replace it
     double minScore = Double.MAX_VALUE;
     int minIndex = 0;
     double currScore = 0;
     for(int i = 0; i<sizeOfPool; i++)
     {
     currScore = poolOfSolutions.get(i).score;
     
     if (currScore < minScore)
     {
     minScore = currScore;
     minIndex = i;
     }
     }
     // min found replace it if new score is better
     if (minScore < score)
     {
     poolOfSolutions.set(minIndex, new Actions(score,currentSolution));
         if (score>maxScore)
         {
         maxScore=score;
         maxIndex=minIndex;
         }
     }
     
       }
     //System.err.println("Nb Iteration="+count);
	return poolOfSolutions.get(maxIndex).actions;
    } 

/**
	* Flag1 is the flag p0 and p1 try to catch
	*
	*/

static List<Action[]> generateInitialAction(
	State state/*
	* Pod[] p, int flagx1, int flagy1, int flagx2, int
	* flagy2, int base
	*/) {
	List<Action[]> result = new ArrayList<>();
	
	
	PlayerState me=state.p1;
	PlayerState en=state.p2;
	
	
	 int FlagPod=-1,ChasePod=-1;
	 double[] angle = new double[4];
	 
            if(me.p1.hasFlag) {
                FlagPod=0;
                ChasePod=1;
            } else if (me.p2.hasFlag) {
                FlagPod=1;
                ChasePod=0;
            }
            if(FlagPod != -1) {
               angle[FlagPod]=me.myBase <5000? Math.PI:0;
            } else { // no flag pod
                if(dist2(me.p1,me.flagx,me.flagy) > dist2(me.p2,me.flagx,me.flagy)) {
                    FlagPod=1; ChasePod=0;
                } else {
                    FlagPod=0; ChasePod=1;
                }
                Pod p=FlagPod==0?me.p1:me.p2;
                 angle[FlagPod]=computeAngle(p.x, p.y, me.flagx,me.flagy);// get close to enemy flag
                
            }
             Pod p = ChasePod==0?me.p1:me.p2;
            if(en.p1.hasFlag) {
               
               angle[ChasePod]= computeAngle(p.x, p.y, (int)en.p1.x,(int)en.p1.y);
            } else if(en.p2.hasFlag) {
               angle[ChasePod]= computeAngle(p.x, p.y,(int) en.p2.x,(int)en.p2.y);
            } else {
               angle[ChasePod]= computeAngle(p.x, p.y, en.flagx,en.flagy);
            }
	angle[2]=0;
	angle[3]=0;
	
	for(int i=0;i<4;++i) {
	   // System.err.println(angle[i]*180/Math.PI);
	}
	
	
	for (int i = 0; i < MAX_ACTION_DEPTH; i++) {
    	int trust0 = 100;// On fout la patate
    	Action action0 = new Action(angle[0], trust0);
    	Action action1 = new Action(angle[1], trust0);
    	Action action2 = new Action(angle[2], trust0);
    	Action action3 = new Action(angle[3], trust0);
    	Action[] actionPerPod = new Action[] { action0, action1, action2, action3 };
    	result.add(actionPerPod);
	}
	return result;

	} 



private static double computeAngle(double x, double y, int flagx1, int flagy1) {
	double deltaX = flagx1 - x;
	double deltaY = flagy1 - y;
	return Math.atan2(deltaY,  deltaX);
	}


static List<Action[]> mutate(State state, List<Action[]> actionsPerStep) {
    
	List<Action[]> result = actionsPerStep;//actionsPerStep;//new ArrayList<>();
//	for (int i = 0; i < actionsPerStep.size(); i++) {
//    	Action[] newStep = new Action[4];
//    	newStep[0] = mutateOneAction(
//    	state.p1.p1.boost - i/* we check the next boost level */, actionsPerStep.get(i)[0],
//    	AMPLITUDE_DECREASE_FACTOR[i]);
//    	newStep[1] = mutateOneAction(
//    	state.p1.p2.boost - i/* we check the next boost level */, actionsPerStep.get(i)[1],
//    	AMPLITUDE_DECREASE_FACTOR[i]);
//    	newStep[2]=new Action(0,0);
//    	newStep[3]=new Action(0,0);
//    	result.add(newStep);
//	}
	return result;
} 


private static Action mutateOneAction(int boostState, Action action, double amplitude) {

	double angleMax = action.angle + ANGLE_MAX_PITCH * amplitude;
	double angleMin = action.angle - ANGLE_MAX_PITCH * amplitude;
	double angle = angleMin+Math.random()*(angleMax-angleMin);

	int thrustMax = action.thrust + (int) (amplitude * THRUST_MAX_PITCH);
	int thrustMin = Math.max(0, action.thrust - (int) (amplitude * THRUST_MAX_PITCH));
	int thrust = thrustMin + (int) (Math.random() * (thrustMax - thrustMin));
	if (thrust > 100 && boostState > 0) {// I am not able to boost
	thrust = 100;
	} else if (thrust > 100 && boostState <= 0) {
	thrust = 500;
	}
	return new Action(angle, thrust);
	}


static Point get_line_intersection(double p0_x, double p0_y, double p1_x, double p1_y, 
    double p2_x, double p2_y, double p3_x, double p3_y)
{
    double s02_x, s02_y, s10_x, s10_y, s32_x, s32_y, s_numer, t_numer, denom, t;
    s10_x = p1_x - p0_x;
    s10_y = p1_y - p0_y;
    s32_x = p3_x - p2_x;
    s32_y = p3_y - p2_y;

    denom = s10_x * s32_y - s32_x * s10_y;
    if (denom == 0)
        return null; // Collinear
    boolean denomPositive = denom > 0;

    s02_x = p0_x - p2_x;
    s02_y = p0_y - p2_y;
    s_numer = s10_x * s02_y - s10_y * s02_x;
    if ((s_numer < 0) == denomPositive)
        return null; // No collision

    t_numer = s32_x * s02_y - s32_y * s02_x;
    if ((t_numer < 0) == denomPositive)
        return null; // No collision

    if (((s_numer > denom) == denomPositive) || ((t_numer > denom) == denomPositive))
        return null; // No collision
    // Collision detected
    t = t_numer / denom;
    Point res = new Point();
    res.x = p0_x + (t * s10_x);
    res.y = p0_y + (t * s10_y);

    return res;
}

private static final double[] AMPLITUDE_DECREASE_FACTOR = { 1, 0.8, 0.6, 0.4, 0.2 };
	private static final double ANGLE_MAX_PITCH = Math.PI/32;
	private static final int THRUST_MAX_PITCH = 40; 

private static final int MAX_ACTION_DEPTH =5;
    
    
   
    static double dist2(Pod p1,int fx,int fy)  { double x=(p1.x-fx),y=p1.y-fy; return x*x+y*y;}
    static double dist2(Pod p1,Pod p2)  { double x=(p1.x-p2.x),y=p1.y-p2.y; return x*x+y*y;}
}
