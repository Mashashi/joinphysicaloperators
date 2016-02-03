package pt.mashashi;

import java.util.List;

public interface Join {
	String getName();
	List<Triple> join(List<Tuple> input1, List<Tuple> input2);
}
