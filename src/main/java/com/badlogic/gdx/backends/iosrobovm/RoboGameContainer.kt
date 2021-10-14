package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.ApplicationListener
import com.badlogicgames.superjumper.framework.SuperJumper
import org.robovm.apple.coregraphics.CGSize
import org.robovm.apple.foundation.Foundation
import org.robovm.apple.foundation.NSNotification
import org.robovm.apple.foundation.NSNotificationCenter
import org.robovm.apple.uikit.*
import org.robovm.objc.Selector
import org.robovm.objc.annotation.CustomClass
import org.robovm.objc.annotation.Method

@CustomClass("RoboGameContainer")
class RoboGameContainer @Method(selector = "init") constructor() : UIViewController() {

    lateinit var config: IOSApplicationConfiguration
    lateinit var listener: ApplicationListener
    lateinit var app: RoboApplication
    lateinit var frameworkId: String

    constructor(
        listener: ApplicationListener,
        config: IOSApplicationConfiguration,
        frameworkId: String
    ) : this() {
        this.listener = listener
        this.config = config
        this.frameworkId = frameworkId
    }

    override fun loadView() {
        Foundation.log("RoboGameContainer.loadView called")
        view = UIView()
    }

    private fun isInitialised() = ::app.isInitialized

    override fun viewWillLayoutSubviews() {
        if (isInitialised()) {
            Foundation.log("RoboGameContainer.viewWillLayoutSubviews call skipped. game already initialised")
            return
        }

        Foundation.log("RoboGameContainer.viewWillLayoutSubviews called")

        app = RoboApplication(listener, config)

        val uiApp = UIApplication.getSharedApplication()
        uiApp.delegate
        app.initialise(uiApp, view.bounds, frameworkId)

        val gdxViewController = app.uiViewController
        addChildViewController(gdxViewController)

        gdxViewController.view.frame = this.view.bounds

        view.addSubview(gdxViewController.view)
        gdxViewController.didMoveToParentViewController(this)

//        app.willEnterForeground()
        Foundation.log("RoboGameContainer.viewWillLayoutSubviews finished")

        //setup lifecycle observers
        //removing the observers isn't required if targeting >ios9
        NSNotificationCenter.getDefaultCenter().addObserver(
            this,
            Selector.register("viewWillDisappear:"),
            UIApplication.WillResignActiveNotification(),
            null
        )

        NSNotificationCenter.getDefaultCenter().addObserver(
            this,
            Selector.register("viewDidDisappear:"),
            UIApplication.WillTerminateNotification(),
            null
        )

        NSNotificationCenter.getDefaultCenter().addObserver(
            this,
            Selector.register("viewDidAppear:"),
            UIApplication.DidBecomeActiveNotification(),
            null
        )

        NSNotificationCenter.getDefaultCenter().addObserver(
            this,
            Selector.register("viewWillAppear:"),
            UIApplication.WillEnterForegroundNotification(),
            null
        )
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        if (isInitialised()) {
            app.debug("RoboGameContainer", "viewWillAppear called")
            app.willEnterForeground()
//            app.uiViewController.viewWillAppear(animated)
        }
    }

    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        if (isInitialised()) {
            app.debug("RoboGameContainer", "viewDidAppear called")
            app.didBecomeActive()
//            app.uiViewController.viewDidAppear(animated)
        }
    }

    override fun viewWillTransitionToSize(
        size: CGSize?,
        coordinator: UIViewControllerTransitionCoordinator?
    ) {
        super.viewWillTransitionToSize(size, coordinator)
        if (isInitialised()) {
            app.debug("RoboGameContainer", "viewWillTransitionToSize called")
        } else {
            Foundation.log("RoboGameContainer.viewWillTransitionToSize called")
        }
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        if (isInitialised()) {
            app.debug("RoboGameContainer", "viewWillDisappear called")

            app.uiViewController.viewWillDisappear(animated)
//            app.willResignActive()
        }
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        if (isInitialised()) {
            app.debug("RoboGameContainer", "viewDidDisappear called")

            app.uiViewController.viewDidDisappear(animated)
//            app.willTerminate()
        }
    }

    override fun shouldAutorotate(): Boolean {
        Foundation.log("RoboGameContainer.shouldAutorotate called")
        return false
    }


    companion object {
        @JvmStatic
        @Method(selector = "newInstance:classname:")
        fun newInstance(frameworkId: String, classname: String): RoboGameContainer {
            val config = IOSApplicationConfiguration()
            val listener = instantiate(classname) as ApplicationListener
            return RoboGameContainer(listener, config, frameworkId)
        }

        private fun instantiate(clazz: String): Any {
            val gameClass = Class.forName(clazz)
            val constructor = gameClass.getConstructor()
            return constructor.newInstance()
        }
    }
}
