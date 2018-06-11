//package com.github.poooower.common
//
//import android.app.Application
//
//aspect AppStartNotifier {
//
//    pointcut postInit(): within(Application+) && execution(* Application+.onCreate());
//
//    before() returning: postInit() {
//        Application app = (Application) thisJoinPoint.getTarget();
//
//    }
//}