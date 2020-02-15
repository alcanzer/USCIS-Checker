package com.alcnzr.checker.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.alcnzr.checker.model.Request;

@Repository
public interface RequestRepository extends ReactiveMongoRepository<Request, Integer> {

}