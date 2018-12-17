package handling;

public class ServerConstants {
	public static final byte CLASS_EXP(final int job) {
		if (job >= 430 && job <= 434) { // Dual Blade
			return 25;
		} else if (job == 900) {
			return 80;
		} else {
			return 0;
		}
	}
	public static final short MAPLE_VERSION = 134;
        public static final int MapLoginNumber = 4; //ServerList ScreenShot // 伺服器選單畫面 // UI\MapLogin And MapLogin 1-4
        public static final boolean DebugMode = true; // false to turn off Debug Mode
        
	public static final String CashShop_Key = "a;!%dfb_=*-a123d9{P~";
	public static final String Login_Key = "pWv]xq:SPTCtk^LGnU9F";
	public static final String[] Channel_Key = {
		"a56=-_dcSAgb", "y5(9=8@nV$;G", "yS5j943GzdUm", "G]R8Frg;kx6Y", "Z)?7fh*([N6S",
		"p4H8=*sknaEK", "A!Z7:mS.2?Kq", "M5:!rfv[?mdF", "Ee@3-7u5s6xy", "p]6L3eS(R;8A",
		"gZ,^k9.npy#F", "cG3M,*7%@zgt", "t+#@TV^3)hL9", "mw4:?sAU7[!6", "b6L]HF(2S,aE",
		"H@rAq]#^Y3+J", "o2A%wKCuqc7Txk5?#rNZ", "d4.Np*B89C6+]y2M^z-7", "oTL2jy9^zkH.84u(%b[d", "WCSJZj3tGX,[4hu;9s?g"
	};
}