package com.badlogicgames.superjumper.framework

import com.badlogic.gdx.*
import com.badlogicgames.superjumper.framework.CustomIOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSInput
import org.robovm.apple.foundation.NSObject
import com.badlogicgames.superjumper.framework.CustomIOSApplicationConfiguration
import com.badlogicgames.superjumper.framework.CustomDefaultIOSInput
import org.robovm.apple.uikit.UIDevice
import org.robovm.apple.uikit.UIForceTouchCapability
import org.robovm.apple.uikit.UIScreen
import com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometer
import com.badlogic.gdx.backends.iosrobovm.custom.UIAcceleration
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.Input.TextInputListener
import com.badlogic.gdx.Input.OnscreenKeyboardType
import org.robovm.apple.uikit.UIAlertController
import org.robovm.apple.uikit.UITextField
import org.robovm.apple.uikit.UITextFieldDelegate
import org.robovm.apple.uikit.UITextFieldDelegateAdapter
import org.robovm.apple.foundation.NSRange
import org.robovm.apple.uikit.UIKeyboardType
import org.robovm.apple.coregraphics.CGRect
import org.robovm.apple.uikit.UIReturnKeyType
import org.robovm.apple.uikit.UITextAutocapitalizationType
import org.robovm.apple.uikit.UITextAutocorrectionType
import org.robovm.apple.uikit.UITextSpellCheckingType
import org.robovm.apple.uikit.UIAlertControllerStyle
import org.robovm.objc.block.VoidBlock1
import org.robovm.apple.uikit.UIAlertAction
import org.robovm.apple.uikit.UIAlertActionStyle
import org.robovm.apple.audiotoolbox.AudioServices
import com.badlogic.gdx.Input.Peripheral
import org.robovm.apple.foundation.Foundation
import org.robovm.apple.gamecontroller.GCKeyboard
import org.robovm.apple.uikit.UIInterfaceOrientation
import org.robovm.apple.uikit.UIKey
import org.robovm.apple.uikit.UITouchPhase
import com.badlogic.gdx.utils.GdxRuntimeException
import org.robovm.apple.uikit.UITouch
import org.robovm.apple.foundation.NSExtensions
import org.robovm.rt.bro.annotation.MachineSizedUInt
import com.badlogic.gdx.backends.iosrobovm.IOSScreenBounds
import org.robovm.apple.coregraphics.CGPoint
import com.badlogic.gdx.graphics.glutils.HdpiMode
import org.robovm.apple.uikit.UIKeyboardHIDUsage
import org.robovm.apple.uikit.UIApplication
import org.robovm.apple.uikit.UIWindow
import com.badlogic.gdx.backends.iosrobovm.IOSViewControllerListener
import com.badlogicgames.superjumper.framework.CustomIOSGraphics
import com.badlogic.gdx.backends.iosrobovm.IOSAudio
import com.badlogicgames.superjumper.framework.CustomIOSNet
import org.robovm.apple.uikit.UIApplicationLaunchOptions
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationLogger
import org.robovm.rt.bro.Bro
import com.badlogic.gdx.backends.iosrobovm.IOSFiles
import com.badlogicgames.superjumper.framework.CustomOALIOSAudio
import com.badlogicgames.superjumper.framework.CustomIOSUIViewController
import org.robovm.apple.uikit.UIUserInterfaceIdiom
import org.robovm.apple.uikit.UIViewController
import com.badlogic.gdx.backends.iosrobovm.objectal.OALAudioSession
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio
import com.badlogic.gdx.Application.ApplicationType
import org.robovm.apple.foundation.NSProcessInfo
import org.robovm.apple.foundation.NSMutableDictionary
import org.robovm.apple.foundation.NSString
import com.badlogic.gdx.backends.iosrobovm.IOSPreferences
import org.robovm.apple.uikit.UIPasteboard
import org.robovm.apple.glkit.GLKViewDrawableColorFormat
import org.robovm.apple.glkit.GLKViewDrawableDepthFormat
import org.robovm.apple.glkit.GLKViewDrawableStencilFormat
import org.robovm.apple.glkit.GLKViewDrawableMultisample
import org.robovm.apple.uikit.UIRectEdge
import com.badlogic.gdx.utils.ObjectMap
import com.badlogicgames.superjumper.framework.CustomIOSDevice
import com.badlogic.gdx.Graphics.BufferFormat
import kotlin.jvm.Volatile
import org.robovm.apple.opengles.EAGLContext
import com.badlogic.gdx.graphics.glutils.GLVersion
import org.robovm.apple.glkit.GLKView
import com.badlogic.gdx.backends.iosrobovm.IOSGLES20
import org.robovm.apple.glkit.GLKViewController
import com.badlogic.gdx.Graphics.GraphicsType
import org.robovm.apple.uikit.UIEdgeInsets
import com.badlogic.gdx.graphics.Cursor.SystemCursor
import org.robovm.apple.glkit.GLKViewDelegate
import org.robovm.apple.glkit.GLKViewControllerDelegate
import org.robovm.apple.opengles.EAGLRenderingAPI
import com.badlogic.gdx.backends.iosrobovm.IOSGLES30
import com.badlogic.gdx.backends.iosrobovm.custom.HWMachine
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Clipboard
import java.io.File

