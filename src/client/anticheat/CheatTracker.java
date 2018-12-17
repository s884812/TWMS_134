package client.anticheat;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import client.GameConstants;
import client.MapleCharacter;
import server.TimerManager;
import tools.StringUtil;

public class CheatTracker {

    private Map<CheatingOffense, CheatingOffenseEntry> offenses = Collections.synchronizedMap(new LinkedHashMap<CheatingOffense, CheatingOffenseEntry>());
    private final WeakReference<MapleCharacter> chr;
    // For keeping track of speed attack hack.
    private int lastAttackTickCount = 0;
    private byte Attack_tickResetCount = 0;
    private long Server_ClientAtkTickDiff = 0;
    private long lastDamage = 0;
    private long takingDamageSince;
    private int numSequentialDamage = 0;
    private long lastDamageTakenTime = 0;
    private byte numZeroDamageTaken = 0;
    private int numSequentialSummonAttack = 0;
    private long summonSummonTime = 0;
    private int numSameDamage = 0;
    private Point lastMonsterMove;
    private int monsterMoveCount;
    private int attacksWithoutHit = 0;
    private ScheduledFuture<?> invalidationTask;

    public CheatTracker(final MapleCharacter chr) {
        this.chr = new WeakReference<MapleCharacter>(chr);
        invalidationTask = TimerManager.getInstance().register(new InvalidationTask(), 60000);
        takingDamageSince = System.currentTimeMillis();
    }

    public final void checkAttack(final int skillId, final int tickcount) {
        final short AtkDelay = GameConstants.getAttackDelay(skillId);
        if ((tickcount - lastAttackTickCount) < AtkDelay) {
            registerOffense(CheatingOffense.FASTATTACK);
        }
        final long STime_TC = System.currentTimeMillis() - tickcount; // hack = - more
        if (Server_ClientAtkTickDiff - STime_TC > 250) { // 250 is the ping, TODO
            registerOffense(CheatingOffense.FASTATTACK2);
        }
        // if speed hack, client tickcount values will be running at a faster pace
        // For lagging, it isn't an issue since TIME is running simotaniously, client
        // will be sending values of older time

//	System.out.println("Delay [" + skillId + "] = " + (tickcount - lastAttackTickCount) + ", " + (Server_ClientAtkTickDiff - STime_TC));
        Attack_tickResetCount++; // Without this, the difference will always be at 100
        if (Attack_tickResetCount >= (AtkDelay <= 200 ? 2 : 4)) {
            Attack_tickResetCount = 0;
            Server_ClientAtkTickDiff = STime_TC;
        }
        lastAttackTickCount = tickcount;
    }

    public final void checkTakeDamage(final int damage) {
        numSequentialDamage++;
        lastDamageTakenTime = System.currentTimeMillis();

        // System.out.println("tb" + timeBetweenDamage);
        // System.out.println("ns" + numSequentialDamage);
        // System.out.println(timeBetweenDamage / 1500 + "(" + timeBetweenDamage / numSequentialDamage + ")");

        if (lastDamageTakenTime - takingDamageSince / 500 < numSequentialDamage) {
            registerOffense(CheatingOffense.FAST_TAKE_DAMAGE);
        }
        if (lastDamageTakenTime - lastDamageTakenTime > 4500) {
            takingDamageSince = lastDamageTakenTime;
            numSequentialDamage = 0;
        }
        /*	(non-thieves)
        Min Miss Rate: 2%
        Max Miss Rate: 80%
        (thieves)
        Min Miss Rate: 5%
        Max Miss Rate: 95%*/
        if (damage == 0) {
            numZeroDamageTaken++;
            if (numZeroDamageTaken >= 35) { // Num count MSEA a/b players
                numZeroDamageTaken = 0;
                registerOffense(CheatingOffense.HIGH_AVOID);
            }
        } else if (damage != -1) {
            numZeroDamageTaken = 0;
        }
    }

    public final void checkSameDamage(final int dmg) {
        if (dmg > 1 && lastDamage == dmg) {
            numSameDamage++;

            if (numSameDamage > 5) {
                numSameDamage = 0;
                registerOffense(CheatingOffense.SAME_DAMAGE, numSameDamage + " times: " + dmg);
            }
        } else {
            lastDamage = dmg;
            numSameDamage = 0;
        }
    }

    public final void checkMoveMonster(final Point pos) {
        if (pos == lastMonsterMove) {
            monsterMoveCount++;
            if (monsterMoveCount > 15) {
                registerOffense(CheatingOffense.MOVE_MONSTERS);
            }
        } else {
            lastMonsterMove = pos;
            monsterMoveCount = 1;
        }
    }

