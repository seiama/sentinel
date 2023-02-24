package com.seiama.sentinel.common.model;

import org.bson.types.ObjectId;

public interface AbstractModel {
  @SuppressWarnings("ConstantName")
  String _ID = "_id";

  ObjectId _id();
}
