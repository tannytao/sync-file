package com.tnt.test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
 
// Simple class to watch directory events.
class DirectoryWatcher implements Runnable {
 
    private Path path;
 
    public DirectoryWatcher(Path path) {
        this.path = path;
    }
 
    // print the events and the affected file
    private void printEvent(WatchEvent<?> event) {
        Kind<?> kind = event.kind();
        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            Path pathCreated = (Path) event.context();
            System.out.println("Entry created:" + pathCreated);
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            Path pathDeleted = (Path) event.context();
            System.out.println("Entry deleted:" + pathDeleted);
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
            Path pathModified = (Path) event.context();
            System.out.println("Entry modified:" + pathModified);
        }
    }
 
    @Override
    public void run() {
        try {
            WatchService watchService = path.getFileSystem().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
 
            // loop forever to watch directory
            while (true) {
                WatchKey watchKey;
                watchKey = watchService.take(); // this call is blocking until events are present
 
                // poll for file system events on the WatchKey
                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    printEvent(event);
                }
 
                // if the watched directed gets deleted, get out of run method
                if (!watchKey.reset()) {
                    System.out.println("No longer valid");
                    watchKey.cancel();
                    watchService.close();
                    break;
                }
            }
 
        } catch (InterruptedException ex) {
            System.out.println("interrupted. Goodbye");
            return;
        } catch (IOException ex) {
            ex.printStackTrace();  // don't do this in production code. Use a loggin framework
            return;
        }
    }
}
 
public class FileEventTest {
 
    public static void main(String[] args) throws InterruptedException {
        Path pathToWatch = FileSystems.getDefault().getPath("C:\\to1");
        DirectoryWatcher dirWatcher = new DirectoryWatcher(pathToWatch);
        Thread dirWatcherThread = new Thread(dirWatcher);
        dirWatcherThread.start();
         
        // interrupt the program after 10 seconds to stop it.
        Thread.sleep(10000);
        dirWatcherThread.interrupt();
 
    }
}