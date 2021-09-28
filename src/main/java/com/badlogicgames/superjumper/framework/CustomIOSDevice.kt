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

class CustomIOSDevice(val classifier: String?, val machineString: String?, val ppi: Int) {
    companion object {
        /** The devices information can be obtained from https://github.com/lmirosevic/GBDeviceInfo or https://gist.github.com/adamawolf/3048717  */
        fun populateWithKnownDevices(): ObjectMap<String?, CustomIOSDevice?> {
            val deviceMap = ObjectMap<String?, CustomIOSDevice?>()
            addDeviceToMap(deviceMap, "IPHONE_2G", "iPhone1,1", 163)
            addDeviceToMap(deviceMap, "IPHONE_3G", "iPhone1,2", 163)
            addDeviceToMap(deviceMap, "IPHONE_3GS", "iPhone2,1", 163)
            addDeviceToMap(deviceMap, "IPHONE_4", "iPhone3,1", 326)
            addDeviceToMap(deviceMap, "IPHONE_4V", "iPhone3,2", 326)
            addDeviceToMap(deviceMap, "IPHONE_4_CDMA", "iPhone3,3", 326)
            addDeviceToMap(deviceMap, "IPHONE_4S", "iPhone4,1", 326)
            addDeviceToMap(deviceMap, "IPHONE_5", "iPhone5,1", 326)
            addDeviceToMap(deviceMap, "IPHONE_5_CDMA_GSM", "iPhone5,2", 326)
            addDeviceToMap(deviceMap, "IPHONE_5C", "iPhone5,3", 326)
            addDeviceToMap(deviceMap, "IPHONE_5C_CDMA_GSM", "iPhone5,4", 326)
            addDeviceToMap(deviceMap, "IPHONE_5S", "iPhone6,1", 326)
            addDeviceToMap(deviceMap, "IPHONE_5S_CDMA_GSM", "iPhone6,2", 326)
            addDeviceToMap(deviceMap, "IPHONE_6_PLUS", "iPhone7,1", 401)
            addDeviceToMap(deviceMap, "IPHONE_6", "iPhone7,2", 326)
            addDeviceToMap(deviceMap, "IPHONE_6S", "iPhone8,1", 326)
            addDeviceToMap(deviceMap, "IPHONE_6S_PLUS", "iPhone8,2", 401)
            addDeviceToMap(deviceMap, "IPHONE_7_CDMA_GSM", "iPhone9,1", 326)
            addDeviceToMap(deviceMap, "IPHONE_7_PLUS_CDMA_GSM", "iPhone9,2", 401)
            addDeviceToMap(deviceMap, "IPHONE_7", "iPhone9,3", 326)
            addDeviceToMap(deviceMap, "IPHONE_7_PLUS", "iPhone9,4", 401)
            addDeviceToMap(deviceMap, "IPHONE_SE", "iPhone8,4", 326)
            addDeviceToMap(deviceMap, "IPHONE_8_CDMA_GSM", "iPhone10,1", 326)
            addDeviceToMap(deviceMap, "IPHONE_8_PLUS_CDMA_GSM", "iPhone10,2", 401)
            addDeviceToMap(deviceMap, "IPHONE_X_CDMA_GSM", "iPhone10,3", 458)
            addDeviceToMap(deviceMap, "IPHONE_8", "iPhone10,4", 326)
            addDeviceToMap(deviceMap, "IPHONE_8_PLUS", "iPhone10,5", 401)
            addDeviceToMap(deviceMap, "IPHONE_X", "iPhone10,6", 458)
            addDeviceToMap(deviceMap, "IPHONE_XS", "iPhone11,2", 458)
            addDeviceToMap(deviceMap, "IPHONE_XS_MAX", "iPhone11,4", 458)
            addDeviceToMap(deviceMap, "IPHONE_XS_MAX_2_NANO_SIM", "iPhone11,6", 458)
            addDeviceToMap(deviceMap, "IPHONE_XR", "iPhone11,8", 326)
            addDeviceToMap(deviceMap, "IPHONE_11", "iPhone12,1", 326)
            addDeviceToMap(deviceMap, "IPHONE_11_PRO", "iPhone12,3", 458)
            addDeviceToMap(deviceMap, "IPHONE_11_PRO_MAX", "iPhone12,5", 458)
            addDeviceToMap(deviceMap, "IPHONE_SE_2G", "iPhone12,8", 326)
            addDeviceToMap(deviceMap, "IPOD_TOUCH_1G", "iPod1,1", 163)
            addDeviceToMap(deviceMap, "IPOD_TOUCH_2G", "iPod2,1", 163)
            addDeviceToMap(deviceMap, "IPOD_TOUCH_3G", "iPod3,1", 163)
            addDeviceToMap(deviceMap, "IPOD_TOUCH_4G", "iPod4,1", 326)
            addDeviceToMap(deviceMap, "IPOD_TOUCH_5G", "iPod5,1", 326)
            addDeviceToMap(deviceMap, "IPOD_TOUCH_6G", "iPod7,1", 326)
            addDeviceToMap(deviceMap, "IPOD_TOUCH_7G", "iPod9,1", 326)
            addDeviceToMap(deviceMap, "IPAD", "iPad1,1", 132)
            addDeviceToMap(deviceMap, "IPAD_3G", "iPad1,2", 132)
            addDeviceToMap(deviceMap, "IPAD_2_WIFI", "iPad2,1", 132)
            addDeviceToMap(deviceMap, "IPAD_2", "iPad2,2", 132)
            addDeviceToMap(deviceMap, "IPAD_2_CDMA", "iPad2,3", 132)
            addDeviceToMap(deviceMap, "IPAD_2V", "iPad2,4", 132)
            addDeviceToMap(deviceMap, "IPAD_MINI_WIFI", "iPad2,5", 164)
            addDeviceToMap(deviceMap, "IPAD_MINI", "iPad2,6", 164)
            addDeviceToMap(deviceMap, "IPAD_MINI_WIFI_CDMA", "iPad2,7", 164)
            addDeviceToMap(deviceMap, "IPAD_3_WIFI", "iPad3,1", 264)
            addDeviceToMap(deviceMap, "IPAD_3_WIFI_CDMA", "iPad3,2", 264)
            addDeviceToMap(deviceMap, "IPAD_3", "iPad3,3", 264)
            addDeviceToMap(deviceMap, "IPAD_4_WIFI", "iPad3,4", 264)
            addDeviceToMap(deviceMap, "IPAD_4", "iPad3,5", 264)
            addDeviceToMap(deviceMap, "IPAD_4_GSM_CDMA", "iPad3,6", 264)
            addDeviceToMap(deviceMap, "IPAD_AIR_WIFI", "iPad4,1", 264)
            addDeviceToMap(deviceMap, "IPAD_AIR_WIFI_GSM", "iPad4,2", 264)
            addDeviceToMap(deviceMap, "IPAD_AIR_WIFI_CDMA", "iPad4,3", 264)
            addDeviceToMap(deviceMap, "IPAD_MINI_RETINA_WIFI", "iPad4,4", 326)
            addDeviceToMap(deviceMap, "IPAD_MINI_RETINA_WIFI_CDMA", "iPad4,5", 326)
            addDeviceToMap(deviceMap, "IPAD_MINI_RETINA_WIFI_CELLULAR_CN", "iPad4,6", 326)
            addDeviceToMap(deviceMap, "IPAD_MINI_3_WIFI", "iPad4,7", 326)
            addDeviceToMap(deviceMap, "IPAD_MINI_3_WIFI_CELLULAR", "iPad4,8", 326)
            addDeviceToMap(deviceMap, "IPAD_MINI_3_WIFI_CELLULAR_CN", "iPad4,9", 326)
            addDeviceToMap(deviceMap, "IPAD_MINI_4_WIFI", "iPad5,1", 326)
            addDeviceToMap(deviceMap, "IPAD_MINI_4_WIFI_CELLULAR", "iPad5,2", 326)
            addDeviceToMap(deviceMap, "IPAD_MINI_AIR_2_WIFI", "iPad5,3", 264)
            addDeviceToMap(deviceMap, "IPAD_MINI_AIR_2_WIFI_CELLULAR", "iPad5,4", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_WIFI", "iPad6,7", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO", "iPad6,8", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_97_WIFI", "iPad6,3", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_97", "iPad6,4", 264)
            addDeviceToMap(deviceMap, "IPAD_5_WIFI", "iPad6,11", 264)
            addDeviceToMap(deviceMap, "IPAD_5_WIFI_CELLULAR", "iPad6,12", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_2_WIFI", "iPad7,1", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_2_WIFI_CELLULAR", "iPad7,2", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_10_5_WIFI", "iPad7,3", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_10_5_WIFI_CELLULAR", "iPad7,4", 264)
            addDeviceToMap(deviceMap, "IPAD_6_WIFI", "iPad7,5", 264)
            addDeviceToMap(deviceMap, "IPAD_6_WIFI_CELLULAR", "iPad7,6", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_11_WIFI", "iPad8,1", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_11_WIFI_6GB", "iPad8,2", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_11_WIFI_CELLULAR", "iPad8,3", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_11_WIFI_CELLULAR_6GB", "iPad8,4", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_3_WIFI", "iPad8,5", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_3_WIFI_6GB", "iPad8,6", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_3_WIFI_CELLULAR", "iPad8,7", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_3_WIFI_CELLULAR_6GB", "iPad8,8", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_11_2G_WIFI", "iPad8,9", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_11_2G_WIFI_CELLULAR", "iPad8,10", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_12.9_4G_WIFI", "iPad8,11", 264)
            addDeviceToMap(deviceMap, "IPAD_PRO_12.9_4G_WIFI_CELLULAR", "iPad8,12", 264)
            addDeviceToMap(deviceMap, "IPAD_MINI_5G_WIFI", "iPad11,1", 326)
            addDeviceToMap(deviceMap, "IPAD_MINI_5G_WIFI_CELLULAR", "iPad11,2", 326)
            addDeviceToMap(deviceMap, "IPAD_AIR_3G_WIFI", "iPad11,3", 264)
            addDeviceToMap(deviceMap, "IPAD_AIR_3G_WIFI_CELLULAR", "iPad11,4", 264)
            addDeviceToMap(deviceMap, "SIMULATOR_32", "i386", 264)
            addDeviceToMap(deviceMap, "SIMULATOR_64", "x86_64", 264)
            return deviceMap
        }

        fun addDeviceToMap(
            deviceMap: ObjectMap<String?, CustomIOSDevice?>,
            classifier: String?,
            machineString: String?,
            ppi: Int
        ) {
            deviceMap.put(machineString, CustomIOSDevice(classifier, machineString, ppi))
        }
    }
}