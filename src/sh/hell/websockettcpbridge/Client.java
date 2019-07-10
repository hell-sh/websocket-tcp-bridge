package sh.hell.websockettcpbridge;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client extends Thread
{
	final OutputStream os;
	private final ChannelHandlerContext ctx;
	private final Socket socket;

	Client(ChannelHandlerContext ctx) throws IOException
	{
		this.ctx = ctx;
		this.socket = new Socket(Main.target_hostname, Main.target_port);
		this.os = socket.getOutputStream();
		this.setName("Client " + ctx.channel().id());
		this.start();
	}

	@Override
	public void run()
	{
		try
		{
			final InputStream is = this.socket.getInputStream();
			do
			{
				int available = is.available();
				if(available > 0)
				{
					byte[] bytes = new byte[available];
					is.read(bytes);
					ctx.writeAndFlush(new TextWebSocketFrame(new String(bytes)));
				}
			}
			while(!this.isInterrupted() && ctx.channel().isOpen());
		}
		catch(IOException e)
		{
			ctx.writeAndFlush(new TextWebSocketFrame(e.getMessage()));
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch(IOException ignored)
			{
			}
			ctx.close();
		}
	}
}
