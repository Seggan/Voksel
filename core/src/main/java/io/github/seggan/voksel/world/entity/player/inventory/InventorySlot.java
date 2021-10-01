package io.github.seggan.voksel.world.entity.player.inventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import io.github.seggan.voksel.util.TextureUtils;

import lombok.Getter;

@Getter
public final class InventorySlot {

    public static final Texture TEXTURE = TextureUtils.load("inventory_slot.png", 2, 2);

    private final Sprite sprite;
    private final int position;

    public InventorySlot(int position) {
        this.position = position;
        this.sprite = new Sprite(TEXTURE);
    }
}
