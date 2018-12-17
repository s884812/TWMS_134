
package client;

import server.MapleStatEffect;
import server.life.Element;

public interface ISkill {

    int getId();
    MapleStatEffect getEffect(int level);
    MapleStatEffect getEffect(MapleCharacter chr, int level);
    byte getMaxLevel();
    int getAnimationTime();
    public boolean canBeLearnedBy(int job);
    public boolean isFourthJob();
    public boolean getAction();
    public Element getElement();
    public boolean isBeginnerSkill();
    public boolean hasRequiredSkill();
    public boolean isInvisible();
    public boolean isChargeSkill();
    public int getRequiredSkillLevel();
    public int getRequiredSkillId();
    public int getMasterLevel();
}
