package com.badlogicgames.superjumper.framework

import org.robovm.apple.foundation.Foundation
import org.robovm.apple.foundation.NSObject
import org.robovm.apple.uikit.UIApplication
import org.robovm.objc.annotation.CustomClass
import org.robovm.objc.annotation.Method

/**
 * Use this as Facade to get access to other Framework functionalities
 */
@CustomClass("IOSGameLauncher")
class IOSGameLauncher @Method(selector = "init") constructor() : NSObject() {
    private val app: CustomIOSApplication

    /**
     * mapping method to -(BOOL)didFinishLaunching selector
     */
    @Method(selector = "didFinishLaunching:")
    fun didFinishLaunching(application: UIApplication): Boolean {
        Foundation.log("IOSGameLauncher didFinishLaunching was called!")
        application.addStrongRef(this) // Prevent this from being GCed until the ObjC UIApplication is deallocated
        return app.didFinishLaunching(application, null)
    }

    /**
     * mapping method to -(void)didBecomeActive selector
     */
    @Method(selector = "didBecomeActive:")
    fun didBecomeActive(application: UIApplication?) {
        app.didBecomeActive(application)
        Foundation.log("IOSGameLauncher didBecomeActive was called!")
    }

    /**
     * mapping method to -(void)willEnterForeground selector
     */
    @Method(selector = "willEnterForeground:")
    fun willEnterForeground(application: UIApplication?) {
        app.willEnterForeground(application)
        Foundation.log("IOSGameLauncher willEnterForeground was called!")
    }

    /**
     * mapping method to -(void)willResignActive selector
     */
    @Method(selector = "willResignActive:")
    fun willResignActive(application: UIApplication?) {
        app.willResignActive(application)
        Foundation.log("IOSGameLauncher willResignActive was called!")
    }

    /**
     * mapping method to -(void)willTerminate selector
     */
    @Method(selector = "willTerminate:")
    fun willTerminate(application: UIApplication?) {
        app.willTerminate(application)
        Foundation.log("IOSGameLauncher willTerminate was called!")
    }

    companion object {
        /**
         * Returns the singleton [IOSGameLauncher] instance. This is exposed
         * to Objective-C code using the `instance` selector.
         */
        /**
         * The singleton [IOSGameLauncher] instance.
         */
        @JvmStatic
        @get:Method(selector = "instance")
        var instance: IOSGameLauncher? = null
            get() {
                field = IOSGameLauncher()
                return field
            }
            private set

        /**
         * mapping method to +(void)hello selector
         */
        @Method(selector = "hello")
        fun hello() {
            Foundation.log("Hello world from RoboVM framework")
        }
    }

    /**
     * mapping constructor to -(void)init selector
     */
    init {
        val config = CustomIOSApplicationConfiguration()
        app = CustomIOSApplication(SuperJumper(), config)
        Foundation.log("IOSGameLauncher default constructor was called!")
    }
}