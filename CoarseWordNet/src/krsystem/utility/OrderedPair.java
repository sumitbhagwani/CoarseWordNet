package krsystem.utility;

public class OrderedPair<L, R> {
	private L l;
	private R r;
	
	public OrderedPair(L l, R r) {
		this.l = l;
		this.r = r;
	}
	
	public L getL() {
		return l;
	}
	
	public R getR() {
		return r;		
	}
	
	public void setL(L l) {
		this.l = l;
	}
	
	public void setR(R r) {
		this.r = r;
	}
	
	public String toString() {
		return "(" + this.l.toString() + ", " + this.r.toString() + ")";
	}
	
	public boolean equals(OrderedPair<L, R> otherPair)
	{
		if(this.l.equals(otherPair.getL()) && this.r.equals(otherPair.getR()))
			return true;
		return false;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
