package tools;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileOutputStream;

public class FileoutputUtil {

	public static final String Acc_Stuck = "AccountStuck.log",
		Login_Error = "Login_Error.log",
		Timer_Log = "Timer_Except.log",
		MapTimer_Log = "MapTimer_Except.log",
		GMCommand_Log = "GMCommand.log",
		IP_Log = "AccountIP.log",
		Horntail_Log = "Horntail_Fight.log",
		Pinkbean_Log = "Pinkbean_Fight.log",
		Error38_Log = "Error38.log";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void log(final String file, final String msg) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file, true);
			out.write(msg.getBytes());
			out.write("\n------------------------\n".getBytes());
		} catch (IOException ess) {
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ignore) {
			}
		}
	}

	public static void outputFileError(final String file, final Throwable t) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file, true);
			out.write(getString(t).getBytes());
			out.write("\n------------------------\n".getBytes());
		} catch (IOException ess) {
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ignore) {
			}
		}
	}

	public static final String CurrentReadable_Time() {
		return sdf.format(Calendar.getInstance().getTime());
	}

	public static final String getString(final Throwable e) {
		String retValue = null;
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			retValue = sw.toString();
		} finally {
			try {
				if (pw != null) {
					pw.close();
				}
				if (sw != null) {
					sw.close();
				}
			} catch (IOException ignore) {
			}
		}
		return retValue;
	}
}