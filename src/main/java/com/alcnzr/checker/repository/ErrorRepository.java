package com.alcnzr.checker.repository;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.alcnzr.checker.model.Error;

@Repository
public interface ErrorRepository extends ReactiveMongoRepository<Error, DateTime> {

}
