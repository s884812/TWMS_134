package tools;

import java.util.List;

public class AttackPair {
	public int objectid;
	public List<Integer> attack;

	public AttackPair(int objectid, List<Integer> attack) {
		this.objectid = objectid;
		this.attack = attack;
	}
}