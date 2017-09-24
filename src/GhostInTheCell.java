import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

public class GhostInTheCell implements GameState,Serializable {
    GICPlayer.State s = new GICPlayer.State();
    @Override
    public int getResult() {
        return UNDECIDED;
    }

    @Override
    public void init() {
    }

    @Override
    public String getStateStr(int id) {
        return null;
    }

    @Override
    public String getInitStr(int id) {
       StringBuffer sb= new StringBuffer();
       sb.append(s.factories.length);
       sb.append("\n");
       ArrayList<GICPlayer.Link> al=new ArrayList<>();
       for(GICPlayer.Factory f:s.factories) {
           for(GICPlayer.Link l:f.links) {
               if(l.other(f.id).id > f.id) al.add(l);
           }
       }
       sb.append(al.size());
       sb.append("\n");
       for(GICPlayer.Link l:al) {
           sb.append(l.f1.id);
           sb.append(" ");
           sb.append(l.f2.id);
           sb.append(" ");
           sb.append(l.dist);
           sb.append("\n");
       }
        return sb.toString();

    }

    @Override
    public void readActions(Scanner s, int id) {

    }

    @Override
    public int resolveActions() {
        s.simulate();
        return getResult();
    }

    @Override
    public void startTurn() {

    }

    @Override
    public GameState save() {
        return null;
    }
}
