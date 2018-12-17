package server.life;

public class MonsterDropEntry {

	public MonsterDropEntry(int itemId, int chance, int Minimum, int Maximum, short questid) {
		this.itemId = itemId;
		this.chance = chance;
		this.questid = questid;
		this.Minimum = Minimum;
		this.Maximum = Maximum;
	}

	public short questid;
	public int itemId, chance, Minimum, Maximum;
}