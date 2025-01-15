package com.seiama.sentinel.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seiama.sentinel.common.jackson.ObjectIdExtendedJsonSerializer;
import com.seiama.sentinel.common.model.AbstractModel;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Id
@Field(AbstractModel._ID)
@JacksonAnnotationsInside
@JsonProperty(AbstractModel._ID)
@JsonSerialize(using = ObjectIdExtendedJsonSerializer.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MongoPrimaryId {
}
