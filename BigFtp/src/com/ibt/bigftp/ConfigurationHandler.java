package com.ibt.bigftp;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * ConfigurationHandler 
 */
public class ConfigurationHandler
{
	public static final String PROP_FTP_HOSTNAME              	= "hostname";
	public static final String PROP_FTP_PORT             		= "port";
	public static final String PROP_FTP_USER             		= "user";
	public static final String PROP_FTP_PASS             		= "pass";
	public static final String PROP_FTP_MODE           			= "mode";
	public static final String PROP_WORKER_COUNT				= "workers";
	public static final String PROP_LOCAL_PATH 					= "localpath";
	public static final String PROP_REMOTE_PATH 				= "remotepath";
	public static final String PROP_VERBOSE_MODE				= "verbose";


	public static String WORKFOLDER = System.getProperty("user.dir");
	public static final String CONFIG_FILENAME = "bigftp.properties";
	public static String ftpHost = "";
	public static int ftpPort = 21;
	public static String ftpUser = "";
	public static String ftpPass = "";
	public static String ftpMode = "";
	public static boolean verbose = true;
	
	public static int workerCount = 2;
	
	public static String localPath;
	public static String remotePath;
	
	public static final String LOGGER_FILENAME = "log4j.properties"; 
	public static final String LOGGER_CONSOLE = "ConsoleLogger";
	public static final String LOGGER_DEBUG = "debuglogger";
	public static final String LOGGER_APP = "applogger";


	//loads values from a configuration properties file
	private ConfigurationHandler(){
	}

	public static boolean loadProperties()
	{
		return loadProperties(CONFIG_FILENAME);
	}

	public static boolean loadProperties(String propertiesFile)
	{
		boolean result = false;
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(CONFIG_FILENAME);

			// load a properties file
			prop.load(input);

			//ftpHost
			if(prop.containsKey(PROP_FTP_HOSTNAME)){
				ftpHost = prop.getProperty(PROP_FTP_HOSTNAME).trim();
			}

			//ftpPort
			if(prop.containsKey(PROP_FTP_PORT)){
				ftpPort = Integer.parseInt(prop.getProperty(PROP_FTP_PORT).trim());
			}

			//ftpUser
			if(prop.containsKey(PROP_FTP_USER)){
				ftpUser = prop.getProperty(PROP_FTP_USER).trim();
			}

			//ftpPass
			if(prop.containsKey(PROP_FTP_PASS)){
				ftpPass = prop.getProperty(PROP_FTP_PASS).trim();
			}

			//ftpMode
			if(prop.containsKey(PROP_FTP_MODE)){
				ftpMode = prop.getProperty(PROP_FTP_MODE).trim();
			}         

			//localPath
			if(prop.containsKey(PROP_LOCAL_PATH)){
				localPath = prop.getProperty(PROP_LOCAL_PATH).trim();
			}  
			
			//remotePath
			if(prop.containsKey(PROP_REMOTE_PATH)){
				remotePath = prop.getProperty(PROP_REMOTE_PATH).trim();
			}
			
			//workerCount
			if(prop.containsKey(PROP_WORKER_COUNT)){
				workerCount = Integer.parseInt(prop.getProperty(PROP_WORKER_COUNT).trim());
			}

			//workerCount
			if(prop.containsKey(PROP_VERBOSE_MODE)){
				verbose = Boolean.parseBoolean(prop.getProperty(PROP_VERBOSE_MODE).trim());
			}

			//default to regular ftp
			if(ConfigurationHandler.ftpMode == null || ConfigurationHandler.ftpMode.length() == 0){
				ConfigurationHandler.ftpMode = "FTP";
			}

			result = true;            
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
