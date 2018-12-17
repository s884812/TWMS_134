package handling.world;

import client.MapleDisease;
import java.io.Serializable;

public class PlayerDiseaseValueHolder implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public int diseaseid;
    public long startTime;
    public long length;
    public MapleDisease disease;

    public PlayerDiseaseValueHolder(final MapleDisease disease, final long startTime, final long length) {
	this.disease = disease;
	this.startTime = startTime;
	this.length = length;
    }
}