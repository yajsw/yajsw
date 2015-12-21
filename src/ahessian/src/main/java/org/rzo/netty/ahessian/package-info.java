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
/**
 * This framework provides support for <a
 href="http://hessian.caucho.com/">hessian</a>
 serialization and rpc by using <a href="http://jboss.org/netty/">netty</a>
 as framework for the transport layer.<br>
 Currently hessian2 rpc is supported.<br>
 <br>
 Dependencies:<br>
 <div style="margin-left: 40px;">
 <ul>
 <li>netty-3.2.0ALPHA2</li>
 <li>hessian-4.0.2</li>
 <li>servlet-api.jar (required by hessian, but not used)</li>
 </ul>
 </div>
 <br>
 Major features<br>
 <div style="margin-left: 40px;">
 <ul>
 <li>transport of hessian serialized objects</li>
 <li>synchronous &amp; asynchronous client RPC proxy</li>
 <li>single or multiple RPC execution threads per connection</li>
 <li>support for server side continuations allowing architecture
 similar to jetty continuations or servlet 3.0 suspend/resume. The
 client may thus receive multiple results with a single rpc invoke</li>
 <li>support for sessions allowing clients to invoke long
 running RPC requests or continuations, disconnect and then reconnect to
 get the results.</li>
 </ul>
 </div>
 <br>
 TODO <br>
 <div style="margin-left: 40px;">
 <ul>
 <li>session timeout</li>
 <li>invocation timeout</li>
 <li>options for storing invocation results on the server and referring to them within subsequent invocations</li>
 </ul>
 </div>
 <br><br>
 <br>
 */
package org.rzo.netty.ahessian;