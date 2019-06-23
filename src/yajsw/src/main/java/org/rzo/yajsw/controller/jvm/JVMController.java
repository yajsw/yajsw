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

package org.rzo.yajsw.controller.jvm;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.rzo.yajsw.Constants;
import org.rzo.yajsw.controller.AbstractController;
import org.rzo.yajsw.controller.Message;
import org.rzo.yajsw.util.Cycler;
import org.rzo.yajsw.util.DaemonThreadFactory;
import org.rzo.yajsw.util.Utils;
import org.rzo.yajsw.wrapper.WrappedJavaProcess;
import org.rzo.yajsw.wrapper.WrappedProcess;

// TODO: Auto-generated Javadoc
/**
 * The Class Controller.
 */
public class JVMController extends AbstractController
{

	/** The Constant STATE_UNKNOWN. */
	static final int STATE_UNKNOWN = 0;

	/** The Constant STATE_WAITING. */
	static final int STATE_WAITING = 1;

	/** The Constant STATE_ESTABLISHED. */
	static final int STATE_ESTABLISHED = 2;

	/** The Constant STATE_LOGGED_ON. */
	static final int STATE_LOGGED_ON = 3;

	/** The Constant STATE_STARTUP_TIMEOUT. */
	public static final int STATE_STARTUP_TIMEOUT = 4;

	/** The Constant STATE_WAITING_CLOSED. */
	public static final int STATE_WAITING_CLOSED = 5;

	/** The Constant STATE_USER_STOP. */
	public static final int STATE_USER_STOP = 6;

	public static final int STATE_PING_TIMEOUT = 7;

	public static final int STATE_PROCESS_KILLED = 8;

	public static final int STATE_THRESHOLD = 9;

	/** The _port. */
	int _port = DEFAULT_PORT;

	int _minPort = DEFAULT_PORT;

	int _maxPort = 65535;

	/** The _startup timeout. */
	int _startupTimeout = DEFAULT_STARTUP_TIMEOUT * 1000;

	/** The _key. */
	String _key;

	/** The _ping timeout. */
	int _pingTimeout = 10;

	volatile boolean _pingOK = false;

	static Executor _pingExecutor = Executors
			.newCachedThreadPool(new DaemonThreadFactory("pinger"));

	/** The _session. */
	// IoSession _session;
	final AtomicReference<Channel> _channel = new AtomicReference<Channel>();

	/** The Constant pool. */
	// static final SimpleIoProcessorPool pool = new
	// SimpleIoProcessorPool(NioProcessor.class);
	/**
	 * To avoid tcp handle leak: Destroy the acceptor at stop and create new one
	 * on each start.
	 * 
	 * The _acceptor.
	 */
	// NioSocketAcceptor _acceptor = null;
	ServerBootstrap _acceptor = null;
	volatile Channel _parentChannel;

	/** The _init. */
	boolean _init = false;

	/** The Constant _usedPorts. */
	static final Set _usedPorts = Collections.synchronizedSet(new TreeSet());

	/** The Constant _scheduler. */
	static private final ScheduledThreadPoolExecutor _scheduler = (ScheduledThreadPoolExecutor) Executors
			.newScheduledThreadPool(1, new DaemonThreadFactory(
					"controller.scheduler"));

	/** The _timeout handle. */
	volatile ScheduledFuture<?> _timeoutHandle;

	Cycler _pingCheck;
	// ExecutorService workerExecutor = Executors.newCachedThreadPool(new
	// DaemonThreadFactory(
	// "controller-worker"));

	Runnable _serviceStartupListener;

	float _heap = -1;
	long _minGC = -1;
	long _fullGC = -1;
	long _heapInBytes = -1;
	EventLoopGroup _bossGroup;
	EventLoopGroup _workerGroup;
	volatile boolean _bound = false;

