/*******************************************************************************
 * Copyright  2015 rzorzorzo@users.sf.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.rzo.yajsw.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.security.KeyStore;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class MyKeyStore implements MyKeyStoreInterface
{
	final private KeyStore ks;
	final private AtomicReference<FileLock> lock = new AtomicReference<FileLock>();
	final private AtomicReference<FileChannel> channel = new AtomicReference<FileChannel>();
	private String file;
	final private char[] pwd;

	public MyKeyStore(String file, char[] pwd) throws Exception
	{
		ks = KeyStore.getInstance("JCEKS");
		this.file = file;
		this.pwd = pwd;
	}

	public void init() throws Exception
	{
		File f = new File(file);
		if (!f.exists())
			createStore();
		else
			readStore();
		lockFile();
	}

	public void start() throws Exception
	{
		String newFile = file + ".xxx";
		if (!new File(file).exists() && !new File(newFile).exists())
			throw new RuntimeException("keystore not found: "
					+ new File(file).getAbsolutePath());

		if (!new File(newFile).exists())
		{
			new File(file).renameTo(new File(newFile));
			file = newFile;
			restrictAccess();
		}
		file = newFile;

		if (!new File(file).exists())
			throw new RuntimeException("keystore not found: "
					+ new File(file).getAbsolutePath());

		init();
	}

	public MyKeyStore() throws Exception
	{
		this("keystore", MyKeyStore.class.getName().toCharArray());
	}

	public MyKeyStore(String file) throws Exception
	{
		this(file, MyKeyStore.class.getName().toCharArray());
	}

	private void lockFile() throws Exception
	{
		channel.set(new RandomAccessFile(file, "rw").getChannel());
		lock.set(channel.get().lock());
	}

	private void readStore() throws Exception
	{
		FileInputStream fIn = new FileInputStream(file);
		ks.load(fIn, pwd);
		fIn.close();
	}

	public void put(String key, char[] text) throws Exception
	{
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
		SecretKey generatedSecret = factory
				.generateSecret(new PBEKeySpec(text));

		KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(
				pwd);

		ks.setEntry(key, new KeyStore.SecretKeyEntry(generatedSecret),
				keyStorePP);

		OutputStream out = Channels.newOutputStream(channel.get());

		// store locks the channel -> reopen after we are done
		ks.store(out, pwd);
		lockFile();

	}

	public char[] get(String key) throws Exception
	{
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
		KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(
				pwd);
		KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry) ks.getEntry(
				key, keyStorePP);

		PBEKeySpec keySpec = (PBEKeySpec) factory.getKeySpec(
				ske.getSecretKey(), PBEKeySpec.class);

		return keySpec.getPassword();

	}

	private void createStore() throws Exception
	{
		ks.load(null, pwd);
		new File(file).createNewFile();
		FileOutputStream fos = new java.io.FileOutputStream(file);
		ks.store(fos, pwd);
		fos.close();
	}

	private void restrictAccess() throws Exception
	{

		UserPrincipal user = getDefaultFileOwner();

		Path path = Paths.get(file);
		AclFileAttributeView aclAttr = Files.getFileAttributeView(path,
				AclFileAttributeView.class);

		System.out.println("keystore owner: " + aclAttr.getOwner());

		UserPrincipalLookupService upls = path.getFileSystem()
				.getUserPrincipalLookupService();
		// UserPrincipal user = upls.lookupPrincipalByName(System
		// .getProperty("user.name"));//
		// aclAttr.getOwner();//upls.lookupPrincipalByName("OWNER@");//System.getProperty("user.name"));
		AclEntry.Builder builder = AclEntry.newBuilder();
		builder.setPermissions(EnumSet.of(AclEntryPermission.READ_DATA,
				AclEntryPermission.WRITE_DATA, AclEntryPermission.APPEND_DATA,
				AclEntryPermission.READ_NAMED_ATTRS,
				AclEntryPermission.WRITE_NAMED_ATTRS,
				AclEntryPermission.READ_ATTRIBUTES,
				AclEntryPermission.WRITE_ATTRIBUTES,
				AclEntryPermission.READ_ACL, AclEntryPermission.SYNCHRONIZE));

		builder.setPrincipal(user);
		builder.setType(AclEntryType.ALLOW);
		aclAttr.setOwner(user);
		aclAttr.setAcl(Collections.singletonList(builder.build()));
	}

	private UserPrincipal getDefaultFileOwner() throws Exception
	{
		File tmp = new File("x.tmp");
		if (tmp.exists())
			tmp.delete();
		tmp.createNewFile();
		if (!tmp.exists())
			System.out.println("keystore error: cannot create "+tmp);
		Path path = Paths.get("x.tmp");
		AclFileAttributeView aclAttr = Files.getFileAttributeView(path,
				AclFileAttributeView.class);
		UserPrincipal result = aclAttr.getOwner();
		new File("x.tmp").delete();
		// System.out.println("owner: "+aclAttr.getOwner());

		return result;
	}

	public static void main(String[] args) throws Exception
	{
		MyKeyStore ks = new MyKeyStore();//new MyKeyStore("mykeystore2.dat", "test".toCharArray());
		ks.start();
		while (true)
		{
			 /*
			  ks.put("pwd1",
			 ("gehjeim-"+System.currentTimeMillis()).toCharArray());
			 ks.put("pwd2",
			 ("gehjeim2-"+System.currentTimeMillis()).toCharArray());
			 */
			System.out.println(new String(ks.get("pwd1")));
			System.out.println(new String(ks.get("pwd2")));
			Thread.sleep(1000);
		}
	}

	@Override
	public String getFile()
	{
		return new File(file).getAbsolutePath();
	}

}
