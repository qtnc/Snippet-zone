package quentinc.graph;
import java.util.*;

public class AStarPathFinder<N extends Node<N,P>,P> {
private class Item implements Comparable<Item> {
protected double cost1, cost2, totalCost;
protected Item parent;
protected N node;
public Item () {}
public Item (double a, double b, double c, Item p, N n) {
cost1=a;
cost2=b;
totalCost=c;
parent=p;
node=n;
}
public boolean equals (Object o) {
return ((Item)o).node.equals(node);
}
public int hashCode () { 
return node.hashCode();
}
public int compareTo (Item it) {
if (this.totalCost<=it.totalCost) return -1;
else return 1;
}}

private NavigableSet<Item> openList = new TreeSet<Item>();
private Set<Item> closedList = new HashSet<Item>();
private Deque<N> path = null;
private N start, end;
private Item tmp = new Item();
private P param;
private boolean impossible = false;

public AStarPathFinder (N s, N e, P p) {
start=s;
end=e;
param=p;
}
public AStarPathFinder (N s, N e) { this(s,e,null); }
public AStarPathFinder () { this(null,null,null); }

public void clear () {
openList.clear();
closedList.clear();
path = null;
impossible = false;
}
private void calculatePath () {
clear();
openList.add(new Item(0, 0, 0, null, start));
browseOpenList();
}
private void browseOpenList () {
Item e;
while ((e = openList.pollFirst())!=null) {
closedList.add(e);
if (e.node.equals(end)) break;
browseNeighbours(e);
}
if (e!=null && e.node.equals(end)) buildPath(e);
else impossible = true;
}
private void browseNeighbours (Item it) {
for (N n : it.node.getNeighbours(param)) {

tmp.node = n;
if (closedList.contains(tmp)) continue;
double cost1 = it.cost1 + it.node.getDistanceTo(n);
double cost2 = end.getDistanceTo(n);
double totalCost = cost1 + cost2;
if (openList.contains(tmp)) {
Item e = openList.ceiling(tmp);
if (e.totalCost <= totalCost) continue;
else {
openList.remove(e);
openList.add(new Item(cost1, cost2, totalCost, it, n));
}}
else openList.add(new Item(cost1, cost2, totalCost, it, n));
}}
private void buildPath (Item it) {
path = new LinkedList<N>();
while (!it.node.equals(start)) {
path.addFirst(it.node);
it = it.parent;
}}
public Queue<N> getPath () {
if (path==null && !impossible) calculatePath();
return path;
}
public Queue<N> getPath (N start, N end, P param) {
this.start = start;
this.end = end;
this.param = param;
calculatePath();
return path;
}
public Queue<N> getPath (N start, N end) { return getPath(start,end,null); }

}