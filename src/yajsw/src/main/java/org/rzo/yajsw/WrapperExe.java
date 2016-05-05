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

package org.rzo.yajsw;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.rzo.yajsw.boot.WrapperLoader;
import org.rzo.yajsw.config.YajswConfiguration;
import org.rzo.yajsw.config.YajswConfigurationImpl;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.os.ms.win.w32.WindowsXPProcess;
import org.rzo.yajsw.tools.ConfigGenerator;
import org.rzo.yajsw.tray.TrayIconMain;
import org.rzo.yajsw.util.MyKeyStoreInterface;
import org.rzo.yajsw.wrapper.WrappedProcess;
import org.rzo.yajsw.wrapper.WrappedProcessFactory;
import org.rzo.yajsw.wrapper.WrappedProcessList;
import org.rzo.yajsw.wrapper.WrappedService;

import com.sun.jna.PlatformEx;

// TODO: Auto-generated Javadoc
/**
 * The Class WrapperExe.
 */
public class WrapperExe
{

	/** The conf file. */
	static String confFile;

	static List confFileList;

	/** The properties. */
	static List properties;

	/** The cmds. */
	static List cmds;

	/** The pid. */
	static int pid;

	/** The pid. */
	static String defaultFile;

	static WrappedService _service = null;

	static boolean _exitOnTerminate = true;

	static int _exitCode = 0;

	static Map<String, Object> _properties = new HashMap<String, Object>();

	static List keyValue;
	
	static Options options = new Options();
	static CommandLine cl;


	private static WrappedService getService()
	{
		if (_service != null)
			return _service;
		prepareProperties();
		_service = new WrappedService();
		if (confFileList != null && confFileList.size() > 1)
			_service.setConfFilesList(confFileList);
		_service.setLocalConfiguration(new MapConfiguration(_properties));
		_service.init();
		return _service;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args)
	{
		System.out.println("YAJSW: " + YajswVersion.YAJSW_VERSION);
		System.out.println("OS   : " + YajswVersion.OS_VERSION);
		System.out.println("JVM  : " + YajswVersion.JAVA_VERSION);
		String wrapperJar = WrapperLoader.getWrapperJar();
		String homeDir = new File(wrapperJar).getParent();
		if (!OperatingSystem.instance().setWorkingDir(homeDir))
			System.out
					.println("could not set working dir, pls check configuration or user rights: "
							+ homeDir);

		// System.out.println(System.getProperty("java.class.path"));
		buildOptions();
		parseCommand(args);
		for (Option option : cl.getOptions())
			executeCommand(option);
		/*
		if (cmds != null && cmds.size() > 0)
			for (Iterator it = cmds.iterator(); it.hasNext();)
			{
				Object cmd = it.next();
				if (cmd instanceof DefaultOption)
					executeCommand((Option) cmd);
			}
		else
			executeCommand(group.findOption("c"));
			*/
		if (_exitOnTerminate)
			Runtime.getRuntime().halt(_exitCode);
	}

	private static File File(String property)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Execute command.
	 * 
	 * @param cmd
	 *            the cmd
	 */
	private static void executeCommand(Option cmd)
	{
		String opt = cmd.getOpt();
		if ("c".equals(opt))
			doConsole();
		else if ("t".equals(opt))
			doStart();
		else if ("p".equals(opt))
			doStop();
		else if ("tx".equals(opt))
			doStartPosix();
		else if ("px".equals(opt))
			doStopPosix();
		else if ("i".equals(opt))
			doInstall();
		else if ("r".equals(opt))
			doRemove();
		else if ("rw".equals(opt))
			doRemoveWait();
		else if ("n".equals(opt))
			doReconnect();
		else if ("g".equals(opt))
			doGenerate();
		else if ("q".equals(opt))
			doState();
		else if ("qs".equals(opt))
			doStateSilent();
		else if ("qx".equals(opt))
			doStatePosix();
		else if ("y".equals(opt))
			doStartTrayIcon();
		else if ("k".equals(opt))
		{
			keyValue = Arrays.asList(cl.getOptionValues("k"));
			doAddKey();
		}
		else
			System.out
			.println("unimplemented option ");

/*		switch ()
		{
		case OPTION_C:
			doConsole();
			break;
		case OPTION_T:
			doStart();
			break;
		case OPTION_P:
			doStop();
			break;
		case OPTION_TX:
			doStartPosix();
			break;
		case OPTION_PX:
			doStopPosix();
			break;
		case OPTION_I:
			doInstall();
			break;
		case OPTION_R:
			doRemove();
			break;
		case OPTION_RW:
			doRemoveWait();
			break;
		case OPTION_N:
			pid = ((Long) cl.getValue(cmd)).intValue();
			doReconnect();
			break;
		case OPTION_G:
			pid = ((Long) cl.getValue(cmd)).intValue();
			doGenerate();
			break;
		case OPTION_D:
			break;
		case OPTION_Q:
			doState();
			break;
		case OPTION_QS:
			doStateSilent();
		case OPTION_QX:
			doStatePosix();
			break;
		case OPTION_Y:
			doStartTrayIcon();
			break;
		case OPTION_K:
			keyValue = cl.getValues(cmd);
			doAddKey();
			break;
		default:
			System.out
					.println("unimplemented option " + cmd.getPreferredName());
		}
		*/
	}

