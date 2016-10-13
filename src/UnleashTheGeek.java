import java.util.Scanner;



class UnleashTheGeek implements GameState {
	State s=null;
	@Override
	public int getResult() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void init() {
		s=new State();
		s.p1.p1.x=500;
		s.p1.p1.y=2500;
		s.p1.p2.x=500;
		s.p1.p2.y=5500;
		s.p1.flagx=1000;
		s.p1.flagy=4000;
		s.p1.myBase=1000;
		
		s.p2.p1.x=9500;
		s.p2.p1.y=2500;
		s.p2.p2.x=9500;
		s.p2.p2.y=5500;
		s.p2.flagx=9000;
		s.p2.flagy=4000;
		s.p2.myBase=9000;

	}

	@Override
	public String getStateStr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitStr(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setPlayerAction(int id, String s) {
		// TODO Auto-generated method stub
		return false;
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
	            p1.flagx=s.nextInt();
	            p1.flagy=s.nextInt();
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
	            hasFlag=s.nextInt()==1;
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
	    static double dist2(Pod p1,int fx,int fy)  { double x=(p1.x-fx),y=p1.y-fy; return x*x+y*y;}
	    static double dist2(Pod p1,Pod p2)  { double x=(p1.x-p2.x),y=p1.y-p2.y; return x*x+y*y;}


}
