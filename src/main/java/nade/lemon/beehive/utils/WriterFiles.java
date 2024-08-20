package nade.lemon.beehive.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriterFiles {
    
    public static void writer(File file, String string) {
        WriterFiles.writer(file, string, false);
    }

    public static void writer(File file, String string, boolean replace) {
        try {
            if (!replace && file.exists()) {
                return;
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