	private static void doAddKey()
	{
		String key = (String) keyValue.get(0);
		String value = (String) keyValue.get(1);
		try
		{
			MyKeyStoreInterface ks = getMyKeystore();
			ks.init();
			ks.put(key, value.toCharArray());
			System.out.println("added key " + key + " to keystore "
					+ ks.getFile());

		}
		catch (Exception ex)
		{
			System.out
					.println("error storing data in keystore -> check folder or user rights");
			System.out.println(ex.getMessage());
		}
	}

	private static MyKeyStoreInterface getMyKeystore() throws Exception
	{
		Class clazz = MyKeyStoreInterface.class.getClassLoader().loadClass(
				"org.rzo.yajsw.util.MyKeyStore");
		return (MyKeyStoreInterface) clazz.newInstance();
	}

	/**
	 * Do reconnect.
	 */
	private static void doReconnect()
	{
		prepareProperties();
		Configuration localConf = new MapConfiguration(_properties);
		YajswConfiguration conf = new YajswConfigurationImpl(localConf, true);
		WrappedProcess w = WrappedProcessFactory.createProcess(conf);

		System.out.println("************* RECONNECTING WRAPPER TO PID  " + pid
				+ " ***********************");
		System.out.println();

		if (w.reconnect(pid))
			System.out.println("Connected to PID " + pid);
		else
			System.out.println("NOT connected to PID " + pid);
		_exitOnTerminate = false;

	}

	/**
	 * Do remove.
	 */
	private static void doRemove()
	{
		prepareProperties();
		WrappedService w = getService();
		System.out.println("************* REMOVING " + w.getServiceName()
				+ " ***********************");
		System.out.println();
		boolean result = w.uninstall();

		if (PlatformEx.isWinVista() && w.requiresElevate())
		{
			System.out.println("try uac elevate");
			WindowsXPProcess.elevateMe();
			return;
		}
		if (result)
			System.out.println("Service " + w.getServiceName() + " removed");
		else
			System.out
					.println("Service " + w.getServiceName() + " NOT removed");

	}

