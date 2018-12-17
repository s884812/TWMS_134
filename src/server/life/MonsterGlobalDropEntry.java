package server.life;

public class MonsterGlobalDropEntry {

    public MonsterGlobalDropEntry(int itemId, int chance, int continent, byte dropType, int Minimum, int Maximum, short questid) {
	this.itemId = itemId;
	this.chance = chance;
	this.dropType = dropType;
	this.questid = questid;
	this.Minimum = Minimum;
	this.Maximum = Maximum;
    }
    public byte dropType;
    public short questid;
    public int itemId, chance, Minimum, Maximum;
}