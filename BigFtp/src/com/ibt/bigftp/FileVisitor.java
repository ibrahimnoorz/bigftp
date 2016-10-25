package com.ibt.bigftp;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class FileVisitor extends SimpleFileVisitor<Path> 
{
	BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();
	AtomicLong filesToProcessCount = new AtomicLong(0);
	boolean directoriesOnly = false;
	public FileVisitor(BlockingQueue<Path> queue, AtomicLong counter, boolean directoriesOnly)
	{
		this.fileQueue = queue;
		this.filesToProcessCount = counter;
		this.directoriesOnly = directoriesOnly;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attr) 
	{
		//if we are only interested in directories
		if(directoriesOnly) return FileVisitResult.CONTINUE;
		
		if (attr.isSymbolicLink()) {
			if(ConfigurationHandler.verbose) {
				System.out.format("Symbolic link: %s ", file);
				System.out.println("Symbolic links are not currently supported.");
			}
		} else if (attr.isRegularFile()) {
			
			//TODO: Can add checks here for pre-process tasks i.e.
			//TODO: -extension check, binary check, size check etc.
			
			//System.out.format("Regular file: %s ", file);
			filesToProcessCount.getAndIncrement();
			fileQueue.add(file);
		} else {
			if(ConfigurationHandler.verbose) {
				System.out.format("Other: %s ", file);
			}
		}
		//System.out.println("(" + attr.size() + "bytes)");
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
	{
		//System.out.format("Directory: %s%n", dir);
		filesToProcessCount.getAndIncrement();
		fileQueue.add(dir);
		return FileVisitResult.CONTINUE;
	}	
	
	// Print each directory visited.
//	@Override
//	public FileVisitResult postVisitDirectory(Path dir,
//			IOException exc) {
//		System.out.format("Directory: %s%n", dir);
//		fileQueue.add(dir);
//		return FileVisitResult.CONTINUE;
//	}

	// If there is some error accessing
	// the file, let the user know.
	// If you don't override this method
	// and an error occurs, an IOException 
	// is thrown.
//	@Override
//	public FileVisitResult visitFileFailed(Path file,
//			IOException exc) {
//		System.err.println(exc);
//		return FileVisitResult.CONTINUE;
//	}
}
//REFERENCES
//https://docs.oracle.com/javase/tutorial/essential/io/walk.html