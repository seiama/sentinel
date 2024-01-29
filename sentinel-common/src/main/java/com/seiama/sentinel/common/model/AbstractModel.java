package com.seiama.sentinel.common.model;

import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface AbstractModel {
  @SuppressWarnings("ConstantName")
  String _ID = "_id";

  ObjectId _id();
}
