package com.ibt.bigftp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class BigFtp 
{
	public BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();
	final static String TAG = "BigFtp v1.4";
	AtomicLong fileCount = new AtomicLong(0);
	AtomicLong filesToProcessCount = new AtomicLong(0);
	final int workerCount = ConfigurationHandler.workerCount;
	List<Worker> workers = new ArrayList<>();	
	public boolean processWork()
	{
		boolean result = false;
		try
		{
			for(int i = 1; i <= workerCount; i++)
			{
				FtpHandler ftp;
				if(ConfigurationHandler.ftpMode.compareToIgnoreCase("FTPS") == 0){
					System.out.println("Using FTPS.");
					ftp = new FtpsProcessor(ConfigurationHandler.ftpMode);
				} else {
					System.out.println("Using FTP.");
					ftp = new FtpProcessor(ConfigurationHandler.ftpMode);
				}
				result = ftp.connect(ConfigurationHandler.ftpHost, ConfigurationHandler.ftpPort);
				if(!result) return result;
				result = ftp.login(ConfigurationHandler.ftpUser, ConfigurationHandler.ftpPass);
				if(!result) return result;		

				Worker worker = new Worker(i, ftp);
				workers.add(worker);
			}

			for(Worker worker : workers)
			{
				worker.startWorking();
			}

			result = true;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}


	public void stopWork()
	{
		for(Worker worker : workers)
		{
			if(ConfigurationHandler.verbose){
				System.out.println(new Date() + " - Stopping worker " + worker.workerId);
			}
			worker.stopWorking();
		}
		if(ConfigurationHandler.verbose){
			System.out.println(new Date() + " - All work has been completed.");
		}
	}

	public class Worker
	{
		boolean isRunning = false;
		int workerId;
		ExecutorService execService = Executors.newFixedThreadPool(1);
		FtpHandler ftp;

		public Worker(int id, FtpHandler ftp){
			workerId = id;
			this.ftp = ftp;
		}

		public void startWorking()
		{			
			if(ConfigurationHandler.verbose) {
				System.out.println("Worker " + workerId + " started.");
			}
			execService.execute(new Runnable()
			{
				public void run()
				{
					isRunning = true;
					while(isRunning)
					{
						try
						{
							Path path = fileQueue.take();
							boolean isDir = path.toFile().isDirectory(); 
							if(isDir){
								processDir(path);
							} else {
								processFile(path);
							}
						}catch(Exception ex){
							//ex.printStackTrace();
						}
					}
				}
			});
		}

		public String makeRemotePath(Path path)
		{
			String rp = path.toFile().getAbsolutePath();
			rp = rp.replace("\\", "/");

			int index = rp.indexOf(ConfigurationHandler.localPath);
			if(index >= 0){
				rp = rp.substring(ConfigurationHandler.localPath.length());
			} else {
				if(ConfigurationHandler.verbose) {
					System.out.println("Could not find " + ConfigurationHandler.localPath + " in " + rp);
				}
			}

			rp = rp.startsWith("/") ? rp.substring(1) : rp;

			rp = ConfigurationHandler.remotePath 
					+ (ConfigurationHandler.remotePath.endsWith("/") ? "" : "/")
					+ rp;						

			return rp;
		}

		public void processDir(Path path)
		{
			//TODO: check if directory exists on ftp, if not create it
			//System.out.println("Processing " + path.toFile().getAbsolutePath());
			try
			{
				String rp = makeRemotePath(path);
				if(!ftp.createFtpDirectoryIfMissing(rp)){
					//The reason could be that it already exists
				}
			}catch(Exception ex){
				ex.printStackTrace();
			} finally{
				filesToProcessCount.decrementAndGet();	
			}
		}

		public void processFile(Path path)
		{
			try
			{
				//TODO: Check if using File instead of Path maybe more efficient
				String rp = makeRemotePath(path);
				ftp.putFile(path, rp);
			}catch(Exception ex){

			} finally{
				filesToProcessCount.decrementAndGet();
				fileCount.incrementAndGet();
			}			
		}

		public void stopWorking()
		{
			isRunning = false;
			execService.shutdownNow();	
			ftp.disconnect();
		}
	}


	public static void usage()
	{
		System.out.println(new Date() + " " + TAG);
		System.out.println("A valid bigftp.properties file needs to be provided.");
	}

	//process directories
	public static void syncLocalPathToFtp(boolean directoriesOnly) throws Exception
	{
		BlockingQueue<Integer> workTokensQueue = new LinkedBlockingQueue<>();
		BigFtp bftp = new BigFtp();
		Path sPath = Paths.get(ConfigurationHandler.localPath);
		Instant start = Instant.now();

		Thread t = new Thread(new Runnable(){
			public void run(){
				try{
					FileVisitor visitor = new FileVisitor(bftp.fileQueue, bftp.filesToProcessCount, directoriesOnly);
					Files.walkFileTree(sPath, visitor);
				}catch(Exception ex){
					ex.printStackTrace();
				}					
			}
		});
		t.start();
		//NOTE: Give filewalker a 1 seconds headstart
		//Thread.sleep(1000);
		if(!bftp.processWork()){
			System.out.println("Error starting up workers.");
			System.exit(-1);
		}

		//a maintainer that will wait till work is completed
		Thread tMaintainer = new Thread(new Runnable(){
			public void run(){
				while(bftp.filesToProcessCount.get() > 0){
					try{
						if(ConfigurationHandler.verbose) {
							//TODO: This metric is not useful due to variation in file sizes. Replace with data tranfer metric
							long timeDiff = System.currentTimeMillis() - start.toEpochMilli();
							timeDiff = timeDiff / 1000;
							if(timeDiff == 0){
								timeDiff = 1;
							}
							long totalFileCount = bftp.fileCount.get();
							System.out.format(new Date().toString() + " - Files processed so far: %d [%.2f files/sec]\n", totalFileCount, (double)totalFileCount/timeDiff );
						}
						Thread.sleep(3000);
					}catch(InterruptedException ie){
						ie.printStackTrace();
					}						
				}

				if(ConfigurationHandler.verbose) {
					System.out.println("All files have been processed. Stopping...");
				}
				bftp.stopWork();	
				workTokensQueue.add(1);
			}
		});

		tMaintainer.start();

		workTokensQueue.take();	
		Instant end = Instant.now();	
		if(ConfigurationHandler.verbose) {
			System.out.println("Time taken: " + Duration.between(start, end));
		}
	}


	public static void main(String[] args)	
	{
		BlockingQueue<Integer> workTokensQueue = new LinkedBlockingQueue<>();
		if(!ConfigurationHandler.loadProperties()){
			System.out.println("Error loading configuration file bigftp.properties");
			System.exit(-1);
		}

		System.out.println("BigFtp v1.4");
		System.out.println(new Date() + " - Started...");
		System.out.println("Synching " + ConfigurationHandler.localPath + " to " + ConfigurationHandler.remotePath
				+ " On Ftp Server " + ConfigurationHandler.ftpHost);
		try
		{
			//complete the directories
			syncLocalPathToFtp(true);

			//now do the files
			syncLocalPathToFtp(false);
		}catch(Exception ex){
			ex.printStackTrace();
		}

	}
}