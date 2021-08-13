package io.github.seggan.blockyworld.world.block;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Material {
    AIR("air"),
    STONE("stone");

    private String defaultTexture;
}
