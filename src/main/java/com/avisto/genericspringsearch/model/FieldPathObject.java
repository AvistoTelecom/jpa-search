package com.avisto.genericspringsearch.model;

/**
 * Path to the database field where you want to apply the operation.
 */
public class FieldPathObject {
    private final String left;
    private final String right;
    private final boolean needsJoin;

    private FieldPathObject(String left, String right, boolean needsJoin) {
        this.left = left;
        this.right = right;
        this.needsJoin = needsJoin;
    }

    public static FieldPathObject of(String path) {
        boolean needsJoin = path.contains("[");
        String[] split = path.split("\\[|\\]");
        return switch (split.length) {
            case 1 -> new FieldPathObject(split[0], null, needsJoin);
            case 2 -> new FieldPathObject(split[0], split[1], needsJoin);
            default -> new FieldPathObject(null, null, false);
        };
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public boolean needsJoin() {
        return needsJoin;
    }
}
