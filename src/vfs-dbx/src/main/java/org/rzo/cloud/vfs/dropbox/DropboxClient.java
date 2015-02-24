/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rzo.cloud.vfs.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileSystemException;

import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;

/**
 * What VFS expects from an ftp client to provide.
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public interface DropboxClient
{
    boolean isConnected() throws FileSystemException, DbxException;

    void disconnect() throws IOException;

    DbxEntry[] listFiles(String relPath) throws IOException;

    boolean removeDirectory(String relPath) throws IOException;

    boolean deleteFile(String relPath) throws IOException;

    boolean rename(String oldName, String newName) throws IOException;

    boolean makeDirectory(String relPath) throws IOException;

    InputStream retrieveFileStream(String relPath) throws IOException;

    OutputStream storeFileStream(String relPath) throws IOException;

    boolean abort() throws IOException;

}
