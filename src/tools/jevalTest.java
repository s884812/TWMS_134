package tools;

import java.sql.Timestamp;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

public class jevalTest {

	public static void main(String[] args) {
		java.util.Date date = new java.util.Date();
		System.out.println(new Timestamp(date.getTime()));
		Evaluator eva = new Evaluator();
		try {
			System.out.println(eva.evaluate("2+8/2"));
			System.out.println(eva.evaluate("round(3)"));
			System.out.println(eva.evaluate("abs(-1)"));
			double test = Math.ceil(5);
			System.out.println(test);
		} catch (EvaluationException e) {
			//e.printStackTrace();
			System.out.println(e);
		}
	}
}