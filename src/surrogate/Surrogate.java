package surrogate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

public class Surrogate {
	
	static void babble(Model model, Integer len) {
		Vector<String> words = new Vector<String>();
		words.add("to"); words.add("any");
		for (int i = 2; i < len; i++) {
			String p2 = words.get(i-2);
			String p1 = words.get(i-1);
			String next = model.predict(p2, p1);
			words.add(next);
		}
		
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(System.out));
		try {
			for (int i = 0; i < words.size(); i++) {
				wr.write(words.get(i) + " ");
			} 
			wr.write("\n");
			wr.flush();
		}	
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String filename = args[0];
		Model model = new Model(filename);
		babble(model, 25);
	}
	
}
