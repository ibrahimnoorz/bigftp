package com.ibt.bigftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

public class FtpProcessor implements FtpHandler
{
    FTPClient ftpClient;
	//FTPClient ftps;
    int bufferSize = 4096;
    
    public FtpProcessor(String protocol)
    {
		ftpClient = new FTPClient();
    	ftpClient.setBufferSize(bufferSize);
    }
    
    /*
     * connect
     * connects to the ftp server using host (hostname:port)
     */
    public boolean connect(String host, int port)
    {
    	boolean result = false;
        try
        {
            int reply;

            ftpClient.connect(host, port);
            System.out.println("Connected to " + host);

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftpClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
            	ftpClient.disconnect();
                System.err.println("FTP server refused connection.");
                result = false;
            }
            
            result = true;
        }
        catch (IOException e)
        {
            if (ftpClient.isConnected())
            {
                try
                {
                	ftpClient.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server.");
            e.printStackTrace();
        }
        return result;
    }
    
    public boolean login(String user, String pass)
    {
    	boolean result = true;
        try
        {
            if (!ftpClient.login(user, pass))
            {
            	ftpClient.logout();
                result = false;
            }

            if(result)
            {
	            System.out.println("Remote system is " + ftpClient.getSystemName());
	            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	            ftpClient.enterLocalPassiveMode();
            }
        }catch(Exception ex){
        	ex.printStackTrace();
        	result = false;
        }
    	
        return result;
    }
    
    public boolean checkFtpDirectoryExists(String rp) throws IOException 
    {
    	return ftpClient.changeWorkingDirectory(rp);
    }    
    
    public boolean createFtpDirectory(String rp) throws IOException
    {    	
    	return ftpClient.makeDirectory(rp);
    }

    public boolean createFtpDirectoryIfMissing(String rp) throws IOException
    {
    	boolean result = checkFtpDirectoryExists(rp);
    	if(!result){
    		result = createFtpDirectory(rp);
    	}
    	
    	return result;
    }
    
    /*
     * storeFile()
     * Uploads a local file to remote path on the ftp server
     * 
     */
    public boolean putFile(Path lp, String rp)
    {
    	boolean result = true;
        try
        {
        	File file = lp.toFile();
        	if(file.exists())
        	{
        		InputStream is = new FileInputStream(file);
        		if(ftpClient.storeFile(rp, is)){
        			is.close();
        			//System.out.println("Successfully uploaded file " + file.getAbsolutePath());
        		} else {
					if(ConfigurationHandler.verbose) {
						System.out.println("Error uploading file " + file.getAbsolutePath());
					}
        		}
        	} else{
				if(ConfigurationHandler.verbose) {
					System.out.println("File " + file.getAbsolutePath() + " not found!");
				}
        		result = false;
        	}
        	
        }catch(Exception ex)
        {
        	result = false;
        	ex.printStackTrace();
        }
        
        return result;
    }
    
    /*
     * getFile()
     * Gets data from a remote location on the ftp server(rp) to local path (lp) 
     * TOIMPLEMENT:
     */
    public boolean getFile(String lp, String rp)
    {
    	
    	return false;
    }
    
    /*
     * getFileList()
     * Returns the list of files under a remotepath(rp)
     * TOIMPLEMENT:
     */
    public List<String> getFileList(String rp)
    {
    	return null;
    }
    
    public void disconnect()
    {
    	if (ftpClient.isConnected())
        {
            try
            {
            	ftpClient.logout();
            	ftpClient.disconnect();
            	if(ConfigurationHandler.verbose){
            		System.out.println("Ftp is now disconnected.");
            	}
            }
            catch (IOException f){}
        }    	
    }
}