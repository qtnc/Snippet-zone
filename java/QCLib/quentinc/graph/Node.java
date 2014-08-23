package quentinc.graph;
import java.util.*;
public interface Node<N extends Node<N,P>,P> {
public Iterable<N> getNeighbours (P param) ;
public double getDistanceTo (N node) ;
}
