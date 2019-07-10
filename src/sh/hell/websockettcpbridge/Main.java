package sh.hell.websockettcpbridge;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;
import java.util.HashMap;

public class Main
{
	static final HashMap<ChannelId, Client> clients = new HashMap<>();
	private static final File CERT_FILE = new File("server.crt");
	private static final File PRIV_FILE = new File("server.key");
	static String target_hostname;
	static int target_port;

	public static void main(String[] args) throws Exception
	{
		if(args.length != 3)
		{
			System.out.println("Syntax: websocket-tcp-bridge <websocket-port> <target-hostname> <target-port>");
			return;
		}
		final int websocket_port = Integer.parseInt(args[0]);
		Main.target_port = Integer.parseInt(args[2]);
		Main.target_hostname = args[1];
		SslContext sslCtx = null;
		if(CERT_FILE.isFile() && PRIV_FILE.isFile())
		{
			sslCtx = SslContextBuilder.forServer(CERT_FILE, PRIV_FILE).build();
		}
		System.out.print("Starting Server...");
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try
		{
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			 .channel(NioServerSocketChannel.class)
			 .childHandler(new WebSocketServerInitializer(sslCtx));
			Channel ch = b.bind(websocket_port).sync().channel();
			System.out.println(" Server online.");
			if(sslCtx == null)
			{
				System.out.println("Use ws://hostname:" + websocket_port + " to connect.");
				System.out.println("Provide server.crt + server.key in the working directory to enable TLS (wss://).");
			} else
			{
				System.out.println("Using TLS. Use wss://hostname:" + websocket_port + " to connect.");
			}
			ch.closeFuture().sync();
		}
		finally
		{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
