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
package org.rzo.netty.ahessian.bootstrap;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.handler.ipfilter.IpFilterRuleHandler;
import io.netty.handler.ipfilter.IpFilterRuleList;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.rzo.netty.ahessian.auth.AuthToken;
import org.rzo.netty.ahessian.auth.AuthTokenList;
import org.rzo.netty.ahessian.auth.Base64AuthToken;
import org.rzo.netty.ahessian.auth.ClientAuthFilter;
import org.rzo.netty.ahessian.auth.EncryptedAuthToken;
import org.rzo.netty.ahessian.auth.ServerAuthFilter;
import org.rzo.netty.ahessian.auth.SimpleAuthToken;
import org.rzo.netty.ahessian.crypto.ClientCryptoData;
import org.rzo.netty.ahessian.crypto.ClientCryptoFilterInbound;
import org.rzo.netty.ahessian.crypto.ClientCryptoFilterOutbound;
import org.rzo.netty.ahessian.crypto.ServerCryptoData;
import org.rzo.netty.ahessian.crypto.ServerCryptoFilterInbound;
import org.rzo.netty.ahessian.heartbeat.ClientHeartbeatHandlerOutbound;
import org.rzo.netty.ahessian.heartbeat.HeartbeatHandlerInbound;
import org.rzo.netty.ahessian.heartbeat.ServerHeartbeatHandler;
import org.rzo.netty.ahessian.io.InputStreamHandler;
import org.rzo.netty.ahessian.io.OutputStreamHandler;
import org.rzo.netty.ahessian.io.PullInputStreamConsumer;
import org.rzo.netty.ahessian.rpc.client.BootstrapProvider;
import org.rzo.netty.ahessian.rpc.client.HessianProxyFactory;
import org.rzo.netty.ahessian.rpc.client.ReconnectHandler;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallDecoder;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallEncoder;
import org.rzo.netty.ahessian.rpc.message.HessianRPCReplyDecoder;
import org.rzo.netty.ahessian.rpc.message.HessianRPCReplyEncoder;
import org.rzo.netty.ahessian.rpc.server.ExecutorInvokeService;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler.ConnectListener;
import org.rzo.netty.ahessian.rpc.server.ImmediateInvokeService;
import org.rzo.netty.ahessian.session.ClientSessionFilter;
import org.rzo.netty.ahessian.session.ServerSessionFilter;
import org.rzo.netty.ahessian.stopable.StopHandler;

import com.caucho.hessian4.io.SerializerFactory;

