package surrogate;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Model {

	Vector<String> tokens;
	public Hashtable<Token, Hashtable<Token, Hashtable<Token, Integer>>> counts;
/*
	public Token weakPredict(String pos2, Token pos1) throws Exception {
		Enumeration<Token> keys = this.counts.keys();
		while (keys.hasMoreElements()) {
			Token key = keys.nextElement();
			if (key.pos.equals(pos1)) {
				Hashtable<Token, Integer> level2 = null;
				if ((level2 = this.counts.get(key).get(pos1)) != null) {
					Enumeration<Token> words = level2.keys();
					while(words.hasMoreElements()) {
						return words.nextElement();
					}
				}
			}
		}
		throw new Exception("bad");
	}*/
	
	public Vector<Token> seed(String pos1, String pos2) {
		Vector<Token> tokens = new Vector<Token>();
		if (pos1.equals("IN")) {
			tokens.add(new Token("in", "IN"));
			tokens.add(new Token("the", "DT"));
		} else if (pos1.equals("DT")) {
			tokens.add(new Token("this", "DT"));
			tokens.add(new Token("is", "VBZ"));
		} else {
			tokens.add(new Token("we", "PRP"));
			tokens.add(new Token("should", "MD"));
		}
		return tokens;
	}
	
	public Token predict(Token p2, Token p1) throws Exception {
		Hashtable<Token, Integer> t = this.counts.get(p2).get(p1);
		Enumeration<Token> keys = t.keys();
		Integer sum = 0;
		while (keys.hasMoreElements()) {
			Token key = keys.nextElement();
			sum += t.get(key);
		}
		if (sum == 0) {
			throw new Exception("bad");
			//weakPredict(p2.pos, p1);
		}
		Integer random = (int) (Math.random() * (sum - 1));
		keys = t.keys();
		sum = 0;
		while (keys.hasMoreElements()) {
			Token key = keys.nextElement();
			sum += t.get(key);
			if (sum >= random) {
				return key;
			}
		}
		return null;
	}

	void increment(Hashtable<Token, Integer> h, Token k) {
		Integer i = null;
		if ((i = h.get(k)) != null) {
			h.put(k, i + 1);
		} else {
			h.put(k, 1);
		}
	}

	void buildModel(List<List<TaggedWord>> sentences) {
		this.counts = new Hashtable<Token, Hashtable<Token, Hashtable<Token, Integer>>>();
		for (List<TaggedWord> sentence : sentences) {
			if (sentence.size() < 3) {
				continue;
			} else {
				Token p2 = new Token("<s>", "<s>");
				Token p1 = new Token("<s>", "<s>");
				Token p;
				for (int i = 0; i < sentence.size()+1; i++) {
					if (i < sentence.size()) {
						TaggedWord word = sentence.get(i);
						if (i % 5 == 0) {
							p = new Token("potato", word.tag());
						} else {
							p = new Token(word.word(), word.tag());
						}
					} else {
						p = new Token("<end>", "<end>");
					}
					Hashtable<Token, Hashtable<Token, Integer>> level2 = null;
					if ((level2 = this.counts.get(p2)) != null) {
						Hashtable<Token, Integer> level3 = null;
						if ((level3 = level2.get(p1)) != null) {
							increment(level3, p);
						} else {
							level3 = new Hashtable<Token, Integer>();
							level3.put(p, 1);
							level2.put(p1, level3);
						}
					} else {
						level2 = new Hashtable<Token, Hashtable<Token, Integer>>();
						Hashtable<Token, Integer> level3 = new Hashtable<Token, Integer>();
						level3.put(p, 1);
						level2.put(p1, level3);
						this.counts.put(p2, level2);
					}
					p2 = p1;
					p1 = p;
				}
				
			}
		}
	}

	public Model(String filename) {

		FileOutputStream fos;
		ObjectOutputStream oos = null;

		try {
			FileInputStream inputFileStream = new FileInputStream("table.obj");
			ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
			this.counts = (Hashtable<Token, Hashtable<Token, Hashtable<Token, Integer>>>) objectInputStream.readObject();
		} catch (Exception e2) {
			e2.printStackTrace();
			System.out.println("Failed to deserialize");

			try {

				fos = new FileOutputStream(new File("table.obj"));
				oos = new ObjectOutputStream(fos);

			} catch (IOException e1) {

				e1.printStackTrace();

			}

			MaxentTagger tagger = new MaxentTagger("models/english-left3words-distsim.tagger");
			List<List<HasWord>> sentences = null;
			try {
				sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(filename)));
			} catch (IOException io) {
				io.printStackTrace();
			}
			List<List<TaggedWord>> tSentences = new Vector<List<TaggedWord>>();
			for (List<HasWord> sentence : sentences) {
				List<TaggedWord> tSentence = tagger.tagSentence(sentence);
				tSentences.add(tSentence);
			}
			System.out.println("Done tagging");
			buildModel(tSentences);
			System.out.println("Done building model");

			try {

				oos.writeObject(this.counts);
				oos.flush();
				oos.close();

			} catch (IOException e) {

				e.printStackTrace();

			}
		}
	}
}
