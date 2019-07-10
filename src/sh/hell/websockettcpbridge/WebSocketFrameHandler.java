package sh.hell.websockettcpbridge;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.io.IOException;
import java.io.OutputStream;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame>
{
	@Override
	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception
	{
		super.userEventTriggered(ctx, evt);
		//noinspection deprecation
		if(evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE)
		{
			synchronized(Main.clients)
			{
				Main.clients.put(ctx.channel().id(), new Client(ctx));
			}
			ctx.channel().closeFuture().addListener((ChannelFutureListener) future->{
				synchronized(Main.clients)
				{
					Main.clients.get(ctx.channel().id()).interrupt();
				}
			});
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws IOException
	{
		if(frame instanceof TextWebSocketFrame)
		{
			final OutputStream os;
			synchronized(Main.clients)
			{
				os = Main.clients.get(ctx.channel().id()).os;
			}
			os.write(((TextWebSocketFrame) frame).text().getBytes());
			os.flush();
		}
	}
}
