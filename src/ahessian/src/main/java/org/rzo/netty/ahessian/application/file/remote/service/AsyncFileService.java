package org.rzo.netty.ahessian.application.file.remote.service;

import java.io.InputStream;
import java.util.List;

public interface AsyncFileService
{
	public FileObject getFile(String file);
	public List<FileObject> list(String filter);
	public InputStream getInputStream(String file);
	
	
}
