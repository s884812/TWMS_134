package server.maps;

import java.awt.Point;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import server.Randomizer;
import server.TimerManager;
import server.life.MapleLifeFactory;
import tools.MaplePacketCreator;

public class Event_DojoAgent {

	private final static int baseAgentMapId = 970030000; // 9500337 = mano
	private final static Point point1 = new Point(140, 0),
		point2 = new Point(-193, 0),
		point3 = new Point(355, 0);

	public static boolean warpStartAgent(final MapleCharacter c, final boolean party) {
		final int stage = 1;
		final int mapid = baseAgentMapId + (stage * 100);
		final ChannelServer ch = c.getClient().getChannelServer();
		for (int i = mapid; i < mapid + 15; i++) {
			final MapleMap map = ch.getMapFactory(c.getWorld()).getMap(i);
			if (map.getCharactersSize() == 0) {
				clearMap(map, false);
				c.changeMap(map, map.getPortal(0));
				map.respawn(true);
				return true;
			}
		}
		return false;
	}

	public static boolean warpNextMap_Agent(final MapleCharacter c, final boolean fromResting) {
		final int currentmap = c.getMapId();
		final int thisStage = (currentmap - baseAgentMapId) / 100;
		MapleMap map = c.getMap();
		if (map.getSpawnedMonstersOnMap() > 0) {
			return false;
		}
		if (!fromResting) {
			clearMap(map, true);
			int cashGain;
			cashGain = (int) (Math.random() * 50);
			if (cashGain < 10) {
				cashGain = 10;
				c.modifyCSPoints(1, cashGain, true);
			} else {
				c.modifyCSPoints(1, cashGain, true);
			}
		}
		final ChannelServer ch = c.getClient().getChannelServer();
		if (currentmap >= 970032700 && currentmap <= 970032800) {
			map = ch.getMapFactory(c.getWorld()).getMap(baseAgentMapId);
			c.changeMap(map, map.getPortal(0));
			return true;
		}
		final int nextmapid = baseAgentMapId + ((thisStage + 1) * 100);
		for (int i = nextmapid; i < nextmapid + 7; i++) {
			map = ch.getMapFactory(c.getWorld()).getMap(i);
			if (map.getCharactersSize() == 0) {
			clearMap(map, false);
			c.changeMap(map, map.getPortal(0));
			map.respawn(true);
			return true;
			}
		}
		return false;
	}

	public static boolean warpStartDojo(final MapleCharacter c, final boolean party) {
		int stage = 1;
		if (party || stage == -1 || stage > 38) {
			stage = 1;
		}
		final int mapid = 925020000 + (stage * 100);
		final ChannelServer ch = c.getClient().getChannelServer();
		for (int i = mapid; i < mapid + 15; i++) {
			final MapleMap map = ch.getMapFactory(c.getWorld()).getMap(i);
			if (map.getCharactersSize() == 0) {
				clearMap(map, false);
				c.changeMap(map, map.getPortal(0));
				spawnMonster(map, stage);
				return true;
			}
		}
		return false;
	}

