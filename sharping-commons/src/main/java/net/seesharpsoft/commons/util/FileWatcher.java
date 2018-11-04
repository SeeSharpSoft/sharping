package net.seesharpsoft.commons.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

import static java.nio.file.StandardWatchEventKinds.*;

public abstract class FileWatcher {
    private Path folderPath;
    private String watchFile;

    private WatchKey watchKey;

    Thread watcherThread = null;

    public FileWatcher(File file) {
        this(file.getAbsolutePath());
    }

    public FileWatcher(String watchFile) {
        Path filePath = Paths.get(watchFile);

        boolean isRegularFile = Files.isRegularFile(filePath);

        if (!isRegularFile) {
            // Do not allow this to be a folder since we want to watch files
            throw new IllegalArgumentException(watchFile + " is not a regular file");
        }

        // This is always a folder
        folderPath = filePath.getParent();

        // Keep this relative to the watched folder
        this.watchFile = watchFile.replace(folderPath.toString() + File.separator, "");
    }

    public void stopWatching() {
        if (watchKey != null) {
            watchKey.cancel();
            watchKey = null;
        }
        if (watcherThread != null) {
            watcherThread.interrupt();
            watcherThread = null;
        }
    }

    public void startWatching() throws IOException {
        stopWatching();

        Runnable runnable =
                () -> {
                    // We obtain the file system of the Path
                    FileSystem fileSystem = folderPath.getFileSystem();

                    // We create the new WatchService using the try-with-resources block
                    try (WatchService service = fileSystem.newWatchService()) {
                        // We watch for modification events
                        folderPath.register(service, ENTRY_MODIFY);

                        // Start the infinite polling loop
                        while (true) {
                            // Wait for the next event
                            watchKey = service.take();

                            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                                // Get the type of the event
                                Kind<?> kind = watchEvent.kind();

                                if (kind == ENTRY_MODIFY) {
                                    Path watchEventPath = (Path) watchEvent.context();

                                    // Call this if the right file is involved
                                    if (watchEventPath.toString().equals(watchFile)) {
                                        onModified();
                                    }
                                }
                            }

                            if (!watchKey.reset()) {
                                // Exit if no longer valid
                                break;
                            }
                        }
                    } catch (IOException | InterruptedException iexc) {
                        this.stopWatching();
                    }
                };
        watcherThread = new Thread(runnable);
        watcherThread.start();
    }

    public abstract void onModified();
}