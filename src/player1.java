import java.util.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;




/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class player1 {
    static Scanner in = new Scanner(System.in);
    static State current= new State(),future=null;
    
    public static void main(String args[]) {
        

        // game loop
        while (true) {
            current.readInput(in);
           //System.err.println(current);
            List<Action[]> a=SimulateAllTurns(current);
            if (future != null) {
               if(!current.p1.equals(future.p1)) {System.err.println("CURRENT\n"+current.p1+"FUTURE\n"+future.p1);}
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
        PlayerState() { p1=new Pod(this);p2=new Pod(this);}
        PlayerState(PlayerState s) {
            nbFlagsCaptured=s.nbFlagsCaptured;
            p1= new Pod(s.p1);
            p2= new Pod(s.p2);
            flagx=s.flagx; flagy=s.flagy;
            myBase=s.myBase;
        }
        void captureFlag(Pod winner) { //implement flag capture for thsi player
        	flagx=-1;
        	flagy=-1;
        	winner.hasFlag=true;
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
            	res += 10 * (DIAG - dist2(FlagPod,myBase, (int)FlagPod.y));
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
    static final int BASE_LEFT=1000,BASE_RIGHT=9000;
    static class State {
        ArrayList<ChangeListener> ll=null;
        PlayerState p1,p2;
        int turn=0;
        void addChangeListener(ChangeListener cl) { 
        	if(ll==null) ll=new ArrayList<ChangeListener>();
        	ll.add(cl);
        }
        double score() {
            return p1.score(p2)-p2.score(p1);
        }
		double score(boolean improveAlly) {
			if (improveAlly)
				return p1.score(p2) - p2.score(p1);
			else
				return p2.score(p1) - p1.score(p2);
		}
        State() {p1=new PlayerState(); p2=new PlayerState();}
        State(State s)  {p1=new PlayerState(s.p1); p2=new PlayerState(s.p2); turn=s.turn;}
            
        void readInput(Scanner s) {
            p2.flagx=s.nextInt();
            p2.flagy=s.nextInt();
            p1.flagx=s.nextInt();
            p1.flagy=s.nextInt();
            p1.p1.initPod(s,p1);
            p1.p2.initPod(s,p1);
            p2.p1.initPod(s,p2);
            p2.p2.initPod(s,p2);
            
            //TODO init base
            if(p1.myBase==-1) {p1.myBase=(p2.flagx>5000?BASE_LEFT:BASE_RIGHT); p2.myBase= (p1.myBase==BASE_LEFT?BASE_RIGHT:BASE_LEFT);}
        }
        void simulate(Action[]a) {simulate(a,false);}
        void simulate(Action[] a,boolean var) {
        	//System.err.println("Start simulate");
        	p1.p1.updateSpeed(a[0]);
            p1.p2.updateSpeed(a[1]);
            p2.p1.updateSpeed(a[2]);
            p2.p2.updateSpeed(a[3]);
            Pod[] pods=new Pod[] {p1.p1,p1.p2,p2.p1,p2.p2};
            // Il faut conserver le temps où on en est dans le tour. Le but est d'arriver à 1.0
            double t = 0.0,t1=-1,t2=-1;
            Collision lastCollision=null;
            while (t < 1.0) {
            	if(t==t1&& t1==t2) {
            		System.err.println("Infinite loop detected");
            		break;
            	}
            	t2=t1;t1=t;
            	//System.err.println("t= "+t);
                Collision firstCollision = null;

                // On cherche toutes les collisions qui vont arriver pendant ce tour
                for (int i = 0; i < pods.length; ++i) {
                	//System.err.println("pod "+i);
                	Collision w=pods[i].getWall(t,lastCollision);
                	if (w != null && w.t + t < 1.0 && (firstCollision == null || w.t < firstCollision.t)) {
                        firstCollision = w;
                    }
                	//System.err.println("After wall");
                    // Collision avec un autre pod ?
                    for (int j = i + 1; j < pods.length; ++j) {
                        Collision col = pods[i].collision2(pods[j],lastCollision) ;

                        // Si la collision arrive plus tôt que celle qu'on, alors on la garde
                        if (col != null && col.t + t < 1.0 && (firstCollision == null || col.t < firstCollision.t)) {
                            firstCollision = col;
                        }
                    }
                    //System.err.println("After col loop pod");

                    // flag
                    PlayerState curPlayer = i<2? p1:p2;
                    double flagT= pods[i].catchFlag(curPlayer.flagx,curPlayer.flagy);

                    // Si la collision arrive plus tôt que celle qu'on, alors on la garde
                    if (flagT>=0 && flagT + t < 1.0 && (firstCollision == null || flagT < firstCollision.t)) {
                        curPlayer.captureFlag(pods[i]);
                    }
                    //System.err.println("After flag");
                }

                if (firstCollision == null) {
                    // Aucune collision, on peut juste déplacer les pods jusqu'à la fin du tour
                    for (int i = 0; i < pods.length; ++i) {
                        pods[i].simpleMove(1.0 - t);
                    }
                    if(ll!= null) {ChangeEvent evt = new ChangeEvent(this); for(ChangeListener c:ll) c.stateChanged(evt);}
                    // Fin du tour
                    t = 1.0;
                } else {
                	lastCollision=firstCollision;
                	//System.err.println(firstCollision);
                    // On bouge les pods du temps qu'il faut pour arriver sur l'instant `t` de la collision
                    for (int i = 0; i < pods.length; ++i) {
                        pods[i].simpleMove(firstCollision.t);
                    }
                    if(ll!= null) {ChangeEvent evt = new ChangeEvent(this); for(ChangeListener c:ll) c.stateChanged(evt);}
                    //System.err.println("Resolving "+firstCollision);
                    // On joue la collision
                    firstCollision.execute();
                    if(ll!= null) {ChangeEvent evt = new ChangeEvent(this); for(ChangeListener c:ll) c.stateChanged(evt);}
                    t += firstCollision.t;
                }
            } //while t<1

            // On arrondi les positions et on tronque les vitesses pour tout le monde
            for (int i = 0; i < pods.length; ++i) {
                pods[i].endTurn();
                //System.err.println(pods[i]);
            }
            turn++;
        }
        public boolean equals(Object o) { State s=(State)o; return p1.equals(s.p1)&&p2.equals(s.p2);}
        public String toString() { return "Turn "+turn+"\n"+p1.toString()+"\n"+p2.toString();}
        public String toInitString() {
        	StringBuffer sb= new StringBuffer();
        	sb.append(p2.flagx+" "+p2.flagy+"\\n");
        	sb.append(p1.flagx+" "+p1.flagy+"\\n");
        	sb.append(p1.p1+"\\n");
        	sb.append(p1.p2+"\\n");
        	sb.append(p2.p1+"\\n");
        	sb.append(p2.p2+"\\n");
        	return sb.toString();
        }
        
    }
    static class Pod extends Point{
        double vx;
        double vy;
        int boost=0;
        boolean hasFlag=false;
        PlayerState ps=null;
        boolean canBoost() { return !hasFlag && boost==0;}
        void initPod(Scanner s,PlayerState ps) {
            x=s.nextInt();
            y=s.nextInt();
            vx=s.nextInt();
            vy=s.nextInt();
            hasFlag=s.nextInt()==1;
            this.ps=ps;
        }
        Pod() {}
        Pod(PlayerState ps) {this.ps=ps;}
        Pod(Pod p) {
            x=p.x;
            y=p.y;
            vx=p.vx;
            vy=p.vy;
            hasFlag=p.hasFlag;
            ps=p.ps;
        }
        void updateSpeed(Action a){
        	double vtx=a.thrust*Math.cos(a.angle);
            double vty=a.thrust*Math.sin(a.angle);
            vx+=vtx;vy+=vty;
        }
        void simpleMove(double t) {
        	x+= vx*t; y+=vy*t;
        	//System.err.println("simplemove "+super.toString()+"==> "+ps);
        	if(hasFlag) {
        		if( ps!= null && ((ps.myBase == BASE_LEFT && x <= BASE_LEFT)|| (ps.myBase==BASE_RIGHT && x>= BASE_RIGHT)) ) {
        			ps.nbFlagsCaptured++;
        			ps.flagx=ps.myBase==BASE_LEFT ? BASE_RIGHT :BASE_LEFT;
        			ps.flagy=4000;
        			hasFlag=false;
        			System.err.println("Flag captured");
        		}
        		
        	}
        }
        void endTurn() {
        	vx*=0.9;vy*=0.9;
            x = Math.rint(x);
			y = Math.rint(y);
            vx=(int)vx;vy=(int)vy;
        }
        void move(Action a,State s,PlayerState ps) {
        	Pod save=new Pod(this);
           
            double vtx=a.thrust*Math.cos(a.angle);
            double vty=a.thrust*Math.sin(a.angle);
            vx+=vtx;vy+=vty;
            
            if(hasFlag && (x+vx <=1000 || x+vx >= 9000)) {
            	ps.flagx=ps.myBase == BASE_LEFT?BASE_RIGHT:BASE_LEFT;
            	ps.flagy=4000;
            	hasFlag=false;
            	ps.nbFlagsCaptured++;
            }
            double t=0.0,colT;
            while((colT=getWall(t,ps)) >=0) {
            	t=colT;
            }
            x+=vx*(1.0-t);
        	y+=vy*(1.0-t);
        
            vx*=0.9;vy*=0.9;
            x = Math.rint(x);
			y = Math.rint(y);
			if(x<400||x>9600||y<400||y>7600) {
				System.err.println("Sorti "+this+"\n"+save+"\n"+a);
			}
            vx=(int)vx;vy=(int)vy;
        }
        
        
        Collision getWall(double t,Collision last) {
        	Wall lastw = (last!= null && last.p1 == this && last.p2==null ) ? last.wall : Wall.NONE; // derive last wall if any to avoid double collision
        	double smallest=2.0;
        	double nx=x+vx*(1.0-t),ny=y+vy*(1.0-t);
        	if(x==400 && nx<400 && lastw !=Wall.LEFT) 	{ return Collision.wallCollision(t, Wall.LEFT, this);}
        	if(x==9600 && nx>9600 && lastw !=Wall.RIGHT) 	{ return Collision.wallCollision(t, Wall.RIGHT, this);}
        	if ( (y==400 && ny <400) && lastw !=Wall.TOP)  { return Collision.wallCollision(t, Wall.TOP, this);}
        	if( (y==7600 && ny >7600) && lastw !=Wall.BOTTOM) {return Collision.wallCollision(t, Wall.BOTTOM, this);}
        	
        	Wall best=Wall.NONE;
            double col=get_line_intersection(x,y,nx,ny,400, 400, 9600, 400);
            if(col>0 && col <smallest && lastw !=Wall.TOP) {smallest=col;best=Wall.TOP;}
            col = get_line_intersection(x,y,nx,ny,400, 400, 400, 7600);
            if(col>0 && col <smallest && lastw !=Wall.LEFT) {smallest=col;best=Wall.LEFT;}
            col = get_line_intersection(x,y,nx,ny,9600, 400, 9600, 7600);
            if(col>0 && col <smallest && lastw !=Wall.RIGHT) {smallest=col;best=Wall.RIGHT;}
            col = get_line_intersection(x,y,nx,ny,400, 7600, 9600, 7600);
            if(col>0 && col <smallest && lastw !=Wall.BOTTOM) {smallest=col;best=Wall.BOTTOM;}
            
            if(best!= Wall.NONE) return Collision.wallCollision((1.0-t)*smallest,best,this);
            return null;
        }
        double getWall(double t,PlayerState ps) {
        	double smallest=2.0;
        	double nx=x+vx*(1.0-t),ny=y+vy*(1.0-t);
        	if(x==400 && nx<400) 	{vx=-vx; return t;}
        	if(x==9600 && nx>9600) 	{vx=-vx; return t;}
        	if ( (y==400 && ny <400) || (y==7600 && ny >7600)) {vy=-vy; return t;}
            int best=-1;
            double flagT=catchFlag(ps.flagx,ps.flagy);
            double col=get_line_intersection(x,y,nx,ny,400, 400, 9600, 400);
            if(col>0 && col <smallest) {smallest=col;best=0;}
            col = get_line_intersection(x,y,nx,ny,400, 400, 400, 7600);
            if(col>0 && col <smallest) {smallest=col;best=1;}
            col = get_line_intersection(x,y,nx,ny,9600, 400, 9600, 7600);
            if(col>0 && col <smallest) {smallest=col;best=2;}
            col = get_line_intersection(x,y,nx,ny,400, 7600, 9600, 7600);
            if(col>0 && col <smallest) {smallest=col;best=3;}
            
            if(best>=0) {
            	if(flagT >= 0 && flagT <=smallest) {
            		ps.captureFlag(this);
            	}
            	x+=vx*(1.0-t)*smallest;y+=vy*(1.0-t)*smallest;
            	switch(best) {
            		case 0:
            		case 3:
            			vy=-vy;
            			break;
            		case 1:
            		case 2:
            			vx=-vx;
	            		break;
            	}
            	//System.err.println("Collision"+(t+(1.0-t)*smallest));
            	return t+(1.0-t)*smallest;
            }
            if(flagT >= 0 && flagT<=1) { ps.captureFlag(this);}
            return -1;  
        }
        public String toString() {
            return (int)x+" "+(int)y+" "+(int)vx+" "+(int)vy+" "+(hasFlag?1:0);
        }
        public boolean equals(Object o) {
            Pod p=(Pod)o;
            return p.x==x && p.y==y && p.vx==vx && p.vy==vy && p.hasFlag==hasFlag;
        }
        final static int SR=4*400*400;
        
        Collision collision2(Pod u,Collision last) {
        	double dist = dist2(this,u);
        	if (dist < SR) { // Les objets sont déjà l'un sur l'autre. On a donc une collision immédiate
            	//System.err.println(last+"\n>"+this+"\n>"+u);
            	if ( !(last != null && ( (this == last.p1 &&  u==last.p2) ||( this == last.p2 && u==last.p1)) ) ) // different collision than last
            		return new Collision(this, u, 0.0);
            	//else
            		//System.err.println("Avoided fake collision");
        	}
        	double dvx=u.vx-vx,dvy=u.vy-vy,dx=u.x-x,dy=u.y-y;
        	double scal=dx*dvx+dy*dvy;
        	if(  scal>=0) { //v divergente
        		return null;
        	} else { 
        		double a=dvx*dvx+dvy*dvy;
        		double b= 2*scal;
        		double c = dist - SR;
        		double t = (-b-Math.sqrt(b*b-4*a*c))/(2*a);
        		return t<=1 ? new Collision(this,u,t):null;
        	}
        }
        
        
        Collision collision(Pod u,Collision last) {
            // Distance carré
            double dist = dist2(this,u);
            
        
            // On prend tout au carré pour éviter d'avoir à appeler un sqrt inutilement. C'est mieux pour les performances
        
            if (dist < SR) { // Les objets sont déjà l'un sur l'autre. On a donc une collision immédiate
            	//System.err.println(last+"\n>"+this+"\n>"+u);
            	if ( !(last != null && ( (this == last.p1 &&  u==last.p2) ||( this == last.p2 && u==last.p1)) ) ) // different collision than last
            		return new Collision(this, u, 0.0);
            	else
            		System.err.println("Avoided fake collision");
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
                if ( !(last != null && ( (this == last.p1 &&  u==last.p2) ||( this == last.p2 && u==last.p1)) ) ) // different collision than last
                	return new Collision(this, u, t);
            }
        
            return null;
        }
        static final int POD_FLAG=(400+150)*(400+150);
        double catchFlag(int fx,int fy) {
            if (fx==-1) return -1;// no flag to capture
        	// Distance carré
            double dist = dist2(this,fx,fy);
        
        
            // On prend tout au carré pour éviter d'avoir à appeler un sqrt inutilement. C'est mieux pour les performances
        
            if (dist < POD_FLAG) {
                // Les objets sont déjà l'un sur l'autre. On a donc une collision immédiate
                return 0.0;
            }
        
        
            // On se met dans le référentiel de u. u est donc immobile et se trouve sur le point (0,0) après ça
            double x = this.x - fx;
            double y = this.y - fy;
            Point myp = new Point(x, y);
            Point up = new Point(0, 0);
        
            // On cherche le point le plus proche de u (qui est donc en (0,0)) sur la droite décrite par notre vecteur de vitesse
            Point p = up.closest(myp, new Point(x + vx, y + vy));
        
            // Distance au carré entre u et le point le plus proche sur la droite décrite par notre vecteur de vitesse
            double pdist = up.distance2(p);
        
            // Distance au carré entre nous et ce point
            double mypdist = this.distance2(p);
        
            // Si la distance entre u et cette droite est inférieur à la somme des rayons, alors il y a possibilité de collision
            if (pdist < POD_FLAG) {
                // Notre vitesse sur la droite
                double length = Math.sqrt(vx*vx + vy*vy);
        
                // On déplace le point sur la droite pour trouver le point d'impact
                double backdist = Math.sqrt(SR - pdist);
                p.x = p.x - backdist * (vx / length);
                p.y = p.y - backdist * (vy / length);
        
                // Si le point s'est éloigné de nous par rapport à avant, c'est que notre vitesse ne va pas dans le bon sens
                if (myp.distance2(p) > mypdist) {
                    return -1;
                }
        
                pdist = p.distance(myp);
        
                // Le point d'impact est plus loin que ce qu'on peut parcourir en un seul tour
                if (pdist > length) {
                    return -1;
                }
        
                // Temps nécessaire pour atteindre le point d'impact
                double t = pdist / length;
        
                return t;
            }
        
            return -1;
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
    enum Wall { NONE,TOP,BOTTOM,LEFT,RIGHT; }
    static class Collision {
    	
    	public String toString() {
    		if(p2==null) {return "Wall("+wall+") "+p1;} 
    		else {return p1+" vs "+p2;}
    	}
    	static Collision wallCollision(double t,Wall w,Pod p) {
    		Collision res=new Collision();
    		res.t=t;
    		res.p1=p;
    		res.wall=w;
    		return res;
    	}
    	Collision() {}
        Pod p1=null,p2=null;
        Wall wall=Wall.NONE;
        double t;
        Collision(Pod p1,Pod p2,double t) { this.p1=p1;this.p2=p2;this.t=t;}
        public void execute(){
        	if(p1!= null && p2!=null) {
        		double vn1=p1.vx*p1.vx+p1.vy*p1.vy;
        		double vn2=p2.vx*p2.vx+p2.vy*p2.vy;
        		double svx=p1.vx,svy=p1.vy;
        		p1.vx=p2.vx;p1.vy=p2.vy;
        		p2.vx=svx;p2.vy=svy;
        		
        		if(p1.ps != p2.ps) {
        			if(p1.hasFlag && vn1 < vn2) { 
        				p1.hasFlag=false;
        				p1.ps.flagx= p1.ps.myBase == BASE_RIGHT ? BASE_LEFT:BASE_RIGHT;
        				p1.ps.flagy= 4000;
        			}
        			if(p2.hasFlag && vn2 < vn1) {
        				p2.hasFlag=false;
        				p2.ps.flagx= p2.ps.myBase == BASE_RIGHT ? BASE_LEFT:BASE_RIGHT;
        				p2.ps.flagy= 4000;
        			}
        		}

        	} else { //Wall collision
        		if(wall==Wall.LEFT || wall == Wall.RIGHT) p1.vx=-p1.vx;
        		else p1.vy=-p1.vy;
        	}
        }
    }
    
    
	static class Actions implements Comparable<Actions>{
		double score;
		List<Action[]> actions = null;

		public Actions(double score, List<Action[]> actions) {
			this.score = score;
			this.actions = actions;
		}
		public int compareTo(Actions o) {
			if (o.score == score)
				return 0;
			else if (o.score>score)
			{
				return -1;
			}
			else 
				return 1;
		};
		
		public void removeFirstDupeLast()
		{
			//get the last element add it again and remove first element

			Action[] last = actions.get(actions.size()-1);
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
				actions = mutate(currentState, actions, true);
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
			poolOfSolutions.set(i, new Actions(score, actions));
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
			currentSolution = (poolOfSolutions.get(randomInPool)).actions;
			currentSolution = mutate(currentState, currentSolution, true);

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
				currScore = poolOfSolutions.get(i).score;

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
				poolOfSolutions.set(minIndex, new Actions(score, currentSolution));
				if (score > maxScore) {
					maxScore = score;
					//System.out.println("MAAAAAX "+score+" "+currentState.p1.p2);
					maxIndex = minIndex;
				}
			}
		}
		//System.out.println("Nb Iteration=" + count +" "+poolOfSolutions.get(maxIndex).score);
		return poolOfSolutions.get(maxIndex).actions;
	}

	
	static Vector<Actions> poolOfSolutions2 = null;
	static long maxTime = 70; // to adjust\
	static long enemyTime = 25;
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
			Actions actions = poolOfSolutions2.get(i);
			actions.removeFirstDupeLast();
			currentState = new State(initialState);


			for (Action[] action : actions.actions) {
				//System.out.println(action[1].angle+" "+action[1].thrust);
				currentState.simulate(action);
			}
			score = currentState.score();
			actions.score = score;
			if (score > maxScore) {
				//System.out.println("MAAAAAX "+score+" "+currentState.p1.p2);
				maxScore = score;
				maxIndex = i;
			}
			//poolOfSolutions2.set(i, new Actions(score, actions));
		}
		for (int i = beg; i < sizeOfPool; i++) {
			currentState = new State(initialState);
			List<Action[]> actions = generateInitialAction(currentState);

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
			currentSolution = mutate(currentState, currentSolution, true);

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
	
	static List<Action[]> SimulateAllTurns3(State initialState) {

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
			Actions actions = poolOfSolutions2.get(i);
			actions.removeFirstDupeLast();
			currentState = new State(initialState);


			for (Action[] action : actions.actions) {
				//System.out.println(action[1].angle+" "+action[1].thrust);
				currentState.simulate(action);
			}
			score = currentState.score();
			actions.score = score;
			if (score > maxScore) {
				//System.out.println("MAAAAAX "+score+" "+currentState.p1.p2);
				maxScore = score;
				maxIndex = i;
			}
			//poolOfSolutions2.set(i, new Actions(score, actions));
		}
		for (int i = beg; i < sizeOfPool; i++) {
			currentState = new State(initialState);
			List<Action[]> actions = generateInitialAction(currentState);

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
		long currentTime = System.currentTimeMillis() - timer;
		boolean improveAlly = false; // we start by the enemy
		while (currentTime < maxTime) {
			currentTime = System.currentTimeMillis() - timer;
			if (currentTime > enemyTime) // we have spent enough time to work on the enemy now work on our own
				improveAlly = true;
			count++;
			currentState = new State(initialState);
			randomInPool = (int) (Math.random() * sizeOfPool);
			//System.out.println(randomInPool);
			currentSolution = (poolOfSolutions2.get(randomInPool)).actions;
			currentSolution = mutate(currentState, currentSolution, improveAlly);

			for (Action[] action : currentSolution) {
				//System.out.println(action[1].angle+" "+action[1].thrust);
				currentState.simulate(action);
			}
			
			score = currentState.score(improveAlly);
			
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
	
	static List<Action[]> SimulateAllTurns4(State initialState) {

		// Adust here

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
				actions = mutate(currentState, actions, true);
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
			poolOfSolutions.set(i, new Actions(score, actions));
		}
		//System.out.println("On mute a fond");
		int randomInPool = 0;
		List<Action[]> currentSolution = null;
		int count = 0;
		long currentTime = System.currentTimeMillis() - timer;
		boolean improveAlly = false; // we start by the enemy
		while (currentTime < maxTime) {
			currentTime = System.currentTimeMillis() - timer;
			if (currentTime > enemyTime) // we have spent enough time to work on the enemy now work on our own
				improveAlly = true;
			count++;
			currentState = new State(initialState);
			randomInPool = (int) (Math.random() * sizeOfPool);
			//System.out.println(randomInPool);
			currentSolution = (poolOfSolutions.get(randomInPool)).actions;
			currentSolution = mutate(currentState, currentSolution, improveAlly);

			for (Action[] action : currentSolution) {
				//System.out.println(action[1].angle+" "+action[1].thrust);
				currentState.simulate(action);
			}
			
			score = currentState.score(improveAlly);
			//System.out.println("score="+score);
			// find the min in the vector and replace it
			double minScore = Double.POSITIVE_INFINITY;
			int minIndex = 0;
			double currScore = 0;
			for (int i = 0; i < sizeOfPool; i++) {
				currScore = poolOfSolutions.get(i).score;

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
				poolOfSolutions.set(minIndex, new Actions(score, currentSolution));
				if (score > maxScore) {
					maxScore = score;
					//System.out.println("MAAAAAX "+score+" "+currentState.p1.p2);
					maxIndex = minIndex;
				}
			}
		}
		//System.out.println("Nb Iteration=" + count +" "+poolOfSolutions.get(maxIndex).score);
		return poolOfSolutions.get(maxIndex).actions;	}
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

		PlayerState me = state.p1;
		PlayerState en = state.p2;

		int FlagPod = -1, ChasePod = -1;
		double[] angle = new double[4];

		if (me.p1.hasFlag) {
			FlagPod = 0;
			ChasePod = 1;
		} else if (me.p2.hasFlag) {
			FlagPod = 1;
			ChasePod = 0;
		}
		if (FlagPod != -1) {
			angle[FlagPod] = me.myBase < 5000 ? Math.PI : 0;
		} else { // no flag pod
			if (dist2(me.p1, me.flagx, me.flagy) > dist2(me.p2, me.flagx, me.flagy)) {
				FlagPod = 1;
				ChasePod = 0;
			} else {
				FlagPod = 0;
				ChasePod = 1;
			}
			Pod p = FlagPod == 0 ? me.p1 : me.p2;
			angle[FlagPod] = computeAngle(p.x, p.y, me.flagx, me.flagy);// get
																		// close
																		// to
																		// enemy
																		// flag

		}
		Pod p = ChasePod == 0 ? me.p1 : me.p2;
		if (en.p1.hasFlag) {

			angle[ChasePod] = computeAngle(p.x, p.y, (int) en.p1.x, (int) en.p1.y);
		} else if (en.p2.hasFlag) {
			angle[ChasePod] = computeAngle(p.x, p.y, (int) en.p2.x, (int) en.p2.y);
		} else {
			angle[ChasePod] = computeAngle(p.x, p.y, en.flagx, en.flagy);
		}
		
		
		
		int FlagPodE = -1, ChasePodE = -1;

		if (en.p1.hasFlag) {
			FlagPodE = 2;
			ChasePodE = 3;
		} else if (en.p2.hasFlag) {
			FlagPodE = 3;
			ChasePodE = 2;
		}
		if (FlagPodE != -1) {
			angle[FlagPodE] = en.myBase < 5000 ? Math.PI : 0;
		} else { // no flag pod
			if (dist2(en.p1, en.flagx, en.flagy) > dist2(en.p2, en.flagx, en.flagy)) {
				FlagPodE = 3;
				ChasePodE = 2;
			} else {
				FlagPodE = 2;
				ChasePodE = 3;
			}
			p = FlagPodE == 2 ? en.p1 : en.p2;
			angle[FlagPodE] = computeAngle(p.x, p.y, en.flagx, en.flagy);// get
																		// close
																		// to
																		// enemy
																		// flag

		}
		p = ChasePodE == 2 ? en.p1 : en.p2;
		if (me.p1.hasFlag) {
			angle[ChasePodE] = computeAngle(p.x, p.y, (int) me.p1.x, (int) me.p1.y);
		} else if (me.p2.hasFlag) {
			angle[ChasePodE] = computeAngle(p.x, p.y, (int) me.p2.x, (int) me.p2.y);
		} else {
			angle[ChasePodE] = computeAngle(p.x, p.y, me.flagx, me.flagy);
		}
		


//		for (int i = 0; i < 4; ++i) {
//			System.err.println(angle[i] * 180 / Math.PI);
//		}

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
		return Math.atan2(deltaY, deltaX);
	}

	static List<Action[]> mutate(State state, List<Action[]> actionsPerStep, boolean forAlly) {
//		 return actionsPerStep;

		List<Action[]> result = new ArrayList<>();

		if (forAlly)
		{
			for (int i = 0; i < actionsPerStep.size(); i++) {
				Action[] newStep = new Action[4];
				newStep[0] = mutateOneAction(
						state.p1.p1.boost - i/* we check the next boost level */, actionsPerStep.get(i)[0],
						AMPLITUDE_DECREASE_FACTOR[i]);
				newStep[1] = mutateOneAction(
						state.p1.p2.boost - i/* we check the next boost level */, actionsPerStep.get(i)[1],
						AMPLITUDE_DECREASE_FACTOR[i]);
				newStep[2] = actionsPerStep.get(i)[2];
				newStep[3] = actionsPerStep.get(i)[3];
				result.add(newStep);
			}
		}
		else
		{
			for (int i = 0; i < actionsPerStep.size(); i++) {
				Action[] newStep = new Action[4];
				newStep[0] = actionsPerStep.get(i)[0];
				newStep[1] = actionsPerStep.get(i)[1];
				newStep[2] = mutateOneAction(
						state.p2.p1.boost - i/* we check the next boost level */, actionsPerStep.get(i)[2],
						AMPLITUDE_DECREASE_FACTOR[i]);
				newStep[3] = mutateOneAction(
						state.p2.p2.boost - i/* we check the next boost level */, actionsPerStep.get(i)[3],
						AMPLITUDE_DECREASE_FACTOR[i]);

				result.add(newStep);
			}
		}
			
		return result;
	}
    
    static class Action {
    	Action() {};
        double angle;
        int thrust;
        boolean boost=false;
        public Action(double angle,int t) {this.angle=angle;this.thrust=t;}
        public String toString(Pod p) {
            return (int)(p.x+10000*Math.cos(angle))+" "+(int)(p.y+10000*Math.sin(angle))+" "+(thrust==500?"BOOST":thrust);
            //return "1000 1000 100";
        }
        public String toString() {return "Angle="+angle+" ,Thrust="+thrust;}
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


static double get_line_intersection(double p0_x, double p0_y, double p1_x, double p1_y, 
    double p2_x, double p2_y, double p3_x, double p3_y)
{
    double s02_x, s02_y, s10_x, s10_y, s32_x, s32_y, s_numer, t_numer, denom, t,res=-1;
    s10_x = p1_x - p0_x;
    s10_y = p1_y - p0_y;
    s32_x = p3_x - p2_x;
    s32_y = p3_y - p2_y;

    denom = s10_x * s32_y - s32_x * s10_y;
    if (denom == 0)
        return res; // Collinear
    boolean denomPositive = denom > 0;

    s02_x = p0_x - p2_x;
    s02_y = p0_y - p2_y;
    s_numer = s10_x * s02_y - s10_y * s02_x;
    if ((s_numer < 0) == denomPositive)
        return res; // No collision

    t_numer = s32_x * s02_y - s32_y * s02_x;
    if ((t_numer < 0) == denomPositive)
        return res; // No collision

    if (((s_numer > denom) == denomPositive) || ((t_numer > denom) == denomPositive))
        return res; // No collision
    // Collision detected
    t = t_numer / denom;
    return t;
}

private static final double[] AMPLITUDE_DECREASE_FACTOR = { 1, 0.8, 0.6, 0.4, 0.2 };
	private static final double ANGLE_MAX_PITCH = Math.PI/32;
	private static final int THRUST_MAX_PITCH = 40; 

private static final int MAX_ACTION_DEPTH =5;
    
    
   
    static double dist2(Pod p1,int fx,int fy)  { double x=(p1.x-fx),y=p1.y-fy; return x*x+y*y;}
    static double dist2(Pod p1,Pod p2)  { double x=(p1.x-p2.x),y=p1.y-p2.y; return x*x+y*y;}
}
