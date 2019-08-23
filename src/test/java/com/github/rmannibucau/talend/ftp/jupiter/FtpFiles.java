package com.github.rmannibucau.talend.ftp.jupiter;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;

@Target(TYPE)
@Retention(RUNTIME)
@ExtendWith(FtpServer.class)
public @interface FtpFiles {
	FtpFile[] files() default {};
	int port() default 0; // random
	String user() default "test";
	String password() default "testpwd";
	String root() default "/";
}
