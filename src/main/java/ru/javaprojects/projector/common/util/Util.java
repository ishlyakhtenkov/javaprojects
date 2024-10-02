package ru.javaprojects.projector.common.util;

import lombok.experimental.UtilityClass;
import ru.javaprojects.projector.common.HasIdAndParentId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@UtilityClass
public class Util {

    public static <T extends HasIdAndParentId, R extends TreeNode<T, R>> List<R> makeTree(List<T> nodes, Function<T, R> treeNodeCreator) {
        List<R> roots = new ArrayList<>();
        Map<Long, R> map = new HashMap<>();
        for (T node : nodes) {
            R treeNode = treeNodeCreator.apply(node);
            map.put(node.id(), treeNode);
            if (node.getParentId() == null) {
                roots.add(treeNode);
            }
        }
        for (T node : nodes) {
            if (node.getParentId() != null) {
                R parent = map.get(node.getParentId());
                R current = map.get(node.id());
                if (parent != null) {
                    parent.subNodes().add(current);
                } else {
                    roots.add(current);
                }
            }
        }
        return roots;
    }
}
