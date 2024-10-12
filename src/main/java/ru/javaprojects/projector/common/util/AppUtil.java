package ru.javaprojects.projector.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.lang.NonNull;
import ru.javaprojects.projector.common.HasIdAndParentId;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.to.FileTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ru.javaprojects.projector.common.util.FileUtil.normalizePath;

@UtilityClass
public class AppUtil {
    public static <T extends HasIdAndParentId, R extends TreeNode<T, R>> List<R> makeTree(List<T> nodes,
                                                                                          Function<T, R> treeNodeCreator) {
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

    //  https://stackoverflow.com/a/65442410/548473
    @NonNull
    public static Throwable getRootCause(@NonNull Throwable t) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(t);
        return rootCause != null ? rootCause : t;
    }

    public static File createFile(Supplier<FileTo> fileToExtractor, String filesPath, String dir) {
        String filename = normalizePath(fileToExtractor.get().getRealFileName());
        return new File(filename, filesPath + normalizePath(dir + filename));
    }
}
