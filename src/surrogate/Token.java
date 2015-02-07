package surrogate;

public class Token implements java.io.Serializable {
	public String word;
	public String pos;
	
	public Token(String word, String pos) {
		this.word = word;
		this.pos = pos;
	}
	
	@Override
	public int hashCode() {
		return this.word.hashCode() + this.pos.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return this.word.equals(((Token)obj).word) && this.pos.equals(((Token)obj).pos);
	}
}
