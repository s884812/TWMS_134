package server.maps;

public enum SavedLocationType {

	FREE_MARKET,
	MIRROR_OF_DIMENSION,
	WORLDTOUR,
	FLORINA,
	FISHING,
	RICHIE,
	DONGDONGCHIANG,
	AMORIA;

	public static SavedLocationType fromString(String Str) {
		return valueOf(Str);
	}
}