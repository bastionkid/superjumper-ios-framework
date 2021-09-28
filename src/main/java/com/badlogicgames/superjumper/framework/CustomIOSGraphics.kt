package com.badlogicgames.superjumper.framework

import com.badlogic.gdx.*
import com.badlogicgames.superjumper.framework.CustomIOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSInput
import org.robovm.apple.foundation.NSObject
import com.badlogicgames.superjumper.framework.CustomIOSApplicationConfiguration
import com.badlogicgames.superjumper.framework.CustomDefaultIOSInput
import com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometer
import com.badlogic.gdx.backends.iosrobovm.custom.UIAcceleration
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.Input.TextInputListener
import com.badlogic.gdx.Input.OnscreenKeyboardType
import org.robovm.apple.foundation.NSRange
import org.robovm.apple.coregraphics.CGRect
import org.robovm.objc.block.VoidBlock1
import org.robovm.apple.audiotoolbox.AudioServices
import com.badlogic.gdx.Input.Peripheral
import org.robovm.apple.foundation.Foundation
import org.robovm.apple.gamecontroller.GCKeyboard
import com.badlogic.gdx.utils.GdxRuntimeException
import org.robovm.apple.foundation.NSExtensions
import org.robovm.rt.bro.annotation.MachineSizedUInt
import com.badlogic.gdx.backends.iosrobovm.IOSScreenBounds
import org.robovm.apple.coregraphics.CGPoint
import com.badlogic.gdx.graphics.glutils.HdpiMode
import com.badlogic.gdx.backends.iosrobovm.IOSViewControllerListener
import com.badlogicgames.superjumper.framework.CustomIOSGraphics
import com.badlogic.gdx.backends.iosrobovm.IOSAudio
import com.badlogicgames.superjumper.framework.CustomIOSNet
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationLogger
import org.robovm.rt.bro.Bro
import com.badlogic.gdx.backends.iosrobovm.IOSFiles
import com.badlogicgames.superjumper.framework.CustomOALIOSAudio
import com.badlogicgames.superjumper.framework.CustomIOSUIViewController
import com.badlogic.gdx.backends.iosrobovm.objectal.OALAudioSession
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio
import com.badlogic.gdx.Application.ApplicationType
import org.robovm.apple.foundation.NSProcessInfo
import org.robovm.apple.foundation.NSMutableDictionary
import org.robovm.apple.foundation.NSString
import com.badlogic.gdx.backends.iosrobovm.IOSPreferences
import org.robovm.apple.glkit.GLKViewDrawableColorFormat
import org.robovm.apple.glkit.GLKViewDrawableDepthFormat
import org.robovm.apple.glkit.GLKViewDrawableStencilFormat
import org.robovm.apple.glkit.GLKViewDrawableMultisample
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
import com.badlogic.gdx.graphics.Cursor.SystemCursor
import org.robovm.apple.glkit.GLKViewDelegate
import org.robovm.apple.glkit.GLKViewControllerDelegate
import org.robovm.apple.opengles.EAGLRenderingAPI
import com.badlogic.gdx.backends.iosrobovm.IOSGLES30
import com.badlogic.gdx.backends.iosrobovm.custom.HWMachine
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.Pixmap
import org.robovm.apple.uikit.*
import org.robovm.objc.annotation.Method
import org.robovm.rt.bro.annotation.Pointer

