package com.jay.template.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import com.jay.template.Starter;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/*
 * make sure to include all annotations in other functional tests so configured propagation stays same and the same
 * spring instance is used instead of starting up a new one. This ensures functionalTest suite speed.
 */
@SpringBootTest(classes = Starter.class, webEnvironment = RANDOM_PORT)
@Target(ElementType.TYPE) // use this annotation on class
@Retention(RetentionPolicy.RUNTIME) //So Spring can see this annotation
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public @interface SpringBootTestShared {
}