	// Resting rooms :
	// 925020600 ~ 925020609
	// 925021200 ~ 925021209
	// 925021800 ~ 925021809
	// 925022400 ~ 925022409
	// 925023000 ~ 925023009
	// 925023600 ~ 925023609
	public static boolean warpNextMap(final MapleCharacter c, final boolean fromResting) {
		final int currentmap = c.getMapId();
		final ChannelServer ch = c.getClient().getChannelServer();
		if (!fromResting) {
			clearMap(ch.getMapFactory(c.getWorld()).getMap(currentmap), true);
			int cashGain;
			cashGain = (int) (Math.random() * 50);
			if (cashGain < 10) {
				cashGain = 10;
				c.modifyCSPoints(1, cashGain, true);
			} else {
				c.modifyCSPoints(1, cashGain, true);
			}
			c.setDojo(c.getDojo() + 10);
			c.getClient().getSession().write(MaplePacketCreator.Mulung_Pts(10, c.getDojo()));
		}
		if (currentmap >= 925023800 && currentmap <= 925023814) {
			final MapleMap map = ch.getMapFactory(c.getWorld()).getMap(925020003);
			c.modifyCSPoints(1, 5000, true);
			c.changeMap(map, map.getPortal(1));
			return true;
		}
		final int temp = (currentmap - 925000000) / 100;
		final int thisStage = (int) (temp - (Math.floor(temp / 100) * 100));
		final int nextmapid = 925020000 + ((thisStage + 1) * 100);

		for (int i = nextmapid; i < nextmapid + 15; i++) {
			final MapleMap map = ch.getMapFactory(c.getWorld()).getMap(i);
			if (map.getCharactersSize() == 0) {
			clearMap(map, false);
			c.changeMap(map, map.getPortal(0));
			spawnMonster(map, thisStage + 1);
			return true;
			}
		}
		return false;
	}

	private static void clearMap(final MapleMap map, final boolean check) {
		if (check) {
			if (map.getCharactersSize() != 0) {
			return;
			}
		}
		map.killAllMonsters(false);
		map.resetReactors();
	}

	private static void spawnMonster(final MapleMap map, final int stage) {
		final int mobid;
		switch (stage) {
			case 1:
			mobid = 9300184; // Mano
			break;
			case 2:
			mobid = 9300185; // Stumpy
			break;
			case 3:
			mobid = 9300186; // Dewu
			break;
			case 4:
			mobid = 9300187; // King Slime
			break;
			case 5:
			mobid = 9300188; // Giant Centipede
			break;
			case 7:
			mobid = 9300189; // Faust
			break;
			case 8:
			mobid = 9300190; // King Clang
			break;
			case 9:
			mobid = 9300191; // Mushmom
			break;
			case 10:
			mobid = 9300192; // Alishar
			break;
			case 11:
			mobid = 9300193; // Timer
			break;
			case 13:
			mobid = 9300194; // Dale
			break;
			case 14:
			mobid = 9300195; // Papa Pixie
			break;
			case 15:
			mobid = 9300196; // Zombie Mushmom
			break;
			case 16:
			mobid = 9300197; // Jeno
			break;
			case 17:
			mobid = 9300198; // Lord Pirate
			break;
			case 19:
			mobid = 9300199; // Old Fox
			break;
			case 20:
			mobid = 9300200; // Tae Roon
			break;
			case 21:
			mobid = 9300201; // Poison Golem
			break;
			case 22:
			mobid = 9300202; // Ghost Priest
			break;
			case 23:
			mobid = 9300203; // Jr. Balrog
			break;
			case 25:
			mobid = 9300204; // Eliza
			break;
			case 26:
			mobid = 9300205; // Frankenroid
			break;
			case 27:
			mobid = 9300206; // Chimera
			break;
			case 28:
			mobid = 9300207; // Snack Bar
			break;
			case 29:
			mobid = 9300208; // Snowman
			break;
			case 31:
			mobid = 9300209; // Blue Mushmom
			break;
			case 32:
			mobid = 9300210; // Crimson Balrog
			break;
			case 33:
			mobid = 9300211; // Manon
			break;
			case 34:
			mobid = 9300212; // Griffey
			break;
			case 35:
			mobid = 9300213; // Leviathan
			break;
			case 37:
			mobid = 9300214; // Papulatus
			break;
			case 38:
			mobid = 9300215; // Mu gong
			break;
			default:
			return;
		}
		if (mobid != 0) {
			final int rand = Randomizer.nextInt(3);
			TimerManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					map.spawnMonsterWithEffect(MapleLifeFactory.getMonster(mobid), 15, rand == 0 ? point1 : rand == 1 ? point2 : point3);
				}
			}, 3000);
		}
	}
}