public class ChannelPipelineFactoryBuilder<T> implements
		ChannelPipelineFactoryFactory
{
	enum Encryption
	{
		MD5, BASE64, NONE
	}

	long _reconnectTimeout = -1;
	private long _serverHeartbeatTimeout = -1;
	private long _clientHeartbeatTimeout = -1;
	private boolean _isClient = true;
	private long _sessionTimeout = -1;
	private String[] _passwords = null;
	private Encryption _passwordEncryption = null;
	private Object _serverService = null;
	private Class _serverServiceInterface = null;
	private String _ipFilter = null;
	private boolean _unique = false;
	private boolean _immediateInvoke;
	private boolean _executorInvoke;
	private int _executorThreads;
	private Map _serviceOptions;
	private static Timer TIMER = new HashedWheelTimer();
	private boolean _ssl = false;
	protected HessianRPCServiceHandler _serverFactory;
	protected HessianProxyFactory _clientFactory;
	private boolean _debug = false;
	private Executor _executor = Executors.newCachedThreadPool();
	private T _proxy;
	private SerializerFactory _serializerFactory;
	private boolean _inverseServer = false;
	private ConnectListener _connectedListener;
	private ConnectListener _disconnectedListener;

	public ChannelPipelineFactoryBuilder reconnect(long timeout)
	{
		_reconnectTimeout = timeout;
		return this;
	}

	public ChannelPipelineFactoryBuilder ssl()
	{
		throw new RuntimeException("not yet implemented");
		// return this;
	}

	public ChannelPipelineFactoryBuilder compression()
	{
		throw new RuntimeException("not yet implemented");
		// return this;
	}

	public ChannelPipelineFactoryBuilder connectedListener(
			ConnectListener listener)
	{
		_connectedListener = listener;
		return this;
	}

	public ChannelPipelineFactoryBuilder disconnectedListener(
			ConnectListener listener)
	{
		_disconnectedListener = listener;
		return this;
	}

	public ChannelPipelineFactoryBuilder serializerFactory(
			SerializerFactory serializerFactory)
	{
		_serializerFactory = serializerFactory;
		return this;
	}

	public ChannelPipelineFactoryBuilder inverseServer(boolean inverseServer)
	{
		_inverseServer = inverseServer;
		return this;
	}

	public ChannelPipelineFactoryBuilder serverHeartbeat(long timeout)
	{
		_serverHeartbeatTimeout = timeout;
		return this;
	}

	public ChannelPipelineFactoryBuilder clientHeartbeat(long timeout)
	{
		_clientHeartbeatTimeout = timeout;
		return this;
	}

	public ChannelPipelineFactoryBuilder sessionTimeout(long timeout)
	{
		_sessionTimeout = timeout;
		return this;
	}

	public ChannelPipelineFactoryBuilder password(String... pwd)
	{
		_passwords = pwd;
		return this;
	}

	public ChannelPipelineFactoryBuilder passwordEncryption(Encryption enc)
	{
		_passwordEncryption = enc;
		return this;
	}

	public ChannelPipelineFactoryBuilder rpcServerService(T service)
	{
		_serverService = service;
		return this;
	}

	public ChannelPipelineFactoryBuilder rpcServiceInterface(
			Class<T> interfaceClass)
	{
		if (!interfaceClass.isInterface())
			throw new RuntimeException("expecting an interface: "
					+ interfaceClass);

		_serverServiceInterface = interfaceClass;
		return this;
	}

	public ChannelPipelineFactoryBuilder ipFilter(String ipFilter)
	{
		_ipFilter = ipFilter;
		return this;
	}

	public ChannelPipelineFactoryBuilder single()
	{
		_unique = true;
		return this;
	}

	public ChannelPipelineFactoryBuilder serverSingleThreadService()
	{
		_immediateInvoke = true;
		return this;
	}

	public ChannelPipelineFactoryBuilder serviceThreads(int threads)
	{
		_executorInvoke = true;
		_executorThreads = threads;
		return this;
	}

	public ChannelPipelineFactoryBuilder serviceOptions(Map options)
	{
		_serviceOptions = options;
		return this;
	}

	public ChannelPipelineFactoryBuilder debug()
	{
		_debug = true;
		return this;
	}

	@Override
	public ChannelPipelineFactory create(EventExecutorGroup group,
			AbstractBootstrap bootstrap)
	{
		ChannelPipelineFactory result;
		if (isRPCServer() && isRPC() && _serverFactory == null)
		{
			_serverFactory = new HessianRPCServiceHandler(
					Executors.newCachedThreadPool(), _serviceOptions, TIMER);
			_serverFactory.setConnectListener(_connectedListener);
			_serverFactory.setDisconnectListener(_disconnectedListener);
			if (isExecutorService())
				_serverFactory
						.addService(
								"default",
								new ExecutorInvokeService(
										_serverService,
										_serverServiceInterface,
										_serverFactory,
										Executors
												.newFixedThreadPool(_executorThreads)));
			else
				_serverFactory.addService("default",
						new ImmediateInvokeService(_serverService,
								_serverServiceInterface, _serverFactory));
		}
		else if (isRPC() && _clientFactory == null)
		{
			Executor executor = _executorThreads > 0 ? Executors
					.newFixedThreadPool(_executorThreads) : Executors
					.newSingleThreadExecutor();
			String name = "NONAME?";
			_clientFactory = new HessianProxyFactory(executor, name,
					_serviceOptions);
		}

		if (hasSession())
		{
			result = basePipelineFactory(
					mixinPipelineFactory(group, bootstrap), group, bootstrap);
		}
		else
			result = mergePipelineFactory(
					basePipelineFactory(group, bootstrap),
					mixinPipelineFactory(group, bootstrap));

		if (_debug)
			result.debug();

		return result;
	}

	private boolean isExecutorService()
	{
		return _executorInvoke;
	}

	private ChannelPipelineFactory mergePipelineFactory(
			final ChannelPipelineFactory basePipelineFactory,
			final ChannelPipelineFactory mixinPipelineFactory)
	{
		return new ChannelPipelineFactory()
		{

			@Override
			public HandlerList getPipeline() throws Exception
			{
				HandlerList basePipeline = basePipelineFactory.getPipeline();
				HandlerList mixinPipeline = mixinPipelineFactory.getPipeline();
				basePipeline.addAll(mixinPipeline);
				return basePipeline;
			}

		};
	}

	private ChannelPipelineFactory basePipelineFactory(
			EventExecutorGroup group, AbstractBootstrap bootstrap)
	{
		return basePipelineFactory(null, group, bootstrap);
	}

	private ChannelPipelineFactory basePipelineFactory(
			ChannelPipelineFactory mixinPipelineFactory,
			EventExecutorGroup group, final AbstractBootstrap bootstrap)
	{
		final ClientSessionFilter clientSessionFilter = mixinPipelineFactory != null ? new ClientSessionFilter(
				mixinPipelineFactory) : null;
		final ServerSessionFilter serverSessionFilter = mixinPipelineFactory != null ? new ServerSessionFilter(
				mixinPipelineFactory, TIMER, _sessionTimeout) : null;

		ChannelPipelineFactory result = new ChannelPipelineFactory(group)
		{

			@Override
			public HandlerList getPipeline() throws Exception
			{
				HandlerList pipeline = new HandlerList();
				if (isRPCServer())
				{
					if (hasIpFilter())
					{
						IpFilterRuleList list = new IpFilterRuleList(_ipFilter);
						IpFilterRuleHandler ipfilter = new IpFilterRuleHandler(
								list);
						pipeline.addLast("ipfilter", ipfilter);
					}
					if (hasCrypto())
					{
						ServerCryptoData data = new ServerCryptoData();
						ServerCryptoFilterInbound cryptoFilterInbound = new ServerCryptoFilterInbound(
								data);
						ServerCryptoFilterInbound cryptoFilterOutbound = new ServerCryptoFilterInbound(
								data);
						if (_passwords != null && _passwords.length > 0)
							for (String password : _passwords)
							{
								cryptoFilterInbound.addPassword(password
										.getBytes());
							}
						pipeline.addLast("cryptoFilterIn", cryptoFilterInbound);
						pipeline.addLast("cryptoFilterOut",
								cryptoFilterOutbound);

					}
					else if (hasAuthentication())
					{
						List<AuthToken> tokens = new LinkedList<AuthToken>();
						for (String password : _passwords)
						{
							AuthToken token = null;
							if (_passwordEncryption.equals(Encryption.NONE))
								token = new SimpleAuthToken();
							else if (_passwordEncryption
									.equals(Encryption.BASE64))
								token = new Base64AuthToken("", _passwords[0]);
							else if (_passwordEncryption.equals(Encryption.MD5))
							{
								token = new EncryptedAuthToken();
								((EncryptedAuthToken) token)
										.setAlgorithm("MD5");
								((EncryptedAuthToken) token)
										.setPassword(_passwords[0]);
								((EncryptedAuthToken) token).setLength(20);
							}
							if (token != null)
								tokens.add(token);
						}
						AuthToken tokenList = AuthTokenList.fromList(tokens,
								_unique);
						ServerAuthFilter filter = new ServerAuthFilter(
								tokenList);
						pipeline.addLast("authentication", filter);
					}
					if (hasServerHeartbeat())
					{
						HeartbeatHandlerInbound inboundHandeler = new HeartbeatHandlerInbound(
								"client_heartbeat", TIMER,
								_clientHeartbeatTimeout);
						ServerHeartbeatHandler outboundHandeler = new ServerHeartbeatHandler(
								inboundHandeler);
						pipeline.addLast("server_heartbeat_in", inboundHandeler);
						pipeline.addLast("server_heartbeat_out",
								outboundHandeler);
					}
					if (hasClientHeartbeat())
					{
						HeartbeatHandlerInbound inboundHandeler = new HeartbeatHandlerInbound(
								"client_heartbeat", TIMER,
								_clientHeartbeatTimeout);
						ClientHeartbeatHandlerOutbound outboundHandeler = new ClientHeartbeatHandlerOutbound(
								inboundHandeler);
						pipeline.addLast("client_heartbeat_in", inboundHandeler);
						pipeline.addLast("client_heartbeat_out",
								outboundHandeler);
					}

					if (serverSessionFilter != null)
						pipeline.addLast("sessionFilter", serverSessionFilter);

				}
				else
				// client
				{
					if (hasReconnect())
					{
						pipeline.addLast("reconnect", new ReconnectHandler(
								new BootstrapProvider()
								{

									@Override
									public AbstractBootstrap getBootstrap()
									{
										return bootstrap;
									}

								}, _reconnectTimeout, TIMER));
					}
					if (hasCrypto())
					{
						ClientCryptoData data = new ClientCryptoData();
						ClientCryptoFilterInbound cryptoFilterIn = new ClientCryptoFilterInbound(
								data);
						ClientCryptoFilterOutbound cryptoFilterOut = new ClientCryptoFilterOutbound(
								data);
						if (_passwords != null && _passwords.length > 0)
							cryptoFilterIn
									.setPassword(_passwords[0].getBytes());
						pipeline.addLast("cryptoFilterIn", cryptoFilterIn);
						pipeline.addLast("cryptoFilterOut", cryptoFilterOut);

					}
					else if (hasAuthentication())
					{
						AuthToken token = null;
						if (_passwordEncryption.equals(Encryption.NONE))
							token = new SimpleAuthToken();
						else if (_passwordEncryption.equals(Encryption.BASE64))
							token = new Base64AuthToken("", _passwords[0]);
						else if (_passwordEncryption.equals(Encryption.MD5))
						{
							token = new EncryptedAuthToken();
							((EncryptedAuthToken) token).setAlgorithm("MD5");
							((EncryptedAuthToken) token)
									.setPassword(_passwords[0]);
							((EncryptedAuthToken) token).setLength(20);
						}
						if (token != null)
							pipeline.addLast("authentication",
									new ClientAuthFilter(token));
					}
					if (hasServerHeartbeat())
					{
						HeartbeatHandlerInbound inboundHandeler = new HeartbeatHandlerInbound(
								"client_heartbeat", TIMER,
								_clientHeartbeatTimeout);
						ServerHeartbeatHandler outboundHandeler = new ServerHeartbeatHandler(
								inboundHandeler);
						pipeline.addLast("server_heartbeat_in", inboundHandeler);
						pipeline.addLast("server_heartbeat_out",
								outboundHandeler);
					}
					if (hasClientHeartbeat())
					{
						HeartbeatHandlerInbound inboundHandeler = new HeartbeatHandlerInbound(
								"client_heartbeat", TIMER,
								_clientHeartbeatTimeout);
						ClientHeartbeatHandlerOutbound outboundHandeler = new ClientHeartbeatHandlerOutbound(
								inboundHandeler);
						pipeline.addLast("client_heartbeat_in", inboundHandeler);
						pipeline.addLast("client_heartbeat_out",
								outboundHandeler);
					}
					if (clientSessionFilter != null)
						pipeline.addLast("sessionFilter", clientSessionFilter);
				}

				return pipeline;

			}
		};

		return result;
	}

	protected boolean hasServerHeartbeat()
	{
		return _serverHeartbeatTimeout > 0;
	}

	protected boolean hasClientHeartbeat()
	{
		return _clientHeartbeatTimeout > 0;
	}

	protected boolean hasCrypto()
	{
		// TODO Auto-generated method stub
		return false;
	}

	protected boolean hasAuthentication()
	{
		return _passwords != null && _passwords.length > 0
				&& _passwordEncryption != null;
	}

	protected boolean hasReconnect()
	{
		return _reconnectTimeout > 0;
	}

	protected boolean hasIpFilter()
	{
		return _ipFilter != null && _ipFilter.length() != 0;
	}

	private ChannelPipelineFactory mixinPipelineFactory(
			EventExecutorGroup group, AbstractBootstrap bootstrap)
	{
		ChannelPipelineFactory result = new ChannelPipelineFactory(group)
		{

			@Override
			public HandlerList getPipeline() throws Exception
			{
				HandlerList pipeline = new HandlerList();
				if (isRPC())
				{
					// EventExecutor loop = getGroup().next();
					// ChannelHandlerInvoker invoker = new
					// DirectWriteChannelHandlerInvoker(loop);
					if (isRPCServer())
					{
						// pipeline.addLast("xlogger-inputStream",new
						// OutLogger("inputStream"));
						pipeline.addLast("inputStream",
								new InputStreamHandler());
						// pipeline.addLast("xlogger-outputStream",new
						// OutLogger("outputStream"));
						pipeline.addLast("outputStream",
								new OutputStreamHandler(), getGroup());
						// pipeline.addLast("xlogger-callDecoder",new
						// OutLogger("callDecoder"), getGroup());
						pipeline.addLast("callDecoder",
								new PullInputStreamConsumer(
										new HessianRPCCallDecoder(
												_serializerFactory)),
								getGroup());
						// pipeline.addLast("xlogger-replyEncoder",new
						// OutLogger("replyEncoder"),getGroup());
						pipeline.addLast("replyEncoder",
								new HessianRPCReplyEncoder(_serializerFactory,
										_executor), getGroup());
						// pipeline.addLast("xlogger-hessianRPCServer",new
						// OutLogger("hessianRPCServer"), getGroup());
						pipeline.addLast("hessianRPCServer", _serverFactory,
								getGroup());
						// pipeline.addLast("xlogger-stop",new
						// OutLogger("stop"), getGroup());
						pipeline.addLast("stop", new StopHandler(), getGroup());
					}
					else
					{
						_clientFactory.setConnectedListener(_connectedListener);
						_clientFactory
								.setDisconnectedListener(_disconnectedListener);

						// pipeline.addLast("xlogger-inputStream",new
						// OutLogger("inputStream"));
						pipeline.addLast("inputStream",
								new InputStreamHandler());
						// pipeline.addLast("xlogger-outputStream",new
						// OutLogger("outputStream"), getGroup());
						pipeline.addLast("outputStream",
								new OutputStreamHandler(), getGroup());
						// pipeline.addLast("xlogger-replyDecoder",new
						// OutLogger("replyDecoder"), getGroup());
						pipeline.addLast("replyDecoder",
								new PullInputStreamConsumer(
										new HessianRPCReplyDecoder(
												_clientFactory,
												_serializerFactory)),
								getGroup());
						// pipeline.addLast("xlogger-callEncoder",new
						// OutLogger("callEncoder"), getGroup());
						pipeline.addLast("callEncoder",
								new HessianRPCCallEncoder(_inverseServer,
										_serializerFactory, _executor),
								getGroup());
						// pipeline.addLast("xlogger-hessianRPCClient",new
						// OutLogger("hessianRPCClient"), getGroup());
						pipeline.addLast("hessianRPCClient", _clientFactory,
								getGroup());
						// pipeline.addLast("xlogger-stop",new
						// OutLogger("stop"), getGroup());
						pipeline.addLast("stop", new StopHandler(), getGroup());
					}

				}
				return pipeline;
			}

		};
		return result;
	}

	private boolean hasSession()
	{
		return _sessionTimeout > 0;
	}

	private boolean isRPC()
	{
		return _serverServiceInterface != null;
	}

	private boolean isRPCServer()
	{
		return _serverService != null;
	}

	public HessianProxyFactory proxyFactory()
	{
		return _clientFactory;
	}

	public T proxy()
	{
		if (_proxy == null)
		{
			if (_clientFactory == null)
				throw new RuntimeException(
						"client factory is null. Note: proxy() can only invoked on clients");
			_proxy = (T) _clientFactory.create(_serverServiceInterface, this
					.getClass().getClassLoader(), _serviceOptions);
		}
		return _proxy;
	}

	public void close()
	{
		if (_clientFactory == null)
			return;
		_clientFactory.setBlocked(true);
		_clientFactory.invalidateAllPendingCalls();
		_clientFactory.invalidateProxies();
		_proxy = null;

	}

	public void unblock()
	{
		if (_clientFactory == null)
			return;
		_clientFactory.setBlocked(false);
	}
}
