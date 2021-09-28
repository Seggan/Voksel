/*
 * A light 2D Minecraft clone
 * Copyright (C) 2021 Seggan (segganew@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.seggan.blockyworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import io.github.seggan.blockyworld.util.MagicValues;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TextureUtils {

    public static Texture load(@NonNull String path, int widthMultiplier, int heightMultiplier) {
        Pixmap orig = new Pixmap(Gdx.files.internal(path));
        Pixmap newPix = new Pixmap(
            MagicValues.WORLD_SCREEN_RATIO * widthMultiplier,
            MagicValues.WORLD_SCREEN_RATIO * heightMultiplier,
            orig.getFormat()
        );
        newPix.drawPixmap(orig, 0, 0, orig.getWidth(), orig.getHeight(), 0, 0, newPix.getWidth(), newPix.getHeight());

        Texture texture = new Texture(newPix);

        orig.dispose();
        newPix.dispose();

        return texture;
    }

    public static Texture load(@NonNull String path) {
        return load(path, 1, 1);
    }
}
