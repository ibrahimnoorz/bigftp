# bigftp
BigFtp allows bulk transmission of files to an ftp server via FTP or FTPS using queues and workers.

# Motivation
I needed a tool to do bulk tranmission of files to an ftp server securely via FTPS. The tool can be calibrated according to resources available i.e. number of workers and bandwidth.

# Installation / Setup
BigFtp project is written in eclipse IDE. Please follow the steps below to setup the project:

 1. Get the bigftp repo from github.com
 
 2. Start eclipse and setup a new workspace in the parent folder where you have copied BigFtp
 
 3. From file menu choose Import->General->"Existing Projects into Workspace" and select BigFtp folder using Browse
 
 4. Click finish when you see the BigFtp project selected in the Projects area.
 
 5. To create a ready to run application (Runnable jar) use the following steps:
    
    5a. Select the workspace and File->Export->Java->Runnable JAR File
    
    5b. Select the Launch Configuration (If the dropdown is empty, Run the application at least once before this step.)
    
    5c. Select the location to place the jar file and complete the wizard instructions.

# Usage
  BigFtp uses the bigftp.properties file for processing a request. 
  Update the bigftp.properties file with your ftp settings and place the file where the runnable jar is sitting.
  
  Run the application using:
    
    java -jar bigftp.jar
  
  
# Todo
The following are some items that may be added in future updates:
  - Add support for ftp features i.e. bulk download, delete, list etc.
  - Test with more ftp servers (current testing done on Filezilla server).
  
# License
This project is licensed under the Apache 2.0 license. Please read the LICENSE file.
