//package com.github.poooower.common
//
//import android.animation.Animator
//import android.animation.AnimatorInflater
//import android.support.v4.app.Fragment
//import org.aspectj.lang.ProceedingJoinPoint
//import org.aspectj.lang.annotation.Around
//import org.aspectj.lang.annotation.Aspect
//import org.aspectj.lang.annotation.Before
//
//
//@Aspect
//class Aspect {
//
//    @Around("execution(android.animation.Animator android.support.v4.app.Fragment.onCreateAnimator(..))")
//    fun hookOnCreateAnimator(proceedingJoinPoint: ProceedingJoinPoint): Animator? {
//        val orig = proceedingJoinPoint.proceed() as Animator?
//        return orig ?: with(proceedingJoinPoint.`this` as Fragment) {
//            if (!(proceedingJoinPoint.args[1] as Boolean) && isParentFragmentRemoving) {
//                return@with AnimatorInflater.loadAnimator(app, R.animator.stay)
//            }
//            orig
//        }
//    }
//
//    private val Fragment.isParentFragmentRemoving: Boolean
//        get() {
//            var parent = parentFragment
//            while (parent != null) {
//                if (parent.isDetached || parent.isRemoving) {
//                    return true
//                }
//                parent = parent.parentFragment
//            }
//            return false
//        }
//
//
//}
//
//////pointcut postInit(): within(Application+) && execution(* Application+.onCreate());
//////
//////before() returning: postInit() {
//////    Application app = (Application) thisJoinPoint.getTarget();
//////
//////}