	private static void doRemoveWait()
	{
		prepareProperties();
		WrappedService w = getService();
		System.out.println("************* REMOVING " + w.getServiceName()
				+ " ***********************");
		System.out.println();
		boolean result = w.uninstall();

		if (PlatformEx.isWinVista() && w.requiresElevate())
		{
			System.out.println("try uac elevate");
			WindowsXPProcess.elevateMe();
			return;
		}
		if (result)
		{
			while (w.isInstalled())
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
					return;
				}
			System.out.println("Service " + w.getServiceName() + " removed");
		}
		else
			System.out
					.println("Service " + w.getServiceName() + " NOT removed");

	}

	/**
	 * Do install.
	 */
	private static void doInstall()
	{
		WrappedService w = getService();
		System.out.println("************* INSTALLING " + w.getServiceName()
				+ " ***********************");
		System.out.println();
		int i = 0;
		while (w.isInstalled() && i < 10)
		{
			if (PlatformEx.isWinVista() && w.requiresElevate())
			{
				System.out.println("try uac elevate");
				WindowsXPProcess.elevateMe();
				return;
			}

			i++;
			w.uninstall();
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}

		boolean result = w.install();
		if (PlatformEx.isWinVista() && w.requiresElevate())
		{
			System.out.println("try uac elevate");
			WindowsXPProcess.elevateMe();
			return;
		}

		if (result)
			System.out.println("Service " + w.getServiceName() + " installed");
		else
			System.out.println("Service " + w.getServiceName()
					+ " NOT installed");

	}

	/**
	 * Do stop.
	 */
	private static void doStop()
	{
		WrappedService w = getService();

		System.out.println("************* STOPPING " + w.getServiceName()
				+ " ***********************");
		System.out.println();

		try
		{
			w.stop();
			if (PlatformEx.isWinVista() && w.requiresElevate())
			{
				System.out.println("try uac elevate");
				WindowsXPProcess.elevateMe();
				return;
			}

			if (w.isRunning())
				System.out.println("Service " + w.getServiceName()
						+ " NOT stopped");
			else
				System.out
						.println("Service " + w.getServiceName() + " stopped");
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void doStopPosix()
	{
		WrappedService w = getService();

		System.out.println("************* STOPPING " + w.getServiceName()
				+ " ***********************");
		System.out.println();

		try
		{
			w.stopProcess();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!w.isRunning())
		{
			System.out.println("Service " + w.getServiceName() + " stopped");
			_exitCode = 0;
			_exitOnTerminate = true;
		}
		else
		{
			System.out.println("Service" + w.getServiceName() + " NOT stopped");
			_exitCode = 1;
			_exitOnTerminate = true;
		}

	}

	/**
	 * Do start.
	 */
	private static void doStart()
	{
		WrappedService w = getService();
		// w.setDebug(true);
		w.init();

		System.out.println("************* STARTING " + w.getServiceName()
				+ " ***********************");
		System.out.println();

		w.start();
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (PlatformEx.isWinVista() && w.requiresElevate())
		{
			System.out.println("try uac elevate");
			WindowsXPProcess.elevateMe();
			return;
		}
		int i = 0;
		while (!w.isRunning() && i++ < 30)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				return;
			}
			if (!w.isStarting())
				break;
		}

		if (w.isRunning())
		{
			System.out.println("Service " + w.getServiceName() + " started");
			_exitCode = 0;
			_exitOnTerminate = true;
		}
		else
		{
			System.out
					.println("Service " + w.getServiceName() + " NOT started");
			_exitCode = 1;
			_exitOnTerminate = true;
		}

	}

	private static void doStartPosix()
	{
		WrappedService w = getService();
		System.out.println("************* STARTING " + w.getServiceName()
				+ " ***********************");
		System.out.println();

		w.startProcess();
		int i = 0;
		while (!w.isRunning() && i < 10)
		{
			i++;
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		if (w.isRunning())
			System.out.println("Service " + w.getServiceName() + " started");
		else
			System.out
					.println("Service " + w.getServiceName() + " NOT started");
		_exitOnTerminate = true;

	}

	/**
	 * Do start.
	 */
	private static void doStartTrayIcon()
	{
		prepareProperties();
		String[] args;
		if (_service != null)
			args = new String[] { _service.getConfigLocalPath() };
		else
			args = new String[] { confFile };
		try
		{
			TrayIconMain.main(args);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		_exitOnTerminate = true;
	}

	private static void doState()
	{
		prepareProperties();
		WrappedService w = getService();
		int state = w.state();
		System.out.print("Name        : ");
		System.out.println(w.getServiceName());
		System.out.print("Installed   : ");
		System.out.println(w.isInstalled(state));
		System.out.print("Running     : ");
		System.out.println(w.isRunning(state));
		System.out.print("Interactive : ");
		System.out.println(w.isInteractive(state));
		System.out.print("Automatic   : ");
		System.out.println(w.isAutomatic(state));
		System.out.print("Manual      : ");
		System.out.println(w.isManual(state));
		System.out.print("Disabled    : ");
		System.out.println(w.isDisabled(state));
		System.out.print("Paused      : ");
		System.out.println(w.isPaused(state));
		System.out.print("Unknown      : ");
		System.out.println(w.isStateUnknown(state));
	}

	private static void doStateSilent()
	{
		prepareProperties();
		WrappedService w = getService();
		w.init();
		int state = w.state();
	}

	private static void doStatePosix()
	{
		prepareProperties();
		WrappedService w = getService();
		int state = w.state();
		if (w.isRunning(state))
			_exitCode = 0;
		else
			_exitCode = 3;
		_exitOnTerminate = true;
	}

	/**
	 * Do console.
	 */
	private static void doConsole()
	{
		prepareProperties();
		final WrappedProcessList list = WrappedProcessFactory
				.createProcessList(_properties, confFileList, true);
		list.startAll();
		// Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		// {
		//
		// public void run()
		// {
		// list.onStopWrapper();
		// }
		//
		// }));
		_exitOnTerminate = false;
	}

	private static void doGenerate()
	{
		System.out
				.println("************* GENERATING YAJSW CONFIGURATION FOR PID "
						+ pid + " ***********************");
		System.out.println();
		if (defaultFile != null)
			ConfigGenerator.generate(pid, new File(defaultFile), new File(
					confFile));
		else
			ConfigGenerator.generate(pid, null, new File(confFile));

	}
	
	private static void doHelp(String reason)
	{
		HelpFormatter formatter = new HelpFormatter();
		String header = reason != null ? "Error: "+reason : "";
		String footer = "";
		formatter.printHelp("java -jar wrapper.jar", header, options, footer, true);
	}

	/**
	 * Prepare properties.
	 */
	private static void prepareProperties()
	{
		if (confFile != null)
			_properties.put("wrapper.config", confFile);
		if (defaultFile != null)
			_properties.put("wrapperx.default.config", defaultFile);
		if (properties != null)
			for (Iterator it = properties.iterator(); it.hasNext();)
			{
				String prop = (String) it.next();
				String key = prop.substring(0, prop.indexOf('='));
				String value = prop.substring(prop.indexOf('=') + 1);
				_properties.put(key, value);
			}
	}

	/**
	 * Parses the command.
	 * 
	 * @param args
	 *            the args
	 */
	private static void parseCommand(String[] args)
	{
		CommandLineParser parser = new DefaultParser();
		try
		{
		cl = parser.parse( options, args);
		}
		catch (Exception ex)
		{
			doHelp(ex.getMessage());
		}

		// abort application if no CommandLine was parsed
		if (cl == null)
		{
			System.exit(-1);
		}
		try
		{
			confFileList = new ArrayList();
			for (Option option : cl.getOptions())
			{
				if (option.hasArgName() && option.getArgName().equals("configFile"))
					confFileList.addAll(Arrays.asList(option.getValues()));
				else if (option.hasArgName() && option.getArgName().equals("PID"))
					pid = Integer.parseInt(option.getValue());
			}
			for (String arg : cl.getArgs())
			{
				if (Pattern.matches("wrapper\\..*=.*", arg))
					properties.add(arg);
				else
					confFileList.add(arg);
			}
			if (confFileList.isEmpty())
			{
				if (!cl.getOptions()[0].getOpt().equals("k"))
					System.out.println("no wrapper config file found ");
			}
			else
				confFile = (String) confFileList.get(0);
			/*
			 * confFileList = cl.getValues(CONF_FILE); if (confFileList == null
			 * || confFileList.isEmpty())
			 * System.out.println("no wrapper config file found "); else
			 * confFile = (String) confFileList.get(0);
			 */
		}
		catch (Exception ex)
		{
			System.out.println("no wrapper config file found ");
		}
		try
		{
			defaultFile = (String) cl.getOptionValue("d");
			if (defaultFile != null)
				defaultFile = new File(defaultFile).getCanonicalPath();
		}
		catch (Exception ex)
		{
			// no defaults -> maybe ok
		}
		// properties = cl.getValues(PROPERTIES);

	}

	/**
	 * Builds the options.
	 */
	private static void buildOptions()
	{
		
		options = new Options();
		options.addOption(Option.builder("c")
				.argName("configFile")
				.longOpt("console")
				.desc("run as a Console application")
				.hasArgs()
				.build()				
				);		
		
		options.addOption(Option.builder("t")
				.argName("configFile")
				.longOpt("start")
				.desc("starT an NT service or Unix daemon")
				.hasArgs()
				.build()				
				);		
		
		options.addOption(Option.builder("p")
				.argName("configFile")
				.longOpt("stop")
				.desc("stoP a running NT service or Unix daemon")
				.hasArgs()
				.build()				
				);		
		
		options.addOption(Option.builder("tx")
				.argName("configFile")
				.longOpt("startx")
				.desc("starT -internal a Posix daemon")
				.hasArgs()
				.build()				
				);		
		
		options.addOption(Option.builder("px")
				.argName("configFile")
				.longOpt("stopx")
				.desc("stoP -internal- a running Posix daemon")
				.hasArgs()
				.build()				
				);		
		
		options.addOption(Option.builder("i")
				.argName("configFile")
				.longOpt("install")
				.desc("Install an NT service or Unix daemon")
				.hasArgs()
				.build()				
				);		
		
		options.addOption(Option.builder("r")
				.argName("configFile")
				.longOpt("remove")
				.desc("Remove an NT service or Unix daemon")
				.hasArg()
				.build()				
				);		
		
		options.addOption(Option.builder("rw")
				.argName("configFile")
				.longOpt("removeWait")
				.desc("Remove an NT service or Unix daemon and wait until it is removed")
				.hasArgs()
				.build()				
				);		
		
		options.addOption(Option.builder("q")
				.argName("configFile")
				.longOpt("query")
				.desc("Query the status of an NT service or Unix daemon")
				.hasArgs()
				.build()				
				);		
		
		options.addOption(Option.builder("y")
				.argName("configFile")
				.longOpt("tray")
				.desc("Start System Tray Icon")
				.hasArgs()
				.build()				
				);		
		
		options.addOption(Option.builder("k")
				.argName("key value")
				.numberOfArgs(2)
				.longOpt("addKey")
				.desc("Add Key/Value to Keystore")
				.build()				
				);		
		
		options.addOption(Option.builder("qs")
				.argName("configFile")
				.hasArgs()
				.longOpt("querysilent")
				.desc("Silent Query the status of an NT service or Unix daemon")
				.build()				
				);		
		
		options.addOption(Option.builder("qx")
				.argName("configFile")
				.hasArgs()
				.longOpt("queryposix")
				.desc("Query the status of a posix daemon. Return status as exit code")
				.build()				
				);		
		
		options.addOption(Option.builder("n")
				.argName("PID")
				.longOpt("reconnect")
				.desc("recoNnect to existing application")
				.hasArg()
				.type(Integer.class)
				.build()				
				);		
		
		options.addOption(Option.builder("d")
				.argName("defConfFile")
				.hasArg()
				.longOpt("defaultConf")
				.desc("Default Configuration File")
				.build()				
				);		
		
		options.addOption(Option.builder("g")
				.argName("PID")
				.longOpt("genconfig")
				.desc("Generate configuration file from pid")
				.hasArg()
				.type(Integer.class)
				.build()				
				);		

		options.addOption(Option.builder("h")
				.longOpt("help")
				.desc("help")
				.build()				
				);		

		
		/*		
		gBuilder.withOption(oBuilder.reset().withId(OPTION_C)
				.withShortName("c").withLongName("console")
				.withDescription("run as a Console application").create());
		gBuilder.withOption(oBuilder.reset().withId(OPTION_T)
				.withShortName("t").withLongName("start")
				.withDescription("starT an NT service or Unix daemon").create());
		gBuilder.withOption(oBuilder.reset().withId(OPTION_P)
				.withShortName("p").withLongName("stop")
				.withDescription("stoP a running NT service or Unix daemon")
				.create());
		gBuilder.withOption(oBuilder.reset().withId(OPTION_TX)
				.withShortName("tx").withLongName("startx")
				.withDescription("starT -internal a Posix daemon").create());
		gBuilder.withOption(oBuilder.reset().withId(OPTION_PX)
				.withShortName("px").withLongName("stopx")
				.withDescription("stoP -internal- a running Posix daemon")
				.create());
		gBuilder.withOption(oBuilder.reset().withId(OPTION_I)
				.withShortName("i").withLongName("install")
				.withDescription("Install an NT service or Unix daemon")
				.create());
		gBuilder.withOption(oBuilder.reset().withId(OPTION_R)
				.withShortName("r").withLongName("remove")
				.withDescription("Remove an NT service or Unix daemon")
				.create());
		gBuilder.withOption(oBuilder
				.reset()
				.withId(OPTION_RW)
				.withShortName("rw")
				.withLongName("removeWait")
				.withDescription(
						"Remove an NT service or Unix daemon and wait until it is removed")
				.create());
		gBuilder.withOption(oBuilder
				.reset()
				.withId(OPTION_Q)
				.withShortName("q")
				.withLongName("query")
				.withDescription(
						"Query the status of an NT service or Unix daemon")
				.create());
		gBuilder.withOption(oBuilder.reset().withId(OPTION_Y)
				.withShortName("y").withLongName("tray")
				.withDescription("Start System Tray Icon").create());
		gBuilder.withOption(oBuilder
				.reset()
				.withId(OPTION_QS)
				.withShortName("qs")
				.withLongName("querysilent")
				.withDescription(
						"Silent Query the status of an NT service or Unix daemon")
				.create());
		gBuilder.withOption(oBuilder
				.reset()
				.withId(OPTION_QX)
				.withShortName("qx")
				.withLongName("queryposix")
				.withDescription(
						"Query the status of a posix daemon. Return status as exit code")
				.create());

		Argument ksKey = aBuilder.reset().withName(KS_KEY_VALUE)
				.withDescription("Key/Value in Keystore").withMinimum(2)
				.withMaximum(2).create();

		gBuilder.withOption(oBuilder.reset().withId(OPTION_K)
				.withShortName("k").withLongName("keystoreAdd")
				.withDescription("Add Key/Value to Keystore")
				.withArgument(ksKey).create());

		Argument pid = aBuilder.reset().withName(PID)
				.withDescription("PID of process to reconnect to")
				.withMinimum(1).withMaximum(1)
				.withValidator(NumberValidator.getIntegerInstance()).create();

		gBuilder.withOption(oBuilder.reset().withId(OPTION_N)
				.withShortName("n").withLongName("reconnect")
				.withDescription("recoNnect to existing application")
				.withArgument(pid).create());

		Argument pid2 = aBuilder.reset().withName(PID)
				.withDescription("PID of process to reconnect to")
				.withMinimum(1).withMaximum(1)
				.withValidator(NumberValidator.getIntegerInstance()).create();

		Argument defaultFile = aBuilder
				.reset()
				.withName(DEFAULT_FILE)
				.withDescription("Default Configuration File")
				.withMinimum(0)
				.withMaximum(1)
				.withValidator(
						VFSFileValidator.getExistingFileInstance().setBase("."))
				.create();
		/*
		 * GroupBuilder childGbuilder = new GroupBuilder(); DefaultOptionBuilder
		 * childoObuilder = new DefaultOptionBuilder("-", "--", true);
		 * 
		 * childGbuilder.withName(DEFAULT_FILE).withMinimum(0).withMaximum(1).
		 * withOption(
		 * childoObuilder.withId(OPTION_D).withArgument(defaultFile).
		 * withShortName("d").withLongName("defaultConf").withDescription(
		 * "Default Configuration File").create());
		 * 
		 * 
		 * 
		 * gBuilder.withOption(oBuilder.reset().withId(OPTION_G).withShortName("g"
		 * ).withLongName("genconf").withDescription(
		 * "Generate configuration file from pid"
		 * ).withArgument(pid2).withChildren(childGbuilder.create()).create());
		 */
/*		gBuilder.withOption(oBuilder.reset().withId(OPTION_D)
				.withShortName("d").withLongName("defaultConf")
				.withDescription("Default Configuration File")
				.withArgument(defaultFile).create());

		gBuilder.withOption(oBuilder.reset().withId(OPTION_G)
				.withShortName("g").withLongName("genconf")
				.withDescription("Generate configuration file from pid")
				.withArgument(pid2).create());

		FileValidator fValidator = VFSFileValidator.getExistingFileInstance()
				.setBase(".");
		fValidator.setFile(false);
		// fValidator.setReadable(true);
		gBuilder.withOption(aBuilder
				.reset()
				.withName(ARGS)
				.withDescription(
						"Arguments: a list of configuration files, for example conf/wrapper.conf followed by an optional list of configuration name-value pairs, for example wrapper.debug=true")
				.withMinimum(0).create());

		gBuilder.withMaximum(3);
*/
		/*
		 * Validator pValidator = new Validator() {
		 * 
		 * public void validate(List values) throws InvalidArgumentException {
		 * for (Iterator it = values.iterator(); it.hasNext();) { String p =
		 * (String) it.next(); if (!Pattern.matches("wrapper\\..*=.*", p)) {
		 * throw new InvalidArgumentException(p); } }
		 * 
		 * }
		 * 
		 * };
		 * gBuilder.withOption(aBuilder.reset().withName(PROPERTIES).withDescription
		 * (
		 * "are configuration name-value pairs which override values. For example: wrapper.debug=true"
		 * ).withMinimum(0).withValidator(pValidator) .create());
		 */

		//group = gBuilder.create();
		

	}

}
