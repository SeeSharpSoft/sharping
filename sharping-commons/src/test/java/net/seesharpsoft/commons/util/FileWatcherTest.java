package net.seesharpsoft.commons.util;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FileWatcherTest {

    private static class MyWatcherImplementation extends FileWatcher {

        private int modifiedCounter = 0;

        public MyWatcherImplementation(File file) {
            super(file);
        }

        public int getModifiedCounter() {
            return modifiedCounter;
        }

        @Override
        public synchronized void onModified() {
            modifiedCounter++;
        }
    }

    @Test
    @Disabled
    public void should_watch_file() throws IOException, InterruptedException {
        File myFile = SharpIO.getFile("/filewatcher/changing.txt");

        MyWatcherImplementation watcher = new MyWatcherImplementation(myFile);
        watcher.startWatching();

        assertThat(watcher.modifiedCounter, is(0));

        try(FileWriter fileWriter = new FileWriter(myFile)) {
            fileWriter.write("Test");
            fileWriter.flush();
        }

        Thread.sleep(500);

        // TODO sometimes its 2, why?
        assertThat(watcher.modifiedCounter, is(1));

        watcher.stopWatching();
    }
}
