import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class GICPlayer {
    final static Random Rnd=new Random(System.currentTimeMillis());

    // input/output magic for local invocation
    static InputStream in=System.in;
    static PrintStream out=System.out;
    static State s;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        s= new State(in);
        for(Factory f:s.factories) System.err.println(f);
        // game loop
        while (true) {
            int entityCount = in.nextInt(); // the number of entities (e.g. factories and troops)
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                int arg5 = in.nextInt();
                if(entityType.equals("FACTORY")) {
                    Factory f=s.factories[entityId];
                    f.owner=arg1;
                    f.cybCount=arg2;
                    f.prod=arg3;
                }
            }
            int max=0;
            Factory biggest =null;
            for(Factory f:s.factories) {
                if(f.owner !=1 ) continue;
                if(f.cybCount > max) { max=f.cybCount; biggest = f;}
            }
            Factory to=null;
            if(biggest != null && (to=biggest.closestOther()) != null)
                System.out.println("MOVE "+biggest.id+" "+to.id+" "+biggest.cybCount);
            else
                System.out.println("WAIT");
        }
    }
    public static class State implements Serializable {
        private static final long serialVersionUID = 1L;
        Factory[] factories;
        void simulate() {}
        State() {}
        State(Scanner in) {
            int factoryCount = in.nextInt(); // the number of factories
            factories = new Factory[factoryCount];
            for(int i=0;i<factoryCount;++i) factories[i] = new Factory(i);
            int linkCount = in.nextInt(); // the number of links between factories
            for (int i = 0; i < linkCount; i++) {
                int factory1 = in.nextInt();
                int factory2 = in.nextInt();
                int distance = in.nextInt();
                Link l = new Link(distance,factory1,factory2);
                factories[factory1].links.add(l);
                factories[factory2].links.add(l);
            }
        }
    }

    static class Factory {
        Factory(int i) {id=i;}
        int id;
        int cybCount=0;
        int owner=0;
        int prod=0;
        ArrayList<Link> links = new ArrayList<Link>();
        public String toString() {
            String res="";
            res=res+id+"\n";
            for(Link l:links) res= res+(l.f1.id==id ? l.f2.id:l.f1.id)+",";
            return res;
        }
        Factory closestOther() {
            int min=Integer.MAX_VALUE;
            Factory best=null;
            for(Link l:links) {
                Factory o = l.other(id);
                if(o.owner != 1 && l.dist < min) {
                    min=l.dist;
                    best=o;
                }
            }
            return best;
        }

    }
    static class Link {
        Link(int dist,int f1,int f2) {
            this.dist=dist;
            this.f1=s.factories[f1];this.f2=s.factories[f2];
        }
        Factory other(int myId) { return f1.id==myId? f2:f1;}
        int dist;
        Factory f1;
        Factory f2;
    }
}
