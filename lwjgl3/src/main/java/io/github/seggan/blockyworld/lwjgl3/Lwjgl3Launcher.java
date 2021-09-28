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

package io.github.seggan.blockyworld.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.seggan.blockyworld.BlockyWorld;
import io.github.seggan.blockyworld.util.MagicValues;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Launches the desktop (LWJGL3) application.
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) throws IOException, InterruptedException {
		PrintStream out = new PrintStream("log.txt");
		//System.setOut(out);
		//System.setErr(out);
        BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        if (available()) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        new ClientServerImpl(queue);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    } catch (InterruptedException e) {
                        System.exit(0);
                    }
                }
            }.start();
        } else {
            queue.add(new Object());
        }

        queue.take();

        System.out.println(System.getProperty("user.dir"));

        new Lwjgl3Application(new BlockyWorld(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("BlockyWorld");
        configuration.setWindowedMode(600, 600);
        configuration.setWindowIcon("icon.png");
        return configuration;
    }

    /**
     * Checks to see if the port is available
     */
    private static boolean available() {
        try(DatagramSocket ds = new DatagramSocket(MagicValues.PORT)) {
            ds.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
        }

        return false;
    }
}