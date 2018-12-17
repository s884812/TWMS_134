package scripting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import client.MapleClient;

/**
 *
 * @author Matze
 */
public abstract class AbstractScriptManager {

    private static final ScriptEngineManager sem = new ScriptEngineManager();

    protected Invocable getInvocable(String path, MapleClient c) {
	FileReader fr = null;
	try {
	    path = "scripts/" + path;
	    ScriptEngine engine = null;

	    if (c != null) {
		engine = c.getScriptEngine(path);
	    }
	    if (engine == null) {
		File scriptFile = new File(path);
		if (!scriptFile.exists()) {
		    return null;
		}
		engine = sem.getEngineByName("javascript");
		if (c != null) {
		    c.setScriptEngine(path, engine);
		}
		fr = new FileReader(scriptFile);
		engine.eval(fr);
	    }
	    return (Invocable) engine;
	} catch (Exception e) {
	    System.err.println("Error executing script." + e);
	    return null;
	} finally {
	    try {
		if (fr != null) {
		    fr.close();
		}
	    } catch (IOException ignore) {
	    }
	}
    }
}
