package com.badlogicgames.superjumper.framework

import com.badlogicgames.superjumper.framework.CustomIOSApplication
import com.badlogic.gdx.AbstractInput
import com.badlogic.gdx.backends.iosrobovm.IOSInput
import org.robovm.apple.foundation.NSObject
import com.badlogicgames.superjumper.framework.CustomIOSApplicationConfiguration
import com.badlogicgames.superjumper.framework.CustomDefaultIOSInput
import com.badlogic.gdx.InputProcessor
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
import com.badlogic.gdx.Gdx
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
import com.badlogic.gdx.ApplicationLogger
import com.badlogic.gdx.LifecycleListener
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
import com.badlogic.gdx.AbstractGraphics
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

class CustomIOSApplicationConfiguration {
    /** whether to enable screen dimming.  */
    var preventScreenDimming = true

    /** whether or not portrait orientation is supported.  */
    @JvmField
    var orientationPortrait = true

    /** whether or not landscape orientation is supported.  */
    @JvmField
    var orientationLandscape = true

    /** the color format, RGBA8888 is the default  */
    var colorFormat = GLKViewDrawableColorFormat.RGBA8888

    /** the depth buffer format, Format16 is default  */
    var depthFormat = GLKViewDrawableDepthFormat._16

    /** the stencil buffer format, None is default  */
    var stencilFormat = GLKViewDrawableStencilFormat.None

    /** the multisample format, None is default  */
    var multisample = GLKViewDrawableMultisample.None

    /** number of frames per second, 60 is default  */
    var preferredFramesPerSecond = 60

    /** whether to use the accelerometer, default true  */
    var useAccelerometer = true

    /** the update interval to poll the accelerometer with, in seconds  */
    var accelerometerUpdate = 0.05f

    /** the update interval to poll the magnetometer with, in seconds  */
    var magnetometerUpdate = 0.05f

    /** whether to use the compass, default true  */
    var useCompass = true

    /** whether or not to allow background music from iPod  */
    @JvmField
    var allowIpod = true

    /** whether or not the onScreenKeyboard should be closed on return key  */
    var keyboardCloseOnReturn = true

    /** Experimental, whether to enable OpenGL ES 3 if supported. If not supported it will fall-back to OpenGL ES 2.0.
     * When GL ES 3 is enabled, [com.badlogic.gdx.Gdx.gl30] can be used to access it's functionality.
     */
    @Deprecated("this option is currently experimental and not yet fully supported, expect issues. ")
    var useGL30 = false

    /** whether the status bar should be visible or not  */
    @JvmField
    var statusBarVisible = false

    /** whether the home indicator should be hidden or not  */
    @JvmField
    var hideHomeIndicator = true

    /** Whether to override the ringer/mute switch, see https://github.com/libgdx/libgdx/issues/4430  */
    @JvmField
    var overrideRingerSwitch = false

    /** Edges where app gestures must be fired over system gestures.
     * Prior to iOS 11, UIRectEdge.All was default behaviour if status bar hidden, see https://github.com/libgdx/libgdx/issues/5110  */
    @JvmField
    var screenEdgesDeferringSystemGestures = UIRectEdge.None

    /** The maximum number of threads to use for network requests. Default is [Integer.MAX_VALUE].  */
    @JvmField
    var maxNetThreads = Int.MAX_VALUE

    /** whether to use audio or not. Default is `true`  */
    @JvmField
    var useAudio = true

    /**
     * This setting allows you to specify whether you want to work in logical or raw pixel units.
     * See [HdpiMode] for more information. Note that some OpenGL
     * functions like [GL20.glViewport] and
     * [GL20.glScissor] require raw pixel units. Use
     * [HdpiUtils] to help with the conversion if HdpiMode is set to
     * [HdpiMode.Logical]. Defaults to [HdpiMode.Logical].
     */
    @JvmField
    var hdpiMode = HdpiMode.Logical
    var knownDevices: ObjectMap<String?, CustomIOSDevice?> =
        CustomIOSDevice.Companion.populateWithKnownDevices()

    /**
     * adds device information for newer iOS devices, or overrides information for given ones
     * @param classifier human readable device classifier
     * @param machineString machine string returned by iOS
     * @param ppi device's pixel per inch value
     */
    fun addIosDevice(classifier: String?, machineString: String?, ppi: Int) {
        CustomIOSDevice.Companion.addDeviceToMap(knownDevices, classifier, machineString, ppi)
    }
}