class CustomIOSApplication(
    var listener: ApplicationListener,
    var config: CustomIOSApplicationConfiguration
) : Application {
    @JvmField
    var uiApp: UIApplication? = null

    /** Return the UI Window of IOSApplication
     * @return the window
     */
    var uIWindow: UIWindow? = null
    @JvmField
    var viewControllerListener: IOSViewControllerListener? = null
    var graphics: CustomIOSGraphics? = null
    var audio: IOSAudio? = null
    private var files: Files? = null
    @JvmField
    var input: IOSInput? = null
    var net: CustomIOSNet? = null
    private var logLevel = Application.LOG_DEBUG
    private var applicationLogger: ApplicationLogger? = null

    /** The display scale factor (1.0f for normal; 2.0f to use retina coordinates/dimensions).  */
    var pixelsPerPoint = 0f
    private var lastScreenBounds: IOSScreenBounds? = null
    var runnables = Array<Runnable>()
    var executedRunnables = Array<Runnable>()
    var lifecycleListeners = Array<LifecycleListener>()
    fun didFinishLaunching(uiApp: UIApplication, options: UIApplicationLaunchOptions?): Boolean {
        setApplicationLogger(IOSApplicationLogger())
        Gdx.app = this
        this.uiApp = uiApp

        // enable or disable screen dimming
        uiApp.isIdleTimerDisabled = config.preventScreenDimming
        Gdx.app.debug("IOSApplication", "iOS version: " + UIDevice.getCurrentDevice().systemVersion)
        Gdx.app.debug(
            "IOSApplication",
            "Running in " + (if (Bro.IS_64BIT) "64-bit" else "32-bit") + " mode"
        )

        // iOS counts in "points" instead of pixels. Points are logical pixels
        pixelsPerPoint = UIScreen.getMainScreen().nativeScale.toFloat()
        Gdx.app.debug("IOSApplication", "Pixels per point: $pixelsPerPoint")
        uIWindow = UIWindow(UIScreen.getMainScreen().bounds)
        uIWindow!!.makeKeyAndVisible()
        uiApp.delegate.window = uIWindow

        // setup libgdx
        input = createInput()
        graphics = createGraphics()
        Gdx.gl20 = graphics!!.gl20
        Gdx.gl = Gdx.gl20
        Gdx.gl30 = graphics!!.gl30
        files = createFiles()
        audio = createAudio(config)
        net = CustomIOSNet(this, config)
        Gdx.files = files
        Gdx.graphics = graphics
        Gdx.audio = audio
        Gdx.input = input
        Gdx.net = net
        input!!.setupPeripherals()
        uIWindow!!.rootViewController = graphics!!.viewController
        Gdx.app.debug("IOSApplication", "created")
        return true
    }

    protected fun createFiles(): Files {
        return IOSFiles()
    }

    protected fun createAudio(config: CustomIOSApplicationConfiguration?): IOSAudio {
        return CustomOALIOSAudio(config!!)
    }

    protected fun createGraphics(): CustomIOSGraphics {
        return CustomIOSGraphics(this, config, input, config.useGL30)
    }

    fun createUIViewController(graphics: CustomIOSGraphics?): CustomIOSUIViewController {
        return CustomIOSUIViewController(this, graphics!!)
    }

    protected fun createInput(): IOSInput {
        return CustomDefaultIOSInput(this)
    }

    /** Returns device ppi using a best guess approach when device is unknown. Overwrite to customize strategy.  */
    fun guessUnknownPpi(): Int {
        val ppi: Int
        ppi =
            if (UIDevice.getCurrentDevice().userInterfaceIdiom == UIUserInterfaceIdiom.Pad) 132 * pixelsPerPoint.toInt() else 164 * pixelsPerPoint.toInt()
        error(
            "IOSApplication",
            "Device PPI unknown. PPI value has been guessed to $ppi but may be wrong"
        )
        return ppi
    }

    /** Return the UI view controller of IOSApplication
     * @return the view controller of IOSApplication
     */
    val uIViewController: UIViewController?
        get() = graphics!!.viewController

    /** @see IOSScreenBounds for detailed explanation
     *
     * @return logical dimensions of space we draw to, adjusted for device orientation
     */
    fun computeBounds(): IOSScreenBounds {
        val screenBounds = uIWindow!!.bounds
        val statusBarFrame = uiApp!!.statusBarFrame
        val statusBarHeight = statusBarFrame.height
        val screenWidth = screenBounds.width
        var screenHeight = screenBounds.height
        if (statusBarHeight != 0.0) {
            debug("IOSApplication", "Status bar is visible (height = $statusBarHeight)")
            screenHeight -= statusBarHeight
        } else {
            debug("IOSApplication", "Status bar is not visible")
        }
        val offsetX = 0
        val offsetY = Math.round(statusBarHeight).toInt()
        val width = Math.round(screenWidth).toInt()
        val height = Math.round(screenHeight).toInt()
        val backBufferWidth = Math.round(screenWidth * pixelsPerPoint).toInt()
        val backBufferHeight = Math.round(screenHeight * pixelsPerPoint).toInt()
        debug(
            "IOSApplication",
            "Computed bounds are x=" + offsetX + " y=" + offsetY + " w=" + width + " h=" + height + " bbW= "
                    + backBufferWidth + " bbH= " + backBufferHeight
        )
        return IOSScreenBounds(
            offsetX,
            offsetY,
            width,
            height,
            backBufferWidth,
            backBufferHeight
        ).also { lastScreenBounds = it }
    }

    /** @return area of screen in UIKit points on which libGDX draws, with 0,0 being upper left corner
     */
    val screenBounds: IOSScreenBounds
        get() = if (lastScreenBounds == null) computeBounds() else lastScreenBounds as IOSScreenBounds

    fun didBecomeActive(uiApp: UIApplication?) {
        Gdx.app.debug("IOSApplication", "resumed")
        // workaround for ObjectAL crash problem
        // see: https://groups.google.com/forum/?fromgroups=#!topic/objectal-for-iphone/ubRWltp_i1Q
        val audioSession = OALAudioSession.sharedInstance()
        audioSession?.forceEndInterruption()
        if (config.allowIpod) {
            val audio = OALSimpleAudio.sharedInstance()
            if (audio != null) {
                audio.isUseHardwareIfAvailable = false
            }
        }
        graphics!!.makeCurrent()
        graphics!!.resume()
    }

    fun willEnterForeground(uiApp: UIApplication?) {
        // workaround for ObjectAL crash problem
        // see: https://groups.google.com/forum/?fromgroups=#!topic/objectal-for-iphone/ubRWltp_i1Q
        val audioSession = OALAudioSession.sharedInstance()
        audioSession?.forceEndInterruption()
    }

    fun willResignActive(uiApp: UIApplication?) {
        Gdx.app.debug("IOSApplication", "paused")
        graphics!!.makeCurrent()
        graphics!!.pause()
        Gdx.gl.glFinish()
    }

    fun willTerminate(uiApp: UIApplication?) {
        Gdx.app.debug("IOSApplication", "disposed")
        graphics!!.makeCurrent()
        val listeners = lifecycleListeners
        synchronized(listeners) {
            for (listener in listeners) {
                listener.pause()
            }
        }
        listener.dispose()
        Gdx.gl.glFinish()
    }

    override fun getApplicationListener(): ApplicationListener {
        return listener
    }

    override fun getGraphics(): Graphics {
        return graphics!!
    }

    override fun getAudio(): Audio {
        return audio!!
    }

    override fun getInput(): Input {
        return input!!
    }

    override fun getFiles(): Files {
        return files!!
    }

    override fun getNet(): Net {
        return net!!
    }

    override fun debug(tag: String, message: String) {
        if (logLevel >= Application.LOG_DEBUG) getApplicationLogger().debug(tag, message)
    }

    override fun debug(tag: String, message: String, exception: Throwable) {
        if (logLevel >= Application.LOG_DEBUG) getApplicationLogger().debug(tag, message, exception)
    }

    override fun log(tag: String, message: String) {
        if (logLevel >= Application.LOG_INFO) getApplicationLogger().log(tag, message)
    }

    override fun log(tag: String, message: String, exception: Throwable) {
        if (logLevel >= Application.LOG_INFO) getApplicationLogger().log(tag, message, exception)
    }

    override fun error(tag: String, message: String) {
        if (logLevel >= Application.LOG_ERROR) getApplicationLogger().error(tag, message)
    }

    override fun error(tag: String, message: String, exception: Throwable) {
        if (logLevel >= Application.LOG_ERROR) getApplicationLogger().error(tag, message, exception)
    }

    override fun setLogLevel(logLevel: Int) {
        this.logLevel = logLevel
    }

    override fun getLogLevel(): Int {
        return logLevel
    }

    override fun setApplicationLogger(applicationLogger: ApplicationLogger) {
        this.applicationLogger = applicationLogger
    }

    override fun getApplicationLogger(): ApplicationLogger {
        return applicationLogger!!
    }

    override fun getType(): ApplicationType {
        return ApplicationType.iOS
    }

    override fun getVersion(): Int {
        return NSProcessInfo.getSharedProcessInfo().operatingSystemVersion.majorVersion.toInt()
    }

    override fun getJavaHeap(): Long {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }

    override fun getNativeHeap(): Long {
        return javaHeap
    }

    override fun getPreferences(name: String): Preferences {
        val libraryPath = File(System.getenv("HOME"), "Library")
        val finalPath = File(libraryPath, "$name.plist")
        var nsDictionary = NSMutableDictionary
            .read(finalPath) as NSMutableDictionary<NSString?, NSObject?>

        // if it fails to get an existing dictionary, create a new one.
        if (nsDictionary == null) {
            nsDictionary = NSMutableDictionary()
            nsDictionary.write(finalPath, false)
        }
        return IOSPreferences(nsDictionary, finalPath.absolutePath)
    }

    override fun postRunnable(runnable: Runnable) {
        synchronized(runnables) {
            runnables.add(runnable)
            Gdx.graphics.requestRendering()
        }
    }

    fun processRunnables() {
        synchronized(runnables) {
            executedRunnables.clear()
            executedRunnables.addAll(runnables)
            runnables.clear()
        }
        for (i in 0 until executedRunnables.size) {
            try {
                executedRunnables[i].run()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    override fun exit() {
        System.exit(0)
    }

    override fun getClipboard(): Clipboard {
        return object : Clipboard {
            override fun setContents(content: String) {
                UIPasteboard.getGeneralPasteboard().string = content
            }

            override fun hasContents(): Boolean {
                if (Foundation.getMajorSystemVersion() >= 10) {
                    return UIPasteboard.getGeneralPasteboard().hasStrings()
                }
                val contents: String? = contents
                return contents != null && !contents.isEmpty()
            }

            override fun getContents(): String {
                return UIPasteboard.getGeneralPasteboard().string
            }
        }
    }

    override fun addLifecycleListener(listener: LifecycleListener) {
        synchronized(lifecycleListeners) { lifecycleListeners.add(listener) }
    }

    override fun removeLifecycleListener(listener: LifecycleListener) {
        synchronized(lifecycleListeners) { lifecycleListeners.removeValue(listener, true) }
    }

    /** Add a listener to handle events from the libgdx root view controller
     * @param listener The {#link IOSViewControllerListener} to add
     */
    fun addViewControllerListener(listener: IOSViewControllerListener?) {
        viewControllerListener = listener
    }
}