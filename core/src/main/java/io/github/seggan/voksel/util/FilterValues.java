package io.github.seggan.voksel.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FilterValues {

    public static final short BLOCK_CATEGORY = 0b1000_0000;
    public static final short NON_PASSABLE_BLOCK_CATEGORY = 0b0100_0000;
    public static final short NON_TRANSPARENT_BLOCK_CATEGORY = 0b0010_0000;

    public static final short ENTITY_MASK = BLOCK_CATEGORY;
    public static final short ENTITY_CATEGORY = 0b0000_1000;

    public static final short SUN_MASK = NON_TRANSPARENT_BLOCK_CATEGORY | BLOCK_CATEGORY;
    public static final short SUN_CATEGORY = 0b0000_0100;

    public static final short BLOCK_MASK = ENTITY_CATEGORY | SUN_CATEGORY;
}
