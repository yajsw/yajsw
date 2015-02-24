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
package org.rzo.cloud.vfs.dropbox.auth;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.vfs2.UserAuthenticationData;

/**
 * Container for various authentication data.
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class AppData extends UserAuthenticationData
{
    public static final Type APPKEY = new Type("appkey");
    public static final Type APPSECRET = new Type("appsecret");
    
    
    

    public AppData(String appKey, String appSecret)
    {
    	
    }

}