    public final void resetSummonAttack() {
        summonSummonTime = System.currentTimeMillis();
        numSequentialSummonAttack = 0;
    }

    public final boolean checkSummonAttack() {
        numSequentialSummonAttack++;
        //estimated
        // System.out.println(numMPRegens + "/" + allowedRegens);
        if ((System.currentTimeMillis() - summonSummonTime) / (2000 + 1) < numSequentialSummonAttack) {
            registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
            return false;
        }
        return true;
    }

    public final int getAttacksWithoutHit() {
        return attacksWithoutHit;
    }

    public final void setAttacksWithoutHit(final boolean increase) {
        if (increase) {
            this.attacksWithoutHit++;
        } else {
            this.attacksWithoutHit = 0;
        }
    }

    public final void registerOffense(final CheatingOffense offense) {
        registerOffense(offense, null);
    }

    public final void registerOffense(final CheatingOffense offense, final String param) {
        final MapleCharacter chrhardref = chr.get();
        if (chrhardref == null || !offense.isEnabled()) {
            return;
        }
        CheatingOffenseEntry entry = offenses.get(offense);
        if (entry != null && entry.isExpired()) {
            expireEntry(entry);
            entry = null;
        }
        if (entry == null) {
            entry = new CheatingOffenseEntry(offense, chrhardref.getId());
        }
        if (param != null) {
            entry.setParam(param);
        }
        entry.incrementCount();
        if (offense.shouldAutoban(entry.getCount())) {
            final byte type = offense.getBanType();
            if (type == 1) {
                //AutobanManager.getInstance().autoban(chrhardref.getClient(), StringUtil.makeEnumHumanReadable(offense.name()));
            } else if (type == 2) {
                if (!chrhardref.isGM()) {
                    //chrhardref.getClient().getSession().close();
                } else {
                    chrhardref.dropMessage(5, "[WARNING] D/c triggled : " + offense.toString());
                }
            }
            return;
        }
        offenses.put(offense, entry);
        CheatingOffensePersister.getInstance().persistEntry(entry);
    }

    public final void expireEntry(final CheatingOffenseEntry coe) {
        offenses.remove(coe.getOffense());
    }

    public final int getPoints() {
        int ret = 0;
        CheatingOffenseEntry[] offenses_copy;
        synchronized (offenses) {
            offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
        }
        for (final CheatingOffenseEntry entry : offenses_copy) {
            if (entry.isExpired()) {
                expireEntry(entry);
            } else {
                ret += entry.getPoints();
            }
        }
        return ret;
    }

    public final Map<CheatingOffense, CheatingOffenseEntry> getOffenses() {
        return Collections.unmodifiableMap(offenses);
    }

    public final String getSummary() {
        final StringBuilder ret = new StringBuilder();
        final List<CheatingOffenseEntry> offenseList = new ArrayList<CheatingOffenseEntry>();
        synchronized (offenses) {
            for (final CheatingOffenseEntry entry : offenses.values()) {
                if (!entry.isExpired()) {
                    offenseList.add(entry);
                }
            }
        }
        Collections.sort(offenseList, new Comparator<CheatingOffenseEntry>() {

            @Override
            public final int compare(final CheatingOffenseEntry o1, final CheatingOffenseEntry o2) {
                final int thisVal = o1.getPoints();
                final int anotherVal = o2.getPoints();
                return (thisVal < anotherVal ? 1 : (thisVal == anotherVal ? 0 : -1));
            }
        });
        final int to = Math.min(offenseList.size(), 4);
        for (int x = 0; x < to; x++) {
            ret.append(StringUtil.makeEnumHumanReadable(offenseList.get(x).getOffense().name()));
            ret.append(": ");
            ret.append(offenseList.get(x).getCount());
            if (x != to - 1) {
                ret.append(" ");
            }
        }
        return ret.toString();
    }

    public final void dispose() {
        if (invalidationTask != null) {
            invalidationTask.cancel(false);
        }
        invalidationTask = null;
    }

    private final class InvalidationTask implements Runnable {

        @Override
        public final void run() {
            CheatingOffenseEntry[] offenses_copy;
            synchronized (offenses) {
                offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
            }
            for (CheatingOffenseEntry offense : offenses_copy) {
                if (offense.isExpired()) {
                    expireEntry(offense);
                }
            }
            if (chr.get() == null) {
                dispose();
            }
        }
    }
}