	/**
	 * Instantiates a new controller.
	 * 
	 * @param wrappedJavaProcess
	 *            the wrapped java process
	 */
	public JVMController(WrappedProcess wrappedJavaProcess)
	{
		super(wrappedJavaProcess);
		_bossGroup = new OioEventLoopGroup();
		_workerGroup = new OioEventLoopGroup();
		ControllerPipelineFactory pipelineFactory = new ControllerPipelineFactory(
				this);

		setDebug(((WrappedJavaProcess)wrappedJavaProcess).getDebug());
		pipelineFactory.setDebug(_debug > 2);
		_acceptor = new ServerBootstrap().group(_bossGroup, _workerGroup)
				.channel(OioServerSocketChannel.class)
				.childOption(ChannelOption.TCP_NODELAY, true)
				// .option(ChannelOption.SO_BACKLOG, 128)
				.childHandler(pipelineFactory);

	}

	public void init()
	{
		if (_pingCheck == null)
			_pingCheck = new Cycler(_pingTimeout, _pingTimeout, _pingExecutor,
					new Runnable()
					{
						int r = 2;

						public void run()
						{
							if (!_pingOK)
							{
								getLog().info(
										"Missing wrapper ping within timeout of "
												+ _pingTimeout);
								// stop the process in a separate thread,
								// otherwise
								// conflict
								executor.execute(new Runnable()
								{
									public void run()
									{
										stop(STATE_PING_TIMEOUT, "PING_TIMEOUT");
									}
								});
							}
							else
								_pingOK = false;
						}
					});
	}

	/**
	 * Inits the.
	 */
	private void initInternal()
	{

		// ???do not allow multiple servers to bind on the same port
		_init = true;

	}

