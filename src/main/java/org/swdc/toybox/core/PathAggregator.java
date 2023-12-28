package org.swdc.toybox.core;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 路径聚合器
 *
 * 聚合路径对象，让路径的数量减少，提高索引的效率
 */
public class PathAggregator {

    private List<FSPathAggregate> parts = new ArrayList<>();

    private List<FSPathAggregate> roots = new ArrayList<>();

    public PathAggregator() {

    }

    /**
     * 添加根路径
     * @param path 绝对路径
     */
    public void addRootPath(String path) {
        roots.add(new FSPathAggregate(path));
    }

    /**
     * 路径聚合
     * 将另一个路径通过路径交错的方法添加到本聚合器的列表中。
     *
     * @param path 绝对路径
     */
    public void aggregate(String path) {

        FSPathAggregate curr = new FSPathAggregate(path);

        Map<FSPathAggregate,FSPathAggregate> matches = new HashMap<>();
        for (FSPathAggregate part: parts) {
            // 依次进行路径交错
            FSPathAggregate matched = part.getMatchedPart(curr);
            if (matched != null) {
                matches.put(part,matched);
            }
        }

        if (!matches.isEmpty()) {

            // 取路径最长的聚合对象
            List<Map.Entry<FSPathAggregate,FSPathAggregate>> sortedList = matches.entrySet().stream()
                    .sorted(Comparator.comparingInt(p -> p.getValue().size()))
                    .collect(Collectors.toList());

            Map.Entry<FSPathAggregate,FSPathAggregate> latest = sortedList.get(sortedList.size() - 1);
            FSPathAggregate target = latest.getValue();

            if (insideRoot(target)) {
                // 聚合对象包含在根路径内
                parts.remove(latest.getKey());
                parts.add(target);
            } else {
                parts.add(curr);
            }

        } else {
            parts.add(curr);
        }

        // 聚合对象去重
        parts = parts.stream().distinct()
                .collect(Collectors.toList());
    }

    private boolean insideRoot(FSPathAggregate path) {
        for (FSPathAggregate aggregate: roots) {
            boolean isInside = aggregate.includeOf(path);
            if (isInside) {
                return true;
            }
        }
        return false;
    }

    public int size(){
        return parts.size();
    }

    /**
     * 弹出所有的路径，返回所有的路径并清空本聚合器。
     * @return 路径列表。
     */
    public List<String> popAllPaths() {
        List<String> part = new ArrayList<>();
        for (FSPathAggregate FSPathAggregate : parts) {
            part.add(FSPathAggregate.getByLevel(FSPathAggregate.size()));
        }
        parts.clear();
        return part;
    }


}
