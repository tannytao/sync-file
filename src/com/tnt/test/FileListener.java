package com.tnt.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class FileListener {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			WatchService watchService = FileSystems.getDefault().newWatchService();
			File watchDirFile = new File("C:\\to1");
			Path watchDirPath = watchDirFile.toPath();
			File watchDirFile1 = new File("C:\\to");
			Path watchDirPath1 = watchDirFile1.toPath();
			watchDirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
			watchDirPath1.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
			while (true) {
                WatchKey watchKey = watchService.take(); // this call is blocking until events are present
 
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
//			WatchKey watchKey = watchService.take();
//			for (WatchEvent<?> event : watchKey.pollEvents()) {
//			   System.out.println(
//			   "An event was found after file creation of kind " + event.kind() 
//			   + ". The event occurred on file " + event.context() + ".");
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void printEvent(WatchEvent<?> event) {
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

}