	/**
	 * Start.
	 */
	public boolean start()
	{
		if (_bound)
		{
			setState(STATE_WAITING);
			return true;
		}
		
		int myPort = -1;
		InetAddress address = null;
		try
		{
			address = Utils.getLoopbackAddress();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		// in case of wrapper chaining: if we already have opened a port to our
		// wrapper: do not use this port for a sub-process
		try
		{
			myPort = Integer.parseInt((String) System.getProperties().get(
					"wrapper.port"));
		}
		catch (Exception e)
		{
		}
		if (myPort != -1)
			_usedPorts.add(myPort);

		try
		{
			initInternal();
			setState(STATE_UNKNOWN);
			// if we have kept the channel
			if (_parentChannel != null && _parentChannel.isActive()) // ??
			{
				setState(STATE_WAITING);
				// beginWaitForStartup();
				if (getDebug() > 2)
					getLog().info("binding successfull");
				return true;
			}
			_port = _minPort;
			while (getState() < STATE_WAITING && _port <= _maxPort)
			{
				if (_usedPorts.contains(_port))
					_port++;
				else
					try
					{
						_usedPorts.add(_port);
						if (getDebug() > 2)
							getLog().info("binding to port " + _port);
						ChannelFuture f = _acceptor.bind(address, _port).sync();
						if (f.isSuccess())
						{
							_parentChannel = f.channel();
							setState(STATE_WAITING);
							_bound = true;
							// beginWaitForStartup();
							if (getDebug() > 2)
								getLog().info("binding successfull");
							return true;
						}
					}
					catch (Exception ex)
					{
						if (_debug > 2)
							getLog().info(
									"binding error: " + ex.getMessage()
											+ " -> retry with another port");

						_usedPorts.remove(_port);
						try
						{
							Thread.sleep(500);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
							getLog().info(
									"sleep interrupted in JVMcontroller start");
							Thread.currentThread().interrupt();
							return false;
						}
						_port++;
					}
			}
			getLog().severe(
					"could not find a free port in the range " + _minPort
							+ "..." + _maxPort);
			return false;
		}
		catch (Exception ex)
		{
			getLog().severe("JVMController start " + ex);
			return false;
		}
	}

	/**
	 * Begin wait for startup.
	 */
	public void beginWaitForStartup()
	{
		if (_startupTimeout <= 0)
			return;
		final Runnable timeOutAction = new Runnable()
		{
			int r = 1;

			public void run()
			{
				if (getDebug() > 1)
					getLog().severe(
							"WrapperManger did not log on within timeout of "
									+ _startupTimeout);
				stop(STATE_STARTUP_TIMEOUT, "STARTUP_TIMEOUT");
			}
		};
		_timeoutHandle = _scheduler.schedule(timeOutAction, _startupTimeout,
				TimeUnit.MILLISECONDS);
	}

	void schedulePingCheck()
	{
		_pingOK = false;
		_pingCheck.start();
	}

	void stopPingCheck()
	{
		if (_pingCheck != null)
			_pingCheck.stop();
	}

	void pingReceived()
	{
		_pingOK = true;
		if (_channel.get() != null)
			_channel.get().writeAndFlush(
					new Message(Constants.WRAPPER_MSG_PING, null));

	}

	void serviceStartup()
	{
		_wrappedProcess.setAppReportedReady(true);
		if (_serviceStartupListener != null)
			_serviceStartupListener.run();
		else
			getLog().info("cannot report service startup: listener is null");
	}

	/**
	 * Stop.
	 * 
	 * @param state
	 *            the state
	 */
	public void stop(int state, String reason)
	{
		stopPingCheck();
		if (_timeoutHandle != null)
			_timeoutHandle.cancel(true);
		_scheduler.purge();
		setState(state);
		if (_parentChannel != null)
		{
			int i = 0;
			while (_channel.get() != null && _channel.get().isActive() && i < 3)
			{
				i++;
				if (_debug > 1)
					getLog().info("controller sending a stop command");
				if (_channel.get() != null)
				{
					String txt = null;
					if (reason != null && reason.length() > 0)
						txt = ":" + reason;
					Future future = _channel.get().writeAndFlush(
							new Message(Constants.WRAPPER_MSG_STOP, txt));
					try {
						future.await(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try
				{
					Thread.sleep(500);
				}
				catch (Exception ex)
				{
				}
			}
			// /* if we close the channel here, the app cannot send
			// signalStop(time) messages
			closeChannel();
			// */

			// undind and dispose all channels and ports.
			// keep the same port until we shut down
			/*
			 * log.info("unbind session"); if (_parentChannel != null &&
			 * _parentChannel.isBound()) { try {
			 * _parentChannel.unbind().await(1000); } catch
			 * (InterruptedException e) { e.printStackTrace(); } _parentChannel
			 * = null; //_acceptor.releaseExternalResources(); }
			 * _usedPorts.remove(_port);
			 */
		}
	}

	/**
	 * Startup ok.
	 */
	void startupOK()
	{
		if (_timeoutHandle == null)
			return;
		_timeoutHandle.cancel(false);
		schedulePingCheck();
		_timeoutHandle = null;
	}

	// test
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args)
	{
		JVMController c = new JVMController(null);
		c.setDebug(3);
		c.setKey("123");
		c.setMinPort(15003);
		c.start();
		c.stop(0, null);
		JVMController c1 = new JVMController(null);
		c1.setDebug(3);
		c1.setKey("123");
		c1.start();

	}

	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public int getPort()
	{
		return _port;
	}

	/**
	 * Sets the port.
	 * 
	 * @param port
	 *            the new port
	 */
	public void setMinPort(int port)
	{
		if (port > 0 && port < 65536)
			_minPort = port;
		else
			getLog().info("port out of range " + port);
	}

	public void setMaxPort(int port)
	{
		if (port > 0 && port < 65536 && port >= _minPort)
			_maxPort = port;
		else
			getLog().info("port out of range " + port);
	}

	public void setPort(int port)
	{
		_port = port;
	}

	/**
	 * Checks if is debug.
	 * 
	 * @return true, if is debug
	 */
	int getDebug()
	{
		return _debug;
	}

	public void setDebugComm(boolean debug)
	{
		_debugComm = debug;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public String getKey()
	{
		return _key;
	}

	/**
	 * Sets the key.
	 * 
	 * @param key
	 *            the new key
	 */
	public void setKey(String key)
	{
		_key = key;
	}

	/**
	 * Gets the startup timeout.
	 * 
	 * @return the startup timeout
	 */
	int getStartupTimeout()
	{
		return _startupTimeout;
	}

	/**
	 * Sets the startup timeout.
	 * 
	 * @param startupTimeout
	 *            the new startup timeout
	 */
	public void setStartupTimeout(int startupTimeout)
	{
		_startupTimeout = startupTimeout;
	}

	/**
	 * Gets the ping timeout.
	 * 
	 * @return the ping timeout
	 */
	int getPingTimeout()
	{
		return _pingTimeout;
	}

	/**
	 * Sets the ping timeout.
	 * 
	 * @param pingTimeout
	 *            the new ping timeout
	 */
	public void setPingTimeout(int pingTimeout)
	{
		_pingTimeout = pingTimeout;
	}

	/**
	 * Wait for.
	 * 
	 * @return true, if successful
	 */
	public boolean waitFor(long timeout)
	{
		long end = System.currentTimeMillis() + timeout;
		while (true)
		{
			if (_state == STATE_LOGGED_ON)
				return true;
			else if (_state == STATE_STARTUP_TIMEOUT)
				return false;
			else if (System.currentTimeMillis() > end)
				return false;
			try
			{
				Thread.sleep(250);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				getLog().info("sleep interrupted in JVMController.waitfor");
				return false;
			}
		}
	}

	/**
	 * Request thread dump.
	 */
	public void requestThreadDump()
	{
		if (_channel.get() != null)
			_channel.get().writeAndFlush(
					new Message(Constants.WRAPPER_MSG_THREAD_DUMP, null));
	}

	/**
	 * Request thread dump.
	 */
	public void requestGc()
	{
		if (_channel.get() != null)
			_channel.get().writeAndFlush(
					new Message(Constants.WRAPPER_MSG_GC, null));
	}

	/**
	 * Request thread dump.
	 */
	public void requestDumpHeap(String fileName)
	{
		if (_channel.get() != null)
			_channel.get().writeAndFlush(
					new Message(Constants.WRAPPER_MSG_DUMP_HEAP, fileName));
	}

	public void reset()
	{
		stop(JVMController.STATE_UNKNOWN, "RESTART");
		_heap = -1;
		_minGC = -1;
		_fullGC = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() throws Throwable
	{
		try
		{
			reset();
		}
		finally
		{
			super.finalize();
		}
	}

	private volatile boolean _waitingForProcessTermination = false;

	private float _maxHeapRestart = -1;

	private long _maxFullGCTimeRestart = -1;

	// invoked when the process has been spawned
	// so we can wait for it to terminate
	public void processStarted()
	{
		int waitCounter = 0;
		// it may, but should not happen, that
		// this method is called twice although this should not happen
		// we log this and after some time if we do not return from
		// osProcess.waitFor(); we restart the application.
		// TODO
		while (_waitingForProcessTermination)
			try
			{
				getLog().info(
						"should not happen: waiting for termination thread");
				if (waitCounter < 100)
				{
					getLog().info(
							"should not happen: waiting for termination thread");
				}

				Thread.sleep(200);
				waitCounter++;

				if (waitCounter == 1000)
				{
					executor.execute(new Runnable()
					{
						public void run()
						{
							_wrappedProcess.restart();
						}
					});
				}

				return;
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

		_waitingForProcessTermination = true;
		executor.execute(new Runnable()
		{
			int r = 3;

			public void run()
			{
				org.rzo.yajsw.os.Process osProcess;
				try
				{
					osProcess = ((WrappedJavaProcess) _wrappedProcess)._osProcess;
					if (_debug > 2)
						getLog().info("waiting for termination of process");
					if (osProcess != null)
						osProcess.waitFor();
					if (_debug > 1)
						getLog().info("process terminated");
				}
				finally
				{
					_waitingForProcessTermination = false;
				}
				_wrappedProcess.osProcessTerminated();
				if (_state == STATE_LOGGED_ON || _state == STATE_WAITING_CLOSED
						|| osProcess == null || osProcess.isTerminated())
				{
					stopPingCheck();
					executor.execute(new Runnable()
					{
						public void run()
						{
							setState(STATE_PROCESS_KILLED);
						}
					});
				}
			}
		});
	}

	public String stateAsStr(int state)
	{
		switch (state)
		{
		case STATE_UNKNOWN:
			return "UNKNOWN";
		case STATE_WAITING:
			return "WAITING";
		case STATE_ESTABLISHED:
			return "ESTABLISHED";
		case STATE_LOGGED_ON:
			return "LOGGED_ON";
		case STATE_STARTUP_TIMEOUT:
			return "STARTUP_TIMEOUT";
		case STATE_WAITING_CLOSED:
			return "WAITING_CLOSED";
		case STATE_USER_STOP:
			return "USER_STOP";
		case STATE_PING_TIMEOUT:
			return "PING_TIMEOUT";
		case STATE_PROCESS_KILLED:
			return "PROCESS_KILLED";
		case STATE_THRESHOLD:
			return "THRESHOLD";

		default:
			return "?";

		}
	}

	public void logStateChange(int state)
	{
		if (state == STATE_STARTUP_TIMEOUT)
			getLog().warning(
					"startup of java application timed out. if this is due to server overload consider increasing wrapper.startup.timeout");
		else if (state == STATE_PING_TIMEOUT)
			getLog().warning(
					"ping between java application and wrapper timed out. if this this is due to server overload consider increasing wrapper.ping.timeout");

	}

	public void processFailed()
	{
		stop(STATE_PROCESS_KILLED, null);
	}

	public void setServiceStartupListener(Runnable serviceStartupListener)
	{
		_serviceStartupListener = serviceStartupListener;
	}

	private void restartProcess()
	{
		executor.execute(new Runnable()
		{
			int r = 4;

			public void run()
			{
				stop(STATE_THRESHOLD, "THRESHOLD");
			}
		});
	}

	public void setHeap(float heap, long minGC, long fullGC, long heapInBytes)
	{
		_heap = heap;
		_minGC = minGC;
		_fullGC = fullGC;
		_heapInBytes = heapInBytes;
		if (_heap > -1 && _heap > _maxHeapRestart && _maxHeapRestart > 0)
		{
			getLog().warning(
					"restarting due to heap threshold : " + _heap + " > "
							+ _maxHeapRestart);
			restartProcess();
		}
		else if (_fullGC > -1 && _fullGC > _maxFullGCTimeRestart
				&& _maxFullGCTimeRestart > 0)
		{
			getLog().warning(
					"restarting due to gc duration threshold : " + _fullGC
							+ " > " + _maxFullGCTimeRestart);
			restartProcess();
		}
	}

	public float getHeap()
	{
		return _heap;
	}

	public long getMinGC()
	{
		return _minGC;
	}

	public long getFullGC()
	{
		return _fullGC;
	}

	public long getHeapInBytes()
	{
		return _heapInBytes;
	}

	public void setMaxHeapRestart(float maxHeapRestart)
	{
		_maxHeapRestart = maxHeapRestart;
	}

	public void setMaxFullGCTimeRestart(long maxFullGCTimeRestart)
	{
		_maxFullGCTimeRestart = maxFullGCTimeRestart;
	}

	public synchronized void closeChannel()
	{
		if (_channel.get() != null && _channel.get().isOpen())
			try
			{
				ChannelFuture cf = _channel.get().close();
				Thread.yield();
				if (getDebug() > 1)
					getLog().info("controller close session");
				cf.await(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				getLog().info("session close wait interrupted in JVMController");
			}
		// stop processing outgoing messages
		// _controller.workerExecutor.shutdownNow();

		// stop the controller
		_channel.set(null);
		setState(JVMController.STATE_WAITING_CLOSED);
		if (getDebug() > 2)
			getLog().info("session closed -> waiting");
	}

}
