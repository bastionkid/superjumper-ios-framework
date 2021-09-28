package com.badlogicgames.superjumper.framework

import com.badlogicgames.superjumper.framework.Assets.playSound
import com.badlogicgames.superjumper.framework.CustomIOSApplication
import com.badlogicgames.superjumper.framework.CustomIOSApplicationConfiguration
import com.badlogic.gdx.net.NetJavaImpl
import org.robovm.apple.uikit.UIApplication
import com.badlogic.gdx.Net.HttpResponseListener
import com.badlogic.gdx.net.ServerSocketHints
import com.badlogic.gdx.net.NetJavaServerSocketImpl
import com.badlogic.gdx.net.SocketHints
import com.badlogic.gdx.net.NetJavaSocketImpl
import org.robovm.apple.foundation.NSURL
import com.badlogicgames.superjumper.framework.CustomIOSGraphics
import org.robovm.apple.glkit.GLKViewController
import org.robovm.apple.uikit.UIInterfaceOrientationMask
import org.robovm.apple.uikit.UIInterfaceOrientation
import org.robovm.apple.uikit.UIRectEdge
import com.badlogic.gdx.backends.iosrobovm.IOSScreenBounds
import com.badlogic.gdx.graphics.glutils.HdpiMode
import org.robovm.apple.foundation.NSSet
import org.robovm.apple.uikit.UIPress
import org.robovm.apple.uikit.UIPressesEvent
import org.robovm.objc.annotation.BindSelector
import com.badlogic.gdx.backends.iosrobovm.IOSUIViewController
import com.badlogic.gdx.backends.iosrobovm.IOSAudio
import com.badlogic.gdx.audio.AudioDevice
import com.badlogic.gdx.audio.AudioRecorder
import com.badlogic.gdx.backends.iosrobovm.IOSSound
import com.badlogic.gdx.backends.iosrobovm.objectal.OALAudioTrack
import com.badlogic.gdx.backends.iosrobovm.IOSMusic
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.Application.ApplicationType
import org.robovm.objc.Selector
import org.robovm.rt.bro.ValuedEnum.AsMachineSizedSIntMarshaler
import org.robovm.rt.bro.annotation.Callback
import org.robovm.rt.bro.annotation.Marshaler

class CustomIOSUIViewController constructor(
    val app: CustomIOSApplication,
    val graphics: CustomIOSGraphics
) : GLKViewController() {
    override fun viewWillAppear(arg0: Boolean) {
        super.viewWillAppear(arg0)
        // start GLKViewController even though we may only draw a single frame
        // (we may be in non-continuous mode)
        isPaused = false
    }

    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        if (app.viewControllerListener != null) app.viewControllerListener!!.viewDidAppear(animated)
    }

    override fun getSupportedInterfaceOrientations(): UIInterfaceOrientationMask {
        var mask: Long = 0
        if (app.config.orientationLandscape) {
            mask = mask or (1 shl UIInterfaceOrientation.LandscapeLeft.value()
                .toInt() or (1 shl UIInterfaceOrientation.LandscapeRight.value()
                .toInt())).toLong()
        }
        if (app.config.orientationPortrait) {
            mask = mask or (1 shl UIInterfaceOrientation.Portrait.value()
                .toInt() or (1 shl UIInterfaceOrientation.PortraitUpsideDown.value()
                .toInt())).toLong()
        }
        return UIInterfaceOrientationMask(mask)
    }

    override fun shouldAutorotate(): Boolean {
        return true
    }

//    @org.robovm.rt.bro.annotation.Marshaler(AsMachineSizedSIntMarshaler::class)
//    fun shouldAutorotateToInterfaceOrientation(@org.robovm.rt.bro.annotation.Marshaler(AsMachineSizedSIntMarshaler::class) orientation: UIInterfaceOrientation?): Boolean {
//        // we return "true" if we support the orientation
//        return when (orientation) {
//            UIInterfaceOrientation.LandscapeLeft, UIInterfaceOrientation.LandscapeRight -> app.config.orientationLandscape
//            else ->                 // assume portrait
//                app.config.orientationPortrait
//        }
//    }

    override fun getPreferredScreenEdgesDeferringSystemGestures(): UIRectEdge {
        return app.config.screenEdgesDeferringSystemGestures
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        // get the view size and update graphics
        val oldBounds = graphics.screenBounds
        val newBounds = app.computeBounds()
        graphics.screenBounds = newBounds
        // Layout may happen without bounds changing, don't trigger resize in that case
        if (graphics.created && (newBounds.width != oldBounds!!.width || newBounds.height != oldBounds.height)) {
            graphics.makeCurrent()
            graphics.updateSafeInsets()
            if (graphics.config.hdpiMode == HdpiMode.Pixels) {
                app.listener.resize(newBounds.backBufferWidth, newBounds.backBufferHeight)
            } else {
                app.listener.resize(newBounds.width, newBounds.height)
            }
        }
    }

    override fun prefersStatusBarHidden(): Boolean {
        return !app.config.statusBarVisible
    }

    override fun prefersHomeIndicatorAutoHidden(): Boolean {
        return app.config.hideHomeIndicator
    }

    override fun pressesBegan(presses: NSSet<UIPress>, event: UIPressesEvent) {
        if (presses == null || presses.isEmpty() || !app.input!!.onKey(
                presses.values.first().key,
                true
            )
        ) {
            super.pressesBegan(presses, event)
        }
    }

    override fun pressesEnded(presses: NSSet<UIPress>, event: UIPressesEvent) {
        if (presses == null || presses.isEmpty() || !app.input!!.onKey(
                presses.values.first().key,
                false
            )
        ) {
            super.pressesEnded(presses, event)
        }
    }

//    companion object {
//        @Callback
//        @BindSelector("shouldAutorotateToInterfaceOrientation:")
//        private @org.robovm.rt.bro.annotation.Marshaler(AsMachineSizedSIntMarshaler::class) fun shouldAutorotateToInterfaceOrientation(
//            @org.robovm.rt.bro.annotation.Marshaler(AsMachineSizedSIntMarshaler::class) self: CustomIOSUIViewController,
//            sel: Selector,
//            @org.robovm.rt.bro.annotation.Marshaler(AsMachineSizedSIntMarshaler::class) orientation: UIInterfaceOrientation
//        ): Boolean {
//            return self.shouldAutorotateToInterfaceOrientation(orientation)
//        }
//    }
}