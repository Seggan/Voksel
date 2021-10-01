package io.github.seggan.voksel.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.seggan.voksel.world.entity.player.inventory.InventorySlot;

import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

public class InventoryScreen implements Screen {

    private final MainScreen screen;
    private final Camera camera;

    private final Set<InventorySlot> slots = new HashSet<>();

    public InventoryScreen(@NonNull MainScreen screen) {
        this.screen = screen;
        this.camera = new OrthographicCamera();

        for (int i = 0; i < 40; i++) {
            slots.add(new InventorySlot(i));
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(new Color(0x91faeeff));

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            this.dispose();
            this.screen.game().setScreen(screen);
        }

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
