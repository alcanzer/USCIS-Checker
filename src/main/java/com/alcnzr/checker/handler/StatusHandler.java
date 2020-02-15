package com.alcnzr.checker.handler;


import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alcnzr.checker.model.Event;
import com.alcnzr.checker.model.Request;
import com.alcnzr.checker.repository.DBHelper;
import com.alcnzr.checker.repository.RequestRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class StatusHandler {
	
	@Autowired
	private RequestRepository repository;
	@Autowired
	private DBHelper helper;
	
	private static final Logger logger = LoggerFactory.getLogger(StatusHandler.class);
	
	public Mono<ServerResponse> getStatus(ServerRequest req) {
		Flux<Request> body = req.bodyToFlux(Request.class);
		return ServerResponse.ok()
				.body(body
						.log()
						.map(request -> {
							repository.save(request)
							.subscribe( r -> logger.info(r.toString()));
							return request.getReceipt();
						})
						.collect(Collectors.joining(",", "Service started for receipts: ", "."))
						, String.class);
	}
	
	public Mono<ServerResponse> getReceipts(ServerRequest req) {
		return ServerResponse.ok()
				.body(helper.getRep(Request.class).findAll()
						, Request.class);
		
	}
	
	public Mono<ServerResponse> getEvents(ServerRequest req) {
		return ServerResponse.ok()
				.body(helper.getRep(Event.class).findAll()
						, Event.class);
		
	}
	
	public Mono<ServerResponse> getErrors(ServerRequest req) {
		return ServerResponse.ok()
				.body(helper.getRep(com.alcnzr.checker.model.Error.class).findAll()
						, com.alcnzr.checker.model.Error.class);
		
	}
}
