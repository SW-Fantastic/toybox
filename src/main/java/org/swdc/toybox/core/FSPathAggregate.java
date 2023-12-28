package org.swdc.toybox.core;

import java.io.File;
import java.nio.file.Path;
import java.util.StringTokenizer;

/**
 * 用于路径处理的路径分析对象
 *
 * 主要目的是判断路径的位置关系，路径对比和交叉。
 */
public class FSPathAggregate {

    private String[] parts;

    private String originalPath;

    /**
     * 路径聚合对象
     * @param path 绝对路径
     */
    public FSPathAggregate(String path) {
        this.originalPath = Path.of(path).toAbsolutePath().normalize().toString();
        StringTokenizer tokenizer = new StringTokenizer(path,File.separator);
        parts = new String[tokenizer.countTokens()];
        int idx = 0;
        while (tokenizer.hasMoreTokens()) {
            parts[idx++] = tokenizer.nextToken();
        }
    }

    /**
     * 创建路径聚合对象
     * @param parts 以File#speator拆分的路径数组
     */
    private FSPathAggregate(String[] parts) {
        this.parts = parts;
        this.originalPath = getByLevel(size());
    }

    /**
     * 按照路径层级获取路径
     * @param level 路径层级
     * @return 绝对路径
     */
    public String getByLevel(int level) {
        StringBuilder path = new StringBuilder();
        for (int idx = 0; idx < level; idx ++) {
            path.append(idx > 0 ? File.separator : "").append(parts[idx]);
        }
        return path.toString();
    }

    /**
     * 路径的Size
     * @return
     */
    public int size() {
        return parts.length;
    }

    /**
     * 判断路径是否包含其他的路径
     * @param aggregate 路径聚合对象
     * @return 是否包含
     */
    public boolean includeOf(FSPathAggregate aggregate) {
        if (this.size() >= aggregate.size()) {
            return false;
        }
        for (int idx = 0; idx < this.size(); idx ++) {
            if (!this.parts[idx].equals(aggregate.parts[idx])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否被其他路径包含
     * @param aggregate 路径聚合对象
     * @return 是否被包含
     */
    public boolean insideOf(FSPathAggregate aggregate) {
        return aggregate.includeOf(this);
    }

    /**
     * 路径聚合，返回两个路径的最大交集
     * @param part 被交错的路径聚合对象
     * @return 路径交错的结果
     */
    public FSPathAggregate getMatchedPart(FSPathAggregate part) {
        FSPathAggregate base = null;
        FSPathAggregate target = null;
        if (this.size() > part.size()) {
            base = part;
            target = this;
        } else {
            base = this;
            target = part;
        }
        for (int idx = 0; idx < base.size(); idx ++) {
            if(!base.parts[idx].equals(target.parts[idx])) {
                if (idx == 0) {
                    return null;
                }
                String[] arr = new String[idx];
                System.arraycopy(target.parts,0,arr,0,arr.length);
                return new FSPathAggregate(arr);
            }
        }
        return base;
    }

    /**
     * 路径的匹配
     * @param obj 任意对象
     * @return 是否为同一对象
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FSPathAggregate) {
            FSPathAggregate aggregate = (FSPathAggregate) obj;
            return aggregate.originalPath.equals(this.originalPath);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return originalPath.hashCode();
    }
}
