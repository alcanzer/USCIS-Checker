package com.alcnzr.checker.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.alcnzr.checker.model.Event;

@Repository
public interface EventRepository extends ReactiveMongoRepository<Event, String> {

}
