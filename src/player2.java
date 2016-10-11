import java.util.*;

/**
 * Auto-generated code below aims at helping you parse  
 * the standard input according to the problem statement.
 **/
class player2 {
static BooleanBoard Bombs= null,Walls=null,Obstacles=null,rItems=null,bItems=null,Boxes=null;
static int width,height,myx=-1,myy=0,myrange=0;
static boolean hasBombs=true;
    public static void main(String args[]) {
        int targetx=0,targety=0;
        @SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
        width = in.nextInt();
        height = in.nextInt();
        int myId = in.nextInt();
        in.nextLine();
        Bombs=new BooleanBoard();
        Walls=new BooleanBoard();
        Obstacles = new BooleanBoard();
        rItems=new BooleanBoard();
        bItems=new BooleanBoard();
        Boxes=new BooleanBoard();
        // game loop
        while(true){
            Bombs.clear();Walls.clear();Obstacles.clear();rItems.clear();bItems.clear();Boxes.clear();
            for (int y = 0; y < height; y++) {
                
                String row = in.nextLine();
                //System.err.println(row);
                int x=0;
                for(char c:row.toCharArray()) {
                    if(c!='.') {
                        if(c=='X') {Walls.set(x,y);Obstacles.set(x,y);}
                        else {Boxes.set(x,y);Obstacles.set(x,y);}
                    }
                    x++;
                }
            }
                   
            int entities = in.nextInt();
            for (int i = 0; i < entities; i++) {
                int entityType = in.nextInt();
                int owner = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int param1 = in.nextInt();
                int param2 = in.nextInt();
                if(entityType==0 && owner==myId) {
                    if(myx==-1) { targetx=x;targety=y;}
                    myx=x;myy=y;hasBombs=param1>0?true:false;myrange=param2;
                }
                else if(entityType==2) {
                    if(param1 == 1) rItems.set(x,y);
                    else bItems.set(x,y);
                    Obstacles.set(x,y);
                }
                else if(entityType == 1) {
                    Bombs.set(x,y);
                    Obstacles.set(x,y);
                }
            }
            System.err.println("x="+myx+" y="+myy+" bombs="+hasBombs+" TargetX="+targetx+" TargetY="+targety);
            in.nextLine();
             //dumpScreen();
            
            String action="";
            if(myx!= targetx || myy!=targety|| !hasBombs)  action="MOVE ";
            else {
                action="BOMB ";
                Score s=new Score();
                ffill(myx,myy,new BooleanBoard(),s,myrange);
                targetx=s.bestx;
                targety=s.besty;
                System.err.println("Nb cases eval= "+s.count);
            }
            System.out.println(action+targetx+" "+targety);
        }//while(true)
    }
    public static void dumpScreen() {
       for(int li=0;li<height;li++) {
            for(int col=0;col<width;col++) 
                System.err.print(   Boxes.is(col,li)?"X":
                                    Walls.is(col,li)?"W":
                                    rItems.is(col,li)?"r":
                                    bItems.is(col,li)?"b":
                                    Bombs.is(col,li)?"B":".");
            System.err.println();           
       }
    }
    static void ffill (int x, int y, BooleanBoard b,Score s,int range) {
        if(x!= myx || y!= myy) {
            if(b.is(x,y) || Walls.is(x,y)|| Boxes.is(x,y)||Bombs.is(x,y)) return;
        }
        b.set(x,y);
        s.eval(x,y,range);
        if(x>0) ffill(x-1,y,b,s,range);
        if(y>0) ffill(x,y-1,b,s,range);
        if(x<width-1) ffill(x+1,y,b,s,range);
        if(y<height-1) ffill(x,y+1,b,s,range);
    }
    static class BooleanBoard {
        boolean[][] b= new boolean[width][];
        boolean is(int x,int y) { return b[x][y];}
        BooleanBoard() { for(int i=0;i<width;++i) b[i]=new boolean[height];}
        void set(int x,int y) { b[x][y]=true;}
        void clear() { for(int i=0;i<width;++i) for(int j=0;j<height;++j) b[i][j]=false;}
    }
    static class Score {
        int best=0;
        int bestx=-1,besty=-1;
        int count=0;
        void eval(int x,int y,int range) {
            count++;
            int score=0;
            if(rItems.is(x,y)) score+=2;
            if(bItems.is(x,y))score+=10;
            for(int i=x-1;i>x-range;i--) {
                    if(i<0 ||Walls.is(i,y)) break;
                    if(Boxes.is(i,y)) { 
                        score++;
                        //if(log) System.err.print(i+","+y+" - ");
                        break;
                    }
            }
            for(int i=x+1;i<x+range;i++) {
                    if(i>width-1 ||Walls.is(i,y)) break;
                    if(Boxes.is(i,y)) { 
                        score++;
                        //if(log) System.err.print(i+","+y+" - ");
                        break;
                    }
            }
            for(int i=y-1;i>y-range;i--) {
                    if(i<0 ||Walls.is(x,i)) break;
                    if(Boxes.is(x,i)) { 
                        score++;
                        //if(log) System.err.print(x+","+i+" - ");
                        break;
                    }
            }
            for(int i=y+1;i<y+range;i++) {
                    if(i>height-1 ||Walls.is(x,i)) break;
                    if(Boxes.is(x,i)) { 
                        score++;
                        //if(log) System.err.print(x+","+i+" - ");
                        break;
                    }
            }
            //if(log) System.err.println();
            //return score==0? -10000:score-Math.abs(li-myli)-Math.abs(col-mycol);
            if(score>best) {
                best=score;
                bestx=x;besty=y;
                System.err.println(best+":"+bestx+","+besty);
            }
    }
    }
}
