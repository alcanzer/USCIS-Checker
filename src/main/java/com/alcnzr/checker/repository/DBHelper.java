package com.alcnzr.checker.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;


@Component
public class DBHelper {
	
	@Autowired
	private RequestRepository requestRep;
	@Autowired
	private EventRepository eventRep;
	@Autowired
	private ErrorRepository errorRep;
	
	
	@SuppressWarnings("unchecked")
	public <T> ReactiveMongoRepository<T, T> getRep(Class<T> obj){
		if(obj.getSimpleName().equalsIgnoreCase("event")) return (ReactiveMongoRepository<T, T>) eventRep;
		if(obj.getSimpleName().equalsIgnoreCase("error")) return (ReactiveMongoRepository<T, T>) errorRep;
		return (ReactiveMongoRepository<T, T>) requestRep;
	}
}
