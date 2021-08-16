package io.github.seggan.blockyworld.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.seggan.blockyworld.BlockyWorld;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Launches the desktop (LWJGL3) application.
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) throws FileNotFoundException {
		PrintStream out = new PrintStream("log.txt");
		//System.setOut(out);
		//System.setErr(out);
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new BlockyWorld(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("BlockyWorld");
        configuration.setWindowedMode(600, 600);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}