package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.rmi.NotBoundException;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

/**
@ Origin from CelinoSEA public source
@ Continued and updated by http://www.johnlth93.tk
**/

public class MonsterDropTracker {
	public static void main(String args[]) throws FileNotFoundException, IOException, NotBoundException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
		int itemToLog = Integer.parseInt(args[0]);
		MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
		MapleData bookData = stringDataWZ.getData("MonsterBook.img");
		MapleData mobData = stringDataWZ.getData("Mob.img");
		StringBuilder sb = new StringBuilder();
		for (MapleData data : bookData.getChildren()) {
			int monsterId = Integer.parseInt(data.getName());
			for (MapleData drop : data.getChildByPath("reward")) {
				int itemid = MapleDataTool.getInt(drop);
				if (itemid == itemToLog) {
					sb.append("Monster ID : ");
					sb.append(monsterId);
					sb.append(", Monster name : ");
					sb.append(MapleDataTool.getString(monsterId + "/name", mobData, "MISSINGNO"));
					sb.append("\n");
					break;
				}
			}
		}
		FileOutputStream out = new FileOutputStream("MonsterDropTracker.log", true);
		out.write(sb.toString().getBytes());
		out.close();
	}
}