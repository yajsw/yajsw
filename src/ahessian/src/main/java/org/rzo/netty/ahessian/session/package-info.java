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
 * Provides Client and Server Session handling
 * <br>
 * When using {@link ChannelPipelineFactory} netty creates a pipeline per connection
 * With sessions we need a pipeline per session. Thus a pipeline may survive multiple connect/disconnect cycles.
 * Sessions thus allows the server to maintain its state in-between disconnect/connect cycles of the client
 * Currently only in-memory sessions are implemented.
 * Therefore when the server is stopped all sessions are lost and reconnecting clients will have to
 * adjust accordingly.
 * 
 * To allow pipeline survival {@link MixinPipeline} is used.
 * On creation of a new session a new {@link MixinPipeline} is created, added to the current pipeline and associated with the session
 * On reconnect, once the session has been identified, the associated {@link MixinPipeline} is added to the
 * current pipeline.
 * 
 * TODO
 * persistent sessions -> handler/pipeline api will have to be extended for persistence
 * distributed sessions using jgroups
 * session timeout -> handlers/pipeline api will have to be extended for cleanup
 */
package org.rzo.netty.ahessian.session;