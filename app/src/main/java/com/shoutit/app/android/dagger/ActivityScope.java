package com.shoutit.app.android.dagger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

@Scope
//@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityScope {
}