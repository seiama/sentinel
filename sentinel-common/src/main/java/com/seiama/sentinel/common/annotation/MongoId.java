package com.seiama.sentinel.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seiama.sentinel.common.jackson.ObjectIdExtendedJsonSerializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@JacksonAnnotationsInside
@JsonSerialize(using = ObjectIdExtendedJsonSerializer.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface MongoId {
}
