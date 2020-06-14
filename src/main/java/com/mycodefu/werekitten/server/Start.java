package com.mycodefu.werekitten.server;

import java.util.concurrent.atomic.AtomicReference;

import com.mycodefu.werekitten.server.netty.NettyServer;
import com.mycodefu.werekitten.server.world.WorldManager;

public class Start {
public static void main(String[] args) {
	AtomicReference<NettyServer> server = new AtomicReference<>();
	WorldManager manager = new WorldManager();
	server.set(new NettyServer(51273, manager));
	manager.setServer(server.get());
	server.get().listen();
	Runtime.getRuntime().addShutdownHook(new Thread(()-> {
		server.get().close();
	}));
}
}
