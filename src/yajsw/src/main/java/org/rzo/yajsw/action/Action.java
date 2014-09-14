package org.rzo.yajsw.action;

import io.netty.channel.Channel;

import java.io.IOException;
import java.io.PrintStream;

import org.rzo.yajsw.controller.Message;

public interface Action
{
	public void execute(Message msg, Channel session, PrintStream out, Object data) throws IOException;
}
