package handling.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import tools.Pair;

public class PlayerBuffStorage implements Serializable {

    private final List<Pair<Integer, List<PlayerBuffValueHolder>>> buffs = new ArrayList<Pair<Integer, List<PlayerBuffValueHolder>>>();
    private final List<Pair<Integer, List<PlayerCoolDownValueHolder>>> coolDowns = new ArrayList<Pair<Integer, List<PlayerCoolDownValueHolder>>>();
    private final List<Pair<Integer, List<PlayerDiseaseValueHolder>>> diseases = new ArrayList<Pair<Integer, List<PlayerDiseaseValueHolder>>>();

    public final void addBuffsToStorage(final int chrid, final List<PlayerBuffValueHolder> toStore) {
	for (final Pair<Integer, List<PlayerBuffValueHolder>> stored : buffs) {
	    if (stored.getLeft() == Integer.valueOf(chrid)) {
		buffs.remove(stored);
	    }
	}
	buffs.add(new Pair<Integer, List<PlayerBuffValueHolder>>(Integer.valueOf(chrid), toStore));
    }

    public final void addCooldownsToStorage(final int chrid, final List<PlayerCoolDownValueHolder> toStore) {
	for (final Pair<Integer, List<PlayerCoolDownValueHolder>> stored : coolDowns) {
	    if (stored.getLeft() == Integer.valueOf(chrid)) {
		coolDowns.remove(stored);
	    }
	}
	coolDowns.add(new Pair<Integer, List<PlayerCoolDownValueHolder>>(Integer.valueOf(chrid), toStore));
    }

    public final void addDiseaseToStorage(final int chrid, final List<PlayerDiseaseValueHolder> toStore) {
	for (final Pair<Integer, List<PlayerDiseaseValueHolder>> stored : diseases) {
	    if (stored.getLeft() == Integer.valueOf(chrid)) {
		diseases.remove(stored);
	    }
	}
	diseases.add(new Pair<Integer, List<PlayerDiseaseValueHolder>>(Integer.valueOf(chrid), toStore));
    }

    public final List<PlayerBuffValueHolder> getBuffsFromStorage(final int chrid) {
	List<PlayerBuffValueHolder> ret = null;

	for (int i = 0; i < buffs.size(); i++) {
	    final Pair<Integer, List<PlayerBuffValueHolder>> stored = buffs.get(i);
	    if (stored.getLeft().equals(chrid)) {
		ret = stored.getRight();
		buffs.remove(stored);
	    }
	}
	return ret;
    }

    public final List<PlayerCoolDownValueHolder> getCooldownsFromStorage(final int chrid) {
	List<PlayerCoolDownValueHolder> ret = null;

	for (int i = 0; i < coolDowns.size(); i++) {
	    final Pair<Integer, List<PlayerCoolDownValueHolder>> stored = coolDowns.get(i);
	    if (stored.getLeft().equals(chrid)) {
		ret = stored.getRight();
		coolDowns.remove(stored);
	    }
	}
	return ret;
    }

    public final List<PlayerDiseaseValueHolder> getDiseaseFromStorage(final int chrid) {
	List<PlayerDiseaseValueHolder> ret = null;

	for (int i = 0; i < diseases.size(); i++) {
	    final Pair<Integer, List<PlayerDiseaseValueHolder>> stored = diseases.get(i);
	    if (stored.getLeft().equals(chrid)) {
		ret = stored.getRight();
		diseases.remove(stored);
	    }
	}
	return ret;
    }
}
