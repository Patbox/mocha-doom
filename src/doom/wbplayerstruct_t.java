package doom;

//
import java.util.logging.Level;
import mochadoom.Logger;

// INTERMISSION
// Structure passed e.g. to WI_Start(wb)
//
public class wbplayerstruct_t implements Cloneable {

    private static final Logger LOGGER = Logger.getLogger(wbplayerstruct_t.class.getName());

    public wbplayerstruct_t() {
        frags = new int[4];
    }
    public boolean in; // whether the player is in game

    /** Player stats, kills, collected items etc. */
    public int skills;
    public int sitems;
    public int ssecret;
    public int stime;
    public int[] frags;
    /** current score on entry, modified on return */
    public int score;

    public wbplayerstruct_t clone() {
        wbplayerstruct_t r = null;
        try {
            r = (wbplayerstruct_t) super.clone();
        } catch (CloneNotSupportedException e) {
            LOGGER.log(Level.SEVERE, "wbplayerstruct_t: clone failure", e);
        }
        /*r.in=this.in;
         r.skills=this.skills;
         r.sitems=this.sitems;
         r.ssecret=this.ssecret;
         r.stime=this.stime; */
        System.arraycopy(this.frags, 0, r.frags, 0, r.frags.length);
        // r.score=this.score;

        return r;

    }

}