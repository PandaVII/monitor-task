package com.common.node.monitor;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
 
public class Client {
 
	public static void main(String[] args) throws Exception{
		
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(group)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				//Netty中使用Marshalling
				sc.pipeline().addLast(MarshallingCodeFactory.buildMarshallingDecoder());
				sc.pipeline().addLast(MarshallingCodeFactory.buildMarshallingEncoder());
//				sc.pipeline().addLast(new StringDecoder());
//				sc.pipeline().addLast(new StringEncoder());
				sc.pipeline().addLast(new ClienHeartBeatHandler());
			}
		});
		
		ChannelFuture cf = b.connect("127.0.0.1", 9999).sync();
		
		cf.channel().closeFuture().sync();
		group.shutdownGracefully();
	}
}
