package org.rzo.cloud.vfs.dropbox.auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticationData.Type;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.apache.commons.vfs2.UserAuthenticator;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

public class DropboxAuthenticator  implements UserAuthenticator
{
	
	private final String _appFile;
	private final String _tokenFile;
	
	
	public DropboxAuthenticator(String appFile, String tokenFile)
	{
		_appFile = appFile;
		_tokenFile = tokenFile;
	}

	@Override
	public UserAuthenticationData requestAuthentication(UserAuthenticationData.Type[] paramArrayOfType)
	{
		try
		{
			return requestAuthenticationInternal();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private UserAuthenticationData requestAuthenticationInternal() throws Exception
	{
		
		File tokenFile = new File(_tokenFile);
		File appFile = new File(_appFile);
        DbxRequestConfig config = new DbxRequestConfig(
                "DbxVfs/00.01", Locale.getDefault().toString());

		DbxAuthInfo authInfo = null;
		if (tokenFile.exists())
		try
		{
			authInfo = DbxAuthInfo.Reader.readFromFile(tokenFile);
			if (!checkToken(authInfo, config))
				authInfo = null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (authInfo == null)
		{
        DbxAppInfo appInfo =  DbxAppInfo.Reader.readFromFile(appFile);

        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        String authorizeUrl = webAuth.start();
        System.out.println("1. Go to: " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first)");
        System.out.println("3. Copy the authorization code.");
        String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
        DbxAuthFinish authFinish = webAuth.finish(code);
        // Save auth information to output file.
        authInfo = new DbxAuthInfo(authFinish.accessToken, DbxHost.Default);
        try {
            DbxAuthInfo.Writer.writeToFile(authInfo, tokenFile);
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
		}

        UserAuthenticationData result = new TokenData();
        result.setData(TokenData.TOKEN, UserAuthenticatorUtils.toChar(authInfo.accessToken));
        return result;
	}

	private boolean checkToken(DbxAuthInfo authInfo, DbxRequestConfig config)
	{
		try
		{
        DbxClient client = new DbxClient(config, authInfo.accessToken);
        return (client.getAccountInfo() != null);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

}
