package pt.mashashi;

public class Tuple {

	  private int id;
	  private int value;

	  public Tuple(int id, int value) {
	    this.id = id;
	    this.value = value;
	  }

	  public int getID() { return id; }
	  public int getValue() { return value; }

	  public Tuple clone(){
		  return new Tuple(this.id, this.value);
	  }
	  
}