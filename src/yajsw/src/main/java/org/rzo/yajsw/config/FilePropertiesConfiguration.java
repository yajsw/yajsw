package org.rzo.yajsw.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocator.FileLocatorBuilder;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.rzo.yajsw.util.VFSUtils;

public class FilePropertiesConfiguration extends PropertiesConfiguration
{
	String _fileName;
	File _url;
	FileSystem _fileSystem;
	String _encoding;
	
	 public class MyFileLocationStrategy implements FileLocationStrategy{

		@Override
		public URL locate(FileSystem fileSystem, FileLocator locator) {
			try {
				FileObject file = VFSUtils.resolveFile(_fileName);
				String base = VFSUtils.isLocal(file) ? new File(_fileName).getParent() : file.getParent().getURL().toString();
				return VFSUtils.resolveFile(base, locator.getFileName()).getURL();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}


	public FilePropertiesConfiguration(File file) throws Exception
	{
		_fileName = file.getAbsolutePath();
		init();
		load();
	}

	public FilePropertiesConfiguration()
	{
		init();
	}
	
	private void init()
	{
		this.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		FileLocationStrategy strategy = new MyFileLocationStrategy();
		String basePath = _fileName == null ? "." : new File(_fileName).getParent();
		FileLocator locator = FileLocatorUtils.fileLocator()
				.locationStrategy(strategy)
				.basePath(basePath)
				.create();
		this.initFileLocator(locator);
	}
	
	public String getPath()
	{
		return _fileName;
	}

	public String getFileName()
	{
		//return _fileName;
		try {
			return VFSUtils.resolveFile(".", _fileName).getName().getBaseName();
		} catch (FileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _fileName;
	}

	public String getURL() throws Exception
	{
		return _fileName;
	}

	public FileSystem getFileSystem()
	{
		return _fileSystem;
	}

	public void setFileName(String fileName)
	{
		_fileName = fileName;
	}

	public void load() throws Exception
	{
		Reader reader = null;
		try
		{
		FileObject f = VFSUtils.resolveFile(".", _fileName);
		InputStream in = f.getContent().getInputStream();
		reader = new InputStreamReader(in);
		this.read(reader);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (reader != null)
				reader.close();
		}
	}

	public void setEncoding(String encoding)
	{
		_encoding = encoding;
	}

	public void reload() throws Exception
	{
		super.clear();
		load();
	}

	public FilePropertiesConfiguration interpolatedFileConfiguration()
	{
		FilePropertiesConfiguration result = new FilePropertiesConfiguration();
		result.copy(interpolatedConfiguration());
		result.setFileName(_fileName);
		return result;
	}

	public void save(File cn) throws Exception
	{
		Writer writer = new FileWriter(cn);
		write(writer);
		writer.close();
	}

}
