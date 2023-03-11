package com.seiama.sentinel.common.model;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModMailRepository extends ReactiveMongoRepository<ModMailModel.Complete, ObjectId>, ModMailRepositoryCustom {
}
