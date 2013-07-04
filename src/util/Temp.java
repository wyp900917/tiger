package util;
public class Temp {

	public static int num = 0;

	public Temp() {
		num++;
	}

	@Override
	public String toString() {
		return "temp_" + num;
	}

}
