/*
 * Copyright (C) 1993-1996 by id Software, Inc.
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package p.Actions.ActiveStates.MonsterStates;

import static data.Limits.MAXPLAYERS;
import data.mobjtype_t;
import doom.DoomMain;
import doom.thinker_t;
import mapinfo.MapEntry;
import p.Actions.ActionTrait;
import p.ActiveStates;
import p.floor_e;
import p.mobj_t;
import p.vldoor_e;
import rr.line_t;

public interface Bosses extends ActionTrait {

    void A_Fall(mobj_t mo);

    /**
     * A_BossDeath
     * Possibly trigger special effects
     * if on first boss level
     *
     * TODO: find out how Plutonia/TNT does cope with this.
     * Special clauses?
     *
     */
    default void A_BossDeath(mobj_t mo) {
        final DoomMain<?, ?> D = DOOM();
        thinker_t th;
        mobj_t mo2;
        line_t junk = new line_t();
        int i;

        var entry = D.getMapEntry(D.gamemap);

        MapEntry.BossActionEntry action = null;

        if (entry != null && entry.bossaction != null) {
            for (var check : entry.bossaction.entries()) {
                if (mo.type == check.actor()) {
                    action = check;
                    break;
                }
            }
        }

        if (action == null && (entry == null || entry.bossaction == null || !entry.bossaction.reset())) {
            if (D.isCommercial()) {
                if (D.gamemap.map() == 7) {
                    if (mo.type == mobjtype_t.MT_FATSO) {
                        action = new MapEntry.BossActionEntry(mo.type, 23, 666);
                    } else if (mo.type == mobjtype_t.MT_BABY) {
                        action = new MapEntry.BossActionEntry(mo.type, 30, 666);
                    }
                }
            } else {
                switch (D.gamemap.episode()) {
                    case 1:
                        if (D.gamemap.map() == 8 && mo.type == mobjtype_t.MT_BRUISER) {
                            action = new MapEntry.BossActionEntry(mo.type, 23, 666);
                        }
                        break;

                    case 2:
                        if (D.gamemap.map() == 8 && mo.type == mobjtype_t.MT_CYBORG) {
                            action = new MapEntry.BossActionEntry(mo.type, 11, 666);
                        }
                        break;

                    case 3:
                        if (D.gamemap.map() == 8 && mo.type == mobjtype_t.MT_SPIDER) {
                            action = new MapEntry.BossActionEntry(mo.type, 11, 666);
                        }

                        break;

                    case 4:
                        switch (D.gamemap.map()) {
                            case 6:
                                if (mo.type == mobjtype_t.MT_CYBORG) {
                                    action = new MapEntry.BossActionEntry(mo.type, 109, 666);
                                }
                                break;

                            case 8:
                                if (mo.type != mobjtype_t.MT_SPIDER) {
                                    action = new MapEntry.BossActionEntry(mo.type, 23, 666);
                                }
                                break;
                        }
                        break;

                    default:
                        if (D.gamemap.map() != 8) {
                            return;
                        }
                        break;
                }
            }
        }

        // make sure there is a player alive for victory
        for (i = 0; i < MAXPLAYERS; i++) {
            if (D.playeringame[i] && D.players[i].health[0] > 0) {
                break;
            }
        }

        if (i == MAXPLAYERS) {
            return; // no one left alive, so do not end game
        }
        // scan the remaining thinkers to see
        // if all bosses are dead
        for (th = getThinkerCap().next; th != getThinkerCap(); th = th.next) {
            if (th.thinkerFunction != ActiveStates.P_MobjThinker) {
                continue;
            }

            mo2 = (mobj_t) th;
            if (mo2 != mo
                    && mo2.type == mo.type
                    && mo2.health > 0) {
                // other boss not dead
                return;
            }
        }

        if (action != null) {
            junk.special = (short) action.linespecial();
            junk.tag = (short) action.tag();
            if (!getThinkers().UseSpecialLine(mo, junk, false, true)) {
                getThinkers().CrossSpecialLine(junk,0, mo, true);
            }
        }
    }

    default void A_KeenDie(mobj_t mo) {
        thinker_t th;
        mobj_t mo2;
        line_t junk = new line_t(); // MAES: fixed null 21/5/2011

        A_Fall(mo);

        // scan the remaining thinkers
        // to see if all Keens are dead
        for (th = getThinkerCap().next; th != getThinkerCap(); th = th.next) {
            if (th.thinkerFunction != ActiveStates.P_MobjThinker) {
                continue;
            }

            mo2 = (mobj_t) th;
            if (mo2 != mo
                    && mo2.type == mo.type
                    && mo2.health > 0) {
                // other Keen not dead
                return;
            }
        }

        junk.tag = 666;
        getThinkers().DoDoor(junk, vldoor_e.open);
    }

}