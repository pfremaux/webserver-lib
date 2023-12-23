package webserver.toolstmp;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public class NodeV2<T> {

    public static final String EMPTY = ".";
    private final NodeV2<T> parent;
    private String nodeName;
    private T value; // MUTABLE
    private final List<NodeV2<T>> children;

    public static <T> NodeV2<T> root() {
        return dir(null, "root", new ArrayList<>());
    }

    public static <T> NodeV2<T> leaf(NodeV2<T> parent, T value) {
        return new NodeV2<T>(parent, value, null, new ArrayList<>());
    }

    public static <T> NodeV2<T> dir(NodeV2<T> parent, String nodeName, List<NodeV2<T>> children) {
        return new NodeV2<T>(parent, null, nodeName, children);
    }

    private NodeV2(NodeV2<T> parent, T value, String nodeName, List<NodeV2<T>> children) {
        this.parent = parent;
        this.nodeName = nodeName;
        this.value = value;
        this.children = children;
    }

    public NodeV2<T> getNode(List<String> hierarchy) {
        if (hierarchy.isEmpty()) {
            return null;
        }
        String levelNodeName = hierarchy.get(0);
        Optional<NodeV2<T>> first = children.stream().filter(node -> node.getNodeName().equals(levelNodeName)).findFirst();
        if (first.isEmpty()) {
            return null;
        }
        final NodeV2<T> tNodeV2 = first.get();
        if (hierarchy.size() == 1) {
            return tNodeV2;
        }
        return tNodeV2.getNode(hierarchy.subList(1, hierarchy.size()));
    }

    public NodeV2<T> getParent() {
        return parent;
    }

    public List<NodeV2<T>> getChildren() {
        return children;
    }

    public T getValue() {
        return value;
    }

    public String getNodeName() {
        if (nodeName == null) {
            return value.toString();
        }
        return nodeName;
    }

    public DefaultMutableTreeNode getTreeNodeInstance(boolean allowChildren) {
        final DefaultMutableTreeNode currentNode;
        currentNode = new DefaultMutableTreeNode(this, allowChildren);
        if (!isLeaf()) {
            for (NodeV2<T> subValue : getChildren()) {
                currentNode.add(subValue.getTreeNodeInstance(!getChildren().isEmpty()));
            }
        }
        return currentNode;
    }

    public Map<String,T> getHierarchy(List<String> parentContext) {
        final Map<String, T> result = new HashMap<>();
        for (NodeV2<T> child : getChildren()) {
            final List<String> s =new ArrayList<>(parentContext);
            s.add(child.getNodeName());
            if (!child.isLeaf()) {
                result.putAll(child.getHierarchy(s));
            } else {
                final String key = String.join(" > ", s);
                result.put(key, child.value);
            }
        }
        return result;
    }

    public NodeV2<T> addLeaf(T value) {
        /*if (isLeaf()) {
            throw new UnsupportedOperationException("Can't add a leaf to a leaf.");
        }*/
        NodeV2<T> leaf = NodeV2.leaf(this, value);
        children.add(leaf);
        return leaf;
    }

    public NodeV2<T> addDir(String value) {
        if (isLeaf()) {
            throw new UnsupportedOperationException("Can't add a directory to a leaf.");
        }
        NodeV2<T> dir = NodeV2.dir(this, value, new ArrayList<>());
        children.add(dir);
        return dir;
    }

    public boolean isLeaf() {
        return value != null;
    }

    public void replaceValueWith(T credentialDatum) {
        if (!isLeaf()) {
            throw new UnsupportedOperationException("A directory has not value to replace.");
        }
        value = credentialDatum;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public String toString() {
        return getNodeName() + " ; value =" + getValue();
    }

    public NodeV2<T> getChildByNodeName(String hierarchyLevelName) {
        for (NodeV2<T> child : children) {
            if (child.getNodeName().equals(hierarchyLevelName)) {
                return child;
            }
        }
        return null;
    }

    public NodeV2<T> addSibling(T value) {
        return getParent().addLeaf(value);
    }

    //This class first expected nodes to not hold values, just other branches. But after more practice it's more convenient for nodes to also hold a value
    public void setValue(T val) {
        this.value = val;
    }
}