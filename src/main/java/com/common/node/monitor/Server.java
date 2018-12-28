package com.common.node.monitor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
 
public class Server {
 
	public static void main(String[] args) {
		
		EventLoopGroup pGroup = new NioEventLoopGroup();
		EventLoopGroup cGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(pGroup, cGroup)
			 .channel(NioServerSocketChannel.class)
			 .option(ChannelOption.SO_BACKLOG, 128)
			 .childOption(ChannelOption.SO_KEEPALIVE, true)
			 //设置日志
			 //.handler(new LoggingHandler(LogLevel.INFO))
			 .childHandler(new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel sc) throws Exception {
					//Netty中使用Marshalling
					sc.pipeline().addLast(MarshallingCodeFactory.buildMarshallingDecoder());
					sc.pipeline().addLast(MarshallingCodeFactory.buildMarshallingEncoder());
//					sc.pipeline().addLast(new StringDecoder());
//					sc.pipeline().addLast(new StringEncoder());
					sc.pipeline().addLast(new ServerHeartBeatHandler());
				}
			});
			System.out.println("NettyServer:"+9999+" start ");
			ChannelFuture cf = b.bind(9999).sync();
			cf.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			pGroup.shutdownGracefully();
			cGroup.shutdownGracefully();
		}
		
		
	}
}
