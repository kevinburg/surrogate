package surrogate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class Model {

	Vector<String> tokens;
	public Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> counts;
	
	public String predict(String p2, String p1) {
		Hashtable<String, Integer> t = this.counts.get(p2).get(p1);
		Enumeration<String> keys = t.keys();
		Integer sum = 0;
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			sum += t.get(key);
		}
		Integer random = (int)(Math.random() * sum); 
		keys = t.keys();
		sum = 0;
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			sum += t.get(key);
			if (sum >= random) {
				return key;
			}
		}
		return "SOMETHINGBAD";
	}
	
	Vector<String> tokenizeDoc(String cur_doc) {
		String[] words = cur_doc.split("\\s+");
		Vector<String> tokens = new Vector<String>();
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].replaceAll("\\W", "");
			if (words[i].length() > 0) {
				tokens.add(words[i].toLowerCase());
			}
		}
		return tokens;
	}
	
	void increment(Hashtable<String, Integer> h, String k) {
		Integer i = null;
		if ((i = h.get(k)) != null) {
			h.put(k, i+1);
		} else {
			h.put(k, 1);
		}
	}
	
	void buildModel() {
		for (int i = 2; i < this.tokens.size(); i++) {
			String p2 = this.tokens.get(i-2);
			String p1 = this.tokens.get(i-1);
			String p = this.tokens.get(i);
			Hashtable<String, Hashtable<String, Integer>> level2 = null;
			if ((level2 = this.counts.get(p2)) != null) {
				Hashtable<String, Integer> level3 = null;
				if ((level3 = level2.get(p1)) != null) {
					increment(level3, p);
				} else {
					level3 = new Hashtable<String, Integer>();
					level3.put(p, 1);
					level2.put(p1, level3);
				}
			} else {
				level2 = new Hashtable<String, Hashtable<String, Integer>>();
				Hashtable<String, Integer> level3 = new Hashtable<String, Integer>();
				level3.put(p, 1);
				level2.put(p1, level3);
				this.counts.put(p2, level2);
			}
		}
	}

	public Model(String filename) {
		this.tokens = new Vector<String>();
		this.counts = new Hashtable<String, Hashtable<String, Hashtable<String, Integer>>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			this.tokens = new Vector<String>();
			while((line=br.readLine())!=null) {
				this.tokens.addAll(tokenizeDoc(line));
			}
			buildModel();
		} catch(IOException io) {
			io.printStackTrace();
		}		
	}
}
