package ru.javaprojects.javaprojects.common.util;

import ru.javaprojects.javaprojects.common.HasIdAndParentId;

import java.util.List;

public interface TreeNode<T extends HasIdAndParentId, R extends TreeNode<T, R>> {
    List<R> subNodes();
}
