
package client;

import java.io.Serializable;

public class SkillEntry implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public final byte skillevel;
    public final byte masterlevel;

    public SkillEntry(final byte skillevel, final byte masterlevel) {
	this.skillevel = skillevel;
	this.masterlevel = masterlevel;
    }
}
