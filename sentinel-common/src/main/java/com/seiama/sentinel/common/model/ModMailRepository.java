package com.seiama.sentinel.common.model;

import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface ModMailRepository extends ReactiveMongoRepository<ModMailModel.Complete, ObjectId>, ModMailRepositoryCustom {
}
