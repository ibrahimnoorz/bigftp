package com.ibt.bigftp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FtpHandler {
	public boolean connect(String host, int port);
	public boolean login(String user, String pass);
	public boolean checkFtpDirectoryExists(String rp) throws IOException;
	public boolean createFtpDirectory(String rp) throws IOException;
	public boolean createFtpDirectoryIfMissing(String rp) throws IOException;
	public boolean putFile(Path lp, String rp);
	public boolean getFile(String lp, String rp);
	public List<String> getFileList(String rp);
	public void disconnect();
}
