package org.rzo.cloud.vfs.dropbox.auth;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.rzo.cloud.vfs.dropbox.DropboxFileProvider;

public class CreateToken
{

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		//((DefaultFileSystemManager)VFS.getManager()).addProvider("dbx", new DropboxFileProvider());
		UserAuthenticator auth = new DropboxAuthenticator("app.auth", "token.auth");
		FileSystemOptions opts = new FileSystemOptions(); 
		DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth); 
		FileObject fo = VFS.getManager().resolveFile("dbx:/", opts);
	}

}
