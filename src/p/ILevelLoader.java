package p;

import defines.skill_t;
import doom.MapId;
import doom.SourceCode.P_Setup;
import static doom.SourceCode.P_Setup.P_SetupLevel;
import java.io.IOException;
import rr.subsector_t;

public interface ILevelLoader {

    // Lump order in a map WAD: each map needs a couple of lumps
    // to provide a complete scene geometry description.
    public static final int ML_LABEL = 0;

    /** A separator, name, ExMx or MAPxx */
    public static final int ML_THINGS = 1;

    /** Monsters, items.. */
    public static final int ML_LINEDEFS = 2;

    /** LineDefs, from editing */
    public static final int ML_SIDEDEFS = 3;

    /** SideDefs, from editing */
    public static final int ML_VERTEXES = 4;

    /** Vertices, edited and BSP splits generated */
    public static final int ML_SEGS = 5;

    /** LineSegs, from LineDefs split by BSP */
    public static final int ML_SSECTORS = 6;

    /** SubSectors, list of LineSegs */
    public static final int ML_NODES = 7;

    /** BSP nodes */
    public static final int ML_SECTORS = 8;

    /** Sectors, from editing */
    public static final int ML_REJECT = 9;

    /** LUT, sector-sector visibility */
    public static final int ML_BLOCKMAP = 10;

    // Maintain single and multi player starting spots.
    public static final int MAX_DEATHMATCH_STARTS = 10;

    /** Expected lump names for verification */
    public static final String[] LABELS = {"MAPNAME", "THINGS", "LINEDEFS", "SIDEDEFS",
        "VERTEXES", "SEGS", "SSECTORS", "NODES",
        "SECTORS", "REJECT", "BLOCKMAP"};

    /** P_SetupLevel
     *
     * @param map
     * @param playermask
     * @param skill
     * @throws IOException
     */
    @P_Setup.C(P_SetupLevel)
    void SetupLevel(MapId map, int playermask, skill_t skill) throws IOException;

    /**
     * P_SetThingPosition Links a thing into both a block and a subsector based
     * on it's x y. Sets thing.subsector properly
     *
     *
     * @param thing
     */
    void SetThingPosition(mobj_t thing);

    /**
     * R_PointInSubsector
     *
     * MAES: it makes more sense to have this here.
     *
     * @param x fixed
     * @param y fixed
     *
     */
    subsector_t PointInSubsector(int x, int y);

}