class CustomIOSGraphics(
    app: CustomIOSApplication,
    var config: CustomIOSApplicationConfiguration,
    input: IOSInput?,
    useGLES30: Boolean
) : AbstractGraphics() {
    var app: CustomIOSApplication
    var input: IOSInput?
    var gl20: GL20? = null
    var gl30: GL30? = null
    @JvmField
    var screenBounds: IOSScreenBounds?
    private var safeInsetLeft = 0
    private var safeInsetTop = 0
    private var safeInsetBottom = 0
    private var safeInsetRight = 0
    var lastFrameTime: Long
    private var deltaTime = 0f
    var framesStart: Long
    var frames = 0
    var fps = 0
    private var bufferFormat: BufferFormat
    var extensions: String? = null
    private var ppiX = 0f
    private var ppiY = 0f
    private var ppcX = 0f
    private var ppcY = 0f
    private var density = 1f

    @Volatile
    var resume = false

    @Volatile
    var appPaused: Boolean
    private var frameId: Long = -1
    private var isContinuous = true
    private var isFrameRequested = true
    var context: EAGLContext? = null
    var glVersion: GLVersion? = null
    var view: GLKView
    var viewController: CustomIOSUIViewController?
    fun resume() {
        if (!appPaused) return
        appPaused = false
        val listeners = app.lifecycleListeners
        synchronized(listeners!!) {
            for (listener in listeners) {
                listener!!.resume()
            }
        }
        resume = true
        app.listener.resume()
    }

    fun pause() {
        if (appPaused) return
        appPaused = true
        val listeners = app.lifecycleListeners
        synchronized(listeners!!) {
            for (listener in listeners) {
                listener!!.pause()
            }
        }
        app.listener.pause()
    }

    @JvmField
    var created = false
    fun draw(view: GLKView?, rect: CGRect?) {
        makeCurrent()
        // massive hack, GLKView resets the viewport on each draw call, so IOSGLES20
        // stores the last known viewport and we reset it here...
        gl20!!.glViewport(IOSGLES20.x, IOSGLES20.y, IOSGLES20.width, IOSGLES20.height)
        if (!created) {
            // OpenGL glViewport() function expects backbuffer coordinates instead of logical coordinates
            gl20!!.glViewport(0, 0, screenBounds!!.backBufferWidth, screenBounds!!.backBufferHeight)
            val versionString = gl20!!.glGetString(GL20.GL_VERSION)
            val vendorString = gl20!!.glGetString(GL20.GL_VENDOR)
            val rendererString = gl20!!.glGetString(GL20.GL_RENDERER)
            glVersion = GLVersion(ApplicationType.iOS, versionString, vendorString, rendererString)
            updateSafeInsets()
            app.listener.create()
            app.listener.resize(width, height)
            created = true
        }
        if (appPaused) {
            return
        }
        val time = System.nanoTime()
        if (!resume) {
            deltaTime = (time - lastFrameTime) / 1000000000.0f
        } else {
            resume = false
            deltaTime = 0f
        }
        lastFrameTime = time
        frames++
        if (time - framesStart >= 1000000000L) {
            framesStart = time
            fps = frames
            frames = 0
        }
        input!!.processEvents()
        frameId++
        app.listener.render()
    }

    fun makeCurrent() {
        EAGLContext.setCurrentContext(context)
    }

    fun update(controller: GLKViewController?) {
        makeCurrent()
        app.processRunnables()
        // pause the GLKViewController render loop if we are no longer continuous
        // and if we haven't requested a frame in the last loop iteration
        if (!isContinuous && !isFrameRequested) {
            viewController!!.isPaused = true
        }
        isFrameRequested = false
    }

    fun willPause(controller: GLKViewController?, pause: Boolean) {}
    override fun getGL20(): GL20 {
        return gl20!!
    }

    override fun setGL20(gl20: GL20) {
        this.gl20 = gl20
        if (gl30 == null) {
            Gdx.gl = gl20
            Gdx.gl20 = gl20
        }
    }

    override fun isGL30Available(): Boolean {
        return gl30 != null
    }

    override fun getGL30(): GL30 {
        return gl30!!
    }

    override fun setGL30(gl30: GL30) {
        this.gl30 = gl30
        if (gl30 != null) {
            gl20 = gl30
            Gdx.gl = gl20
            Gdx.gl20 = gl20
            Gdx.gl30 = gl30
        }
    }

    override fun getWidth(): Int {
        return if (config.hdpiMode == HdpiMode.Pixels) {
            backBufferWidth
        } else {
            screenBounds!!.width
        }
    }

    override fun getHeight(): Int {
        return if (config.hdpiMode == HdpiMode.Pixels) {
            backBufferHeight
        } else {
            screenBounds!!.height
        }
    }

    override fun getBackBufferWidth(): Int {
        return screenBounds!!.backBufferWidth
    }

    override fun getBackBufferHeight(): Int {
        return screenBounds!!.backBufferHeight
    }

    override fun getBackBufferScale(): Float {
        return app.pixelsPerPoint
    }

    override fun getDeltaTime(): Float {
        return deltaTime
    }

    override fun getFramesPerSecond(): Int {
        return fps
    }

    override fun getType(): GraphicsType {
        return GraphicsType.iOSGL
    }

    override fun getGLVersion(): GLVersion {
        return glVersion!!
    }

    override fun getPpiX(): Float {
        return ppiX
    }

    override fun getPpiY(): Float {
        return ppiY
    }

    override fun getPpcX(): Float {
        return ppcX
    }

    override fun getPpcY(): Float {
        return ppcY
    }

    override fun getDensity(): Float {
        return density
    }

    override fun supportsDisplayModeChange(): Boolean {
        return false
    }

    override fun getDisplayModes(): Array<Graphics.DisplayMode> {
        return arrayOf(displayMode)
    }

    override fun getDisplayMode(): Graphics.DisplayMode {
        return IOSDisplayMode(
            width, height, config.preferredFramesPerSecond, bufferFormat.r + bufferFormat.g
                    + bufferFormat.b + bufferFormat.a
        )
    }

    override fun getPrimaryMonitor(): Graphics.Monitor {
        return IOSMonitor(0, 0, "Primary Monitor")
    }

    override fun getMonitor(): Graphics.Monitor {
        return primaryMonitor
    }

    override fun getMonitors(): Array<Graphics.Monitor> {
        return arrayOf(primaryMonitor)
    }

    override fun getDisplayModes(monitor: Graphics.Monitor): Array<Graphics.DisplayMode> {
        return displayModes
    }

    override fun getDisplayMode(monitor: Graphics.Monitor): Graphics.DisplayMode {
        return displayMode
    }

    fun updateSafeInsets() {
        safeInsetTop = 0
        safeInsetLeft = 0
        safeInsetRight = 0
        safeInsetBottom = 0
        if (Foundation.getMajorSystemVersion() >= 11) {
            val edgeInsets = viewController!!.view.safeAreaInsets
            safeInsetTop = edgeInsets.top.toInt()
            safeInsetLeft = edgeInsets.left.toInt()
            safeInsetRight = edgeInsets.right.toInt()
            safeInsetBottom = edgeInsets.bottom.toInt()
            if (config.hdpiMode == HdpiMode.Pixels) {
                safeInsetTop *= app.pixelsPerPoint.toInt()
                safeInsetLeft *= app.pixelsPerPoint.toInt()
                safeInsetRight *= app.pixelsPerPoint.toInt()
                safeInsetBottom *= app.pixelsPerPoint.toInt()
            }
        }
    }

    override fun getSafeInsetLeft(): Int {
        return safeInsetLeft
    }

    override fun getSafeInsetTop(): Int {
        return safeInsetTop
    }

    override fun getSafeInsetBottom(): Int {
        return safeInsetBottom
    }

    override fun getSafeInsetRight(): Int {
        return safeInsetRight
    }

    override fun setFullscreenMode(displayMode: Graphics.DisplayMode): Boolean {
        return false
    }

    override fun setWindowedMode(width: Int, height: Int): Boolean {
        return false
    }

    override fun setTitle(title: String) {}
    override fun setUndecorated(undecorated: Boolean) {}
    override fun setResizable(resizable: Boolean) {}
    override fun setVSync(vsync: Boolean) {}

    /** Sets the preferred framerate for the application. Default is 60. Is not generally advised to be used on mobile platforms.
     *
     * @param fps the preferred fps
     */
    override fun setForegroundFPS(fps: Int) {
        viewController!!.preferredFramesPerSecond = fps.toLong()
    }

    override fun getBufferFormat(): BufferFormat {
        return bufferFormat
    }

    override fun supportsExtension(extension: String): Boolean {
        if (extensions == null) extensions = Gdx.gl.glGetString(GL20.GL_EXTENSIONS)
        return extensions!!.contains(extension)
    }

    override fun setContinuousRendering(isContinuous: Boolean) {
        if (isContinuous != this.isContinuous) {
            this.isContinuous = isContinuous
            // start the GLKViewController if we go from non-continuous -> continuous
            if (isContinuous) viewController!!.isPaused = false
        }
    }

    override fun isContinuousRendering(): Boolean {
        return isContinuous
    }

    override fun requestRendering() {
        isFrameRequested = true
        // start the GLKViewController if we are in non-continuous mode
        // (we should already be started in continuous mode)
        if (!isContinuous) viewController!!.isPaused = false
    }

    override fun isFullscreen(): Boolean {
        return true
    }

    override fun getFrameId(): Long {
        return frameId
    }

    override fun newCursor(pixmap: Pixmap, xHotspot: Int, yHotspot: Int): Cursor? {
        return null
    }

    override fun setCursor(cursor: Cursor) {}
    override fun setSystemCursor(systemCursor: SystemCursor) {}
    private inner class IOSViewDelegate : NSObject(), GLKViewDelegate, GLKViewControllerDelegate {
        override fun update(controller: GLKViewController) {
            this@CustomIOSGraphics.update(controller)
        }

        override fun willPause(controller: GLKViewController, pause: Boolean) {
            this@CustomIOSGraphics.willPause(controller, pause)
        }

        override fun draw(view: GLKView, rect: CGRect) {
            this@CustomIOSGraphics.draw(view, rect)
        }
    }

    private inner class IOSDisplayMode(
        width: Int,
        height: Int,
        refreshRate: Int,
        bitsPerPixel: Int
    ) : Graphics.DisplayMode(width, height, refreshRate, bitsPerPixel)

    private inner class IOSMonitor(virtualX: Int, virtualY: Int, name: String?) :
        Graphics.Monitor(virtualX, virtualY, name)

    companion object {
        private const val tag = "IOSGraphics"
    }

    init {

        // setup view and OpenGL
        screenBounds = app.computeBounds()
        if (useGLES30) {
            context = EAGLContext(EAGLRenderingAPI.OpenGLES3)
            if (context != null) {
                gl30 = IOSGLES30()
                gl20 = gl30
            } else Gdx.app.log("IOGraphics", "OpenGL ES 3.0 not supported, falling back on 2.0")
        }
        if (context == null) {
            context = EAGLContext(EAGLRenderingAPI.OpenGLES2)
            gl20 = IOSGLES20()
            gl30 = null
        }
        this.input = input
        val viewDelegate: IOSViewDelegate = IOSViewDelegate()
        view = object : GLKView(
            CGRect(0.0, 0.0, screenBounds!!.width.toDouble(), screenBounds!!.height.toDouble()),
            context
        ) {
            @Method(selector = "touchesBegan:withEvent:")
            fun touchesBegan(@Pointer touches: Long, event: UIEvent?) {
                this@CustomIOSGraphics.input!!.onTouch(touches)
            }

            @Method(selector = "touchesCancelled:withEvent:")
            fun touchesCancelled(@Pointer touches: Long, event: UIEvent?) {
                this@CustomIOSGraphics.input!!.onTouch(touches)
            }

            @Method(selector = "touchesEnded:withEvent:")
            fun touchesEnded(@Pointer touches: Long, event: UIEvent?) {
                this@CustomIOSGraphics.input!!.onTouch(touches)
            }

            @Method(selector = "touchesMoved:withEvent:")
            fun touchesMoved(@Pointer touches: Long, event: UIEvent?) {
                this@CustomIOSGraphics.input!!.onTouch(touches)
            }

            override fun draw(rect: CGRect) {
                this@CustomIOSGraphics.draw(this, rect)
            }
        }
        view.delegate = viewDelegate
        view.drawableColorFormat = config.colorFormat
        view.drawableDepthFormat = config.depthFormat
        view.drawableStencilFormat = config.stencilFormat
        view.drawableMultisample = config.multisample
        view.isMultipleTouchEnabled = true
        viewController = app.createUIViewController(this)
        viewController!!.view = view
        viewController!!.delegate = viewDelegate
        viewController!!.preferredFramesPerSecond = config.preferredFramesPerSecond.toLong()
        this.app = app
        var r = 0
        var g = 0
        var b = 0
        var a = 0
        var depth = 0
        var stencil = 0
        var samples = 0
        if (config.colorFormat == GLKViewDrawableColorFormat.RGB565) {
            r = 5
            g = 6
            b = 5
            a = 0
        } else {
            a = 8
            b = a
            g = b
            r = g
        }
        depth = if (config.depthFormat == GLKViewDrawableDepthFormat._16) {
            16
        } else if (config.depthFormat == GLKViewDrawableDepthFormat._24) {
            24
        } else {
            0
        }
        if (config.stencilFormat == GLKViewDrawableStencilFormat._8) {
            stencil = 8
        }
        if (config.multisample == GLKViewDrawableMultisample._4X) {
            samples = 4
        }
        bufferFormat = BufferFormat(r, g, b, a, depth, stencil, samples, false)
        val machineString = HWMachine.getMachineString()
        val device = config.knownDevices.get(machineString)
        if (device == null) app.error(
            tag,
            "Machine ID: $machineString not found, please report to LibGDX"
        )
        val ppi = device?.ppi ?: app.guessUnknownPpi()
        density = ppi / 160f
        ppiX = ppi.toFloat()
        ppiY = ppi.toFloat()
        ppcX = ppiX / 2.54f
        ppcY = ppiY / 2.54f
        app.debug(tag, "Display: ppi=$ppi, density=$density")

        // time + FPS
        lastFrameTime = System.nanoTime()
        framesStart = lastFrameTime
        appPaused = false
    }
}