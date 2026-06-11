package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.numbers.Number;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.HALF;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;

/**
 * Quadtree for the Barnes-Hut approximation of the gravity. It holds the total mass and the center of mass
 * of every node. It is built once per iteration and only read after that, so many threads can use it.
 */
public class BarnesHutTree {
    /**
     * Objects at (almost) the same position would subdivide forever. Deeper than this
     * the objects are collected in a simple list and treated exactly, pairwise.
     */
    public static final int MAX_DEPTH = 50;

    static class Node {
        final Number minX;
        final Number minY;
        final Number side;
        final Number sideSquared;
        final int depth;

        /* null means the node is a leaf; entries are created lazily and may be null */
        Node[] children;
        /* Index of the single object in a leaf; -1 otherwise */
        int objectIndex = -1;
        /* Objects collected at MAX_DEPTH; null otherwise */
        List<Integer> bucket;

        /* Mass distribution, calculated after the tree is built */
        Number mass;
        Number centerOfMassX;
        Number centerOfMassY;
        /* The biggest radius in the subtree; only when the tree is built with radiuses */
        Number maxRadius;

        Node(Number minX, Number minY, Number side, int depth) {
            this.minX = minX;
            this.minY = minY;
            this.side = side;
            this.sideSquared = side.multiply(side);
            this.depth = depth;
        }
    }

    private final Node root;
    private final Number[] x;
    private final Number[] y;
    private final Number[] mass;
    private final Number[] radius;

    private BarnesHutTree(Node root, Number[] x, Number[] y, Number[] mass, Number[] radius) {
        this.root = root;
        this.x = x;
        this.y = y;
        this.mass = mass;
        this.radius = radius;
    }

    Node getRoot() {
        return root;
    }

    /**
     * Builds the tree over the not deleted objects. The given arrays must not change
     * while the tree is in use (pass the read-only snapshots).
     */
    public static BarnesHutTree build(Number[] x, Number[] y, Number[] mass, boolean[] deleted, int n) {
        return build(x, y, mass, null, deleted, n);
    }

    /**
     * Builds the tree with object radiuses, so it can also be used for collision
     * detection with {@link #findOverlapCandidates(int)}.
     */
    public static BarnesHutTree build(Number[] x, Number[] y, Number[] mass, Number[] radius, boolean[] deleted, int n) {
        /* Bounding square of all alive objects */
        Number minX = null;
        Number maxX = null;
        Number minY = null;
        Number maxY = null;
        for (int i = 0; i < n; i++) {
            if (!deleted[i]) {
                if (minX == null) {
                    minX = x[i];
                    maxX = x[i];
                    minY = y[i];
                    maxY = y[i];
                } else {
                    minX = minX.min(x[i]);
                    maxX = maxX.max(x[i]);
                    minY = minY.min(y[i]);
                    maxY = maxY.max(y[i]);
                }
            }
        }

        if (minX == null) {
            return new BarnesHutTree(null, x, y, mass, radius);
        }

        Number side = maxX.subtract(minX).max(maxY.subtract(minY));
        if (side.compareTo(ZERO) == 0) {
            /* All objects are at the same point. Any positive side works. */
            side = ONE;
        }

        BarnesHutTree tree = new BarnesHutTree(new Node(minX, minY, side, 0), x, y, mass, radius);
        for (int i = 0; i < n; i++) {
            if (!deleted[i]) {
                tree.insert(i);
            }
        }
        tree.calculateMassDistribution(tree.root);
        return tree;
    }

    private void insert(int i) {
        Node node = root;
        while (true) {
            if (node.children != null) {
                /* Internal node - descend */
                node = childFor(node, i);
            } else if (node.bucket != null) {
                node.bucket.add(i);
                return;
            } else if (node.objectIndex == -1) {
                /* Empty leaf */
                node.objectIndex = i;
                return;
            } else if (node.depth == MAX_DEPTH) {
                /* Cannot subdivide further - collect the objects in a list */
                node.bucket = new ArrayList<>();
                node.bucket.add(node.objectIndex);
                node.bucket.add(i);
                node.objectIndex = -1;
                return;
            } else {
                /* Occupied leaf - subdivide and reinsert the existing object,
                 * then continue descending with the new one. */
                int existing = node.objectIndex;
                node.objectIndex = -1;
                node.children = new Node[4];
                childFor(node, existing).objectIndex = existing;
            }
        }
    }

    private Node childFor(Node node, int i) {
        Number half = node.side.multiply(HALF);
        Number middleX = node.minX.add(half);
        Number middleY = node.minY.add(half);
        boolean right = x[i].compareTo(middleX) > 0;
        boolean top = y[i].compareTo(middleY) > 0;
        int quadrant = (right ? 1 : 0) + (top ? 2 : 0);
        if (node.children[quadrant] == null) {
            node.children[quadrant] = new Node(right ? middleX : node.minX, top ? middleY : node.minY, half, node.depth + 1);
        }
        return node.children[quadrant];
    }

    private void calculateMassDistribution(Node node) {
        if (node.children == null) {
            if (node.bucket != null) {
                Number totalMass = ZERO;
                Number weightedX = ZERO;
                Number weightedY = ZERO;
                for (int k = 0; k < node.bucket.size(); k++) {
                    int i = node.bucket.get(k);
                    totalMass = totalMass.add(mass[i]);
                    weightedX = weightedX.add(mass[i].multiply(x[i]));
                    weightedY = weightedY.add(mass[i].multiply(y[i]));
                    if (radius != null) {
                        node.maxRadius = node.maxRadius == null ? radius[i] : node.maxRadius.max(radius[i]);
                    }
                }
                setMassDistribution(node, totalMass, weightedX, weightedY);
            } else {
                node.mass = mass[node.objectIndex];
                node.centerOfMassX = x[node.objectIndex];
                node.centerOfMassY = y[node.objectIndex];
                if (radius != null) {
                    node.maxRadius = radius[node.objectIndex];
                }
            }
        } else {
            Number totalMass = ZERO;
            Number weightedX = ZERO;
            Number weightedY = ZERO;
            for (Node child : node.children) {
                if (child != null) {
                    calculateMassDistribution(child);
                    totalMass = totalMass.add(child.mass);
                    weightedX = weightedX.add(child.mass.multiply(child.centerOfMassX));
                    weightedY = weightedY.add(child.mass.multiply(child.centerOfMassY));
                    if (radius != null) {
                        node.maxRadius = node.maxRadius == null ? child.maxRadius : node.maxRadius.max(child.maxRadius);
                    }
                }
            }
            setMassDistribution(node, totalMass, weightedX, weightedY);
        }
    }

    /**
     * Returns the objects which may overlap the given one; the distance still has to be checked.
     * A node is visited only if the object can reach it: the distance to the node is not bigger than
     * the radius of the object plus the biggest radius in the node. The result is sorted by index.
     */
    public List<Integer> findOverlapCandidates(int objectIndex) {
        List<Integer> result = new ArrayList<>();
        if (root != null) {
            collectOverlapCandidates(root, objectIndex, result);
            Collections.sort(result);
        }
        return result;
    }

    private void collectOverlapCandidates(Node node, int objectIndex, List<Integer> result) {
        Number reach = radius[objectIndex].add(node.maxRadius);
        if (minDistanceSquaredToNode(node, x[objectIndex], y[objectIndex]).compareTo(reach.multiply(reach)) > 0) {
            return;
        }
        if (node.children != null) {
            for (Node child : node.children) {
                if (child != null) {
                    collectOverlapCandidates(child, objectIndex, result);
                }
            }
        } else if (node.bucket != null) {
            result.addAll(node.bucket);
        } else {
            result.add(node.objectIndex);
        }
    }

    private Number minDistanceSquaredToNode(Node node, Number px, Number py) {
        Number dx = px.min(node.minX.add(node.side)).max(node.minX).subtract(px);
        Number dy = py.min(node.minY.add(node.side)).max(node.minY).subtract(py);
        return dx.multiply(dx).add(dy.multiply(dy));
    }

    private void setMassDistribution(Node node, Number totalMass, Number weightedX, Number weightedY) {
        node.mass = totalMass;
        if (totalMass.compareTo(ZERO) == 0) {
            /* Objects without mass exert no force; just give the node a defined center. */
            Number half = node.side.multiply(HALF);
            node.centerOfMassX = node.minX.add(half);
            node.centerOfMassY = node.minY.add(half);
        } else {
            node.centerOfMassX = weightedX.divide(totalMass);
            node.centerOfMassY = weightedY.divide(totalMass);
        }
    }
}
