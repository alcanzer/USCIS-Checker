package com.alcnzr.checker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.alcnzr.checker.handler.StatusHandler;

@Configuration
public class RouterConfig {
	
	@Bean
	public RouterFunction<ServerResponse> routes(StatusHandler handler) {
		return RouterFunctions
				.route(RequestPredicates.POST("/status/"), handler::getStatus)
				.andRoute(RequestPredicates.GET("/receipt"), handler::getReceipts)
				.andRoute(RequestPredicates.GET("/event"), handler::getEvents)
				.andRoute(RequestPredicates.GET("/error"), handler::getErrors);
	}
}
