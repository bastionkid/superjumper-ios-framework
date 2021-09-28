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
import com.badlogic.gdx.backends.iosrobovm.custom.*
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import org.robovm.objc.annotation.Method
import org.robovm.rt.VM
import org.robovm.rt.bro.NativeObject
import org.robovm.rt.bro.annotation.Pointer
import java.lang.Error
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class CustomDefaultIOSInput(var app: CustomIOSApplication) : AbstractInput(), IOSInput {
    class NSObjectWrapper<T : NSObject?>(cls: Class<T>?) {
        companion object {
            private var HANDLE_OFFSET: Long = 0

            init {
                try {
                    HANDLE_OFFSET = VM.getInstanceFieldOffset(
                        VM.getFieldAddress(
                            NativeObject::class.java.getDeclaredField("handle")
                        )
                    ).toLong()
                } catch (t: Throwable) {
                    throw Error(t)
                }
            }
        }

        private val instance: T
        fun wrap(handle: Long): T {
            VM.setLong(VM.getObjectAddress(instance) + HANDLE_OFFSET, handle)
            return instance
        }

        init {
            instance = VM.allocateObject(cls)
        }
    }

    var config: CustomIOSApplicationConfiguration? = app.config
    var deltaX = IntArray(MAX_TOUCHES)
    var deltaY = IntArray(MAX_TOUCHES)
    var touchX = IntArray(MAX_TOUCHES)
    var touchY = IntArray(MAX_TOUCHES)
    var pressures = FloatArray(MAX_TOUCHES)
    var pressureSupported = false

    // we store the pointer to the UITouch struct here, or 0
    var touchDown = LongArray(MAX_TOUCHES)
    var numTouched = 0
    var justTouched = false
    var touchEventPool: Pool<TouchEvent> = object : Pool<TouchEvent>() {
        override fun newObject(): TouchEvent {
            return TouchEvent()
        }
    }
    var touchEvents = Array<TouchEvent>()
    private val keyEventPool: Pool<KeyEvent> = object : Pool<KeyEvent>(16, 1000) {
        override fun newObject(): KeyEvent {
            return KeyEvent()
        }
    }
    private val keyEvents: Array<KeyEvent?> = Array<KeyEvent?>()
    private var currentEventTimeStamp: Long = 0
    var acceleration = FloatArray(3)
    var rotation = FloatArray(3)
    var R = FloatArray(9)
    private var inputProcessor: InputProcessor? = null
    var hasVibrator = false

    //CMMotionManager motionManager;
    protected var accelerometerDelegate: UIAccelerometerDelegate? = null
    var compassSupported = false
    var keyboardCloseOnReturn = app.config.keyboardCloseOnReturn
    var softkeyboardActive = false
    private var hadHardwareKeyEvent = false
    override fun setupPeripherals() {
        //motionManager = new CMMotionManager();
        setupAccelerometer()
        setupCompass()
        val device = UIDevice.getCurrentDevice()
        if (device.model.equals("iphone", ignoreCase = true)) hasVibrator = true
        if (app.version >= 9) {
            val forceTouchCapability = UIScreen.getMainScreen().traitCollection.forceTouchCapability
            pressureSupported = forceTouchCapability == UIForceTouchCapability.Available
        }
    }

    protected fun setupCompass() {
        if (config!!.useCompass) {
            //setupMagnetometer();
        }
    }

    protected fun setupAccelerometer() {
        if (config!!.useAccelerometer) {
            accelerometerDelegate = object : UIAccelerometerDelegateAdapter() {
                @Method(selector = "accelerometer:didAccelerate:")
                fun didAccelerate(accelerometer: UIAccelerometer?, @Pointer valuesPtr: Long) {
                    val values = UI_ACCELERATION_WRAPPER.wrap(valuesPtr)
                    val x = values.x.toFloat() * 10
                    val y = values.y.toFloat() * 10
                    val z = values.z.toFloat() * 10
                    acceleration[0] = -x
                    acceleration[1] = -y
                    acceleration[2] = -z
                }
            }
            UIAccelerometer.getSharedAccelerometer().delegate = accelerometerDelegate
            UIAccelerometer.getSharedAccelerometer().updateInterval =
                config!!.accelerometerUpdate.toDouble()
        }
    }

    // need to retain a reference so GC doesn't get right of the
    // object passed to the native thread
    //	VoidBlock2<CMAccelerometerData, NSError> accelVoid = null;
    //	private void setupAccelerometer () {
    //		if (config.useAccelerometer) {
    //			motionManager.setAccelerometerUpdateInterval(config.accelerometerUpdate);
    //			accelVoid = new VoidBlock2<CMAccelerometerData, NSError>() {
    //				@Override
    //				public void invoke(CMAccelerometerData accelData, NSError error) {
    //					updateAccelerometer(accelData);
    //				}
    //			};
    //			motionManager.startAccelerometerUpdates(new NSOperationQueue(), accelVoid);
    //		}
    //	}
    // need to retain a reference so GC doesn't get right of the
    // object passed to the native thread
    //	VoidBlock2<CMMagnetometerData, NSError> magnetVoid = null;
    //	private void setupMagnetometer () {
    //		if (motionManager.isMagnetometerAvailable() && config.useCompass) compassSupported = true;
    //		else return;
    //		motionManager.setMagnetometerUpdateInterval(config.magnetometerUpdate);
    //		magnetVoid = new VoidBlock2<CMMagnetometerData, NSError>() {
    //			@Override
    //			public void invoke(CMMagnetometerData magnetData, NSError error) {
    //				updateRotation(magnetData);
    //			}
    //		};
    //		motionManager.startMagnetometerUpdates(new NSOperationQueue(), magnetVoid);
    //	}
    //	private void updateAccelerometer (CMAccelerometerData data) {
    //		float x = (float) data.getAcceleration().x() * 10f;
    //		float y = (float) data.getAcceleration().y() * 10f;
    //		float z = (float) data.getAcceleration().z() * 10f;
    //		acceleration[0] = -x;
    //		acceleration[1] = -y;
    //		acceleration[2] = -z;
    //	}
    //
    //	private void updateRotation (CMMagnetometerData data) {
    //		final float eX = (float) data.getMagneticField().x();
    //		final float eY = (float) data.getMagneticField().y();
    //		final float eZ = (float) data.getMagneticField().z();
    //
    //		float gX = acceleration[0];
    //		float gY = acceleration[1];
    //		float gZ = acceleration[2];
    //
    //		float cX = eY * gZ - eZ * gY;
    //		float cY = eZ * gX - eX * gZ;
    //		float cZ = eX * gY - eY * gX;
    //
    //		final float normal = (float) Math.sqrt(cX * cX + cY * cY + cZ * cZ);
    //		final float invertC = 1.0f / normal;
    //		cX *= invertC;
    //		cY *= invertC;
    //		cZ *= invertC;
    //		final float invertG = 1.0f / (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);
    //		gX *= invertG;
    //		gY *= invertG;
    //		gZ *= invertG;
    //		final float mX = gY * cZ - gZ * cY;
    //		final float mY = gZ * cX - gX * cZ;
    //		final float mZ = gX * cY - gY * cX;
    //
    //		R[0] = cX;	R[1] = cY;	R[2] = cZ;
    //		R[3] = mX;	R[4] = mY;	R[5] = mZ;
    //		R[6] = gX;	R[7] = gY;	R[8] = gZ;
    //
    //		rotation[0] = (float) Math.atan2(R[1], R[4]) * MathUtils.radDeg;
    //		rotation[1] = (float) Math.asin(-R[7]) * MathUtils.radDeg;
    //		rotation[2] = (float) Math.atan2(-R[6], R[8]) * MathUtils.radDeg;
    //	}
    override fun getAccelerometerX(): Float {
        return acceleration[0]
    }

    override fun getAccelerometerY(): Float {
        return acceleration[1]
    }

    override fun getAccelerometerZ(): Float {
        return acceleration[2]
    }

    override fun getAzimuth(): Float {
        return if (!compassSupported) 0f else rotation[0]
    }

    override fun getPitch(): Float {
        return if (!compassSupported) 0f else rotation[1]
    }

    override fun getRoll(): Float {
        return if (!compassSupported) 0f else rotation[2]
    }

    override fun getRotationMatrix(matrix: FloatArray) {
        if (matrix.size != 9) return
        //TODO implement when azimuth is fixed
    }

    override fun getMaxPointers(): Int {
        return MAX_TOUCHES
    }

    override fun getX(): Int {
        return touchX[0]
    }

    override fun getX(pointer: Int): Int {
        return touchX[pointer]
    }

    override fun getDeltaX(): Int {
        return deltaX[0]
    }

    override fun getDeltaX(pointer: Int): Int {
        return deltaX[pointer]
    }

    override fun getY(): Int {
        return touchY[0]
    }

    override fun getY(pointer: Int): Int {
        return touchY[pointer]
    }

    override fun getDeltaY(): Int {
        return deltaY[0]
    }

    override fun getDeltaY(pointer: Int): Int {
        return deltaY[pointer]
    }

    override fun isTouched(): Boolean {
        for (pointer in 0 until MAX_TOUCHES) {
            if (touchDown[pointer] != 0L) {
                return true
            }
        }
        return false
    }

    override fun justTouched(): Boolean {
        return justTouched
    }

    override fun isTouched(pointer: Int): Boolean {
        return touchDown[pointer] != 0L
    }

    override fun getPressure(): Float {
        return pressures[0]
    }

    override fun getPressure(pointer: Int): Float {
        return pressures[pointer]
    }

    override fun isButtonPressed(button: Int): Boolean {
        return button == Buttons.LEFT && numTouched > 0
    }

    override fun isButtonJustPressed(button: Int): Boolean {
        return button == Buttons.LEFT && justTouched
    }

    override fun getTextInput(
        listener: TextInputListener,
        title: String,
        text: String,
        hint: String
    ) {
        getTextInput(listener, title, text, hint, OnscreenKeyboardType.Default)
    }

    override fun getTextInput(
        listener: TextInputListener,
        title: String,
        text: String,
        hint: String,
        type: OnscreenKeyboardType
    ) {
        val uiAlertController = buildUIAlertController(listener, title, text, hint, type)
        app.uIViewController?.presentViewController(uiAlertController, true, null)
    }

    // hack for software keyboard support
    // uses a hidden textfield to capture input
    // see: http://www.badlogicgames.com/forum/viewtopic.php?f=17&t=11788
    private var textfield: UITextField? = null
    private val textDelegate: UITextFieldDelegate = object : UITextFieldDelegateAdapter() {
        override fun shouldChangeCharacters(
            textField: UITextField,
            range: NSRange,
            string: String
        ): Boolean {
            for (i in 0 until range.length) {
                inputProcessor!!.keyTyped(8.toChar())
            }
            if (string.isEmpty()) {
                if (range.length > 0) Gdx.graphics.requestRendering()
                return false
            }
            val chars = CharArray(string.length)
            string.toCharArray(chars, 0, 0, string.length)
            for (i in chars.indices) {
                inputProcessor!!.keyTyped(chars[i])
            }
            Gdx.graphics.requestRendering()
            return true
        }

        override fun shouldEndEditing(textField: UITextField): Boolean {
            // Text field needs to have at least one symbol - so we can use backspace
            textField.text = "x"
            Gdx.graphics.requestRendering()
            return true
        }

        override fun shouldReturn(textField: UITextField): Boolean {
            if (keyboardCloseOnReturn) setOnscreenKeyboardVisible(false)
            inputProcessor!!.keyDown(Input.Keys.ENTER)
            inputProcessor!!.keyTyped(13.toChar())
            Gdx.graphics.requestRendering()
            return false
        }
    }

    override fun setOnscreenKeyboardVisible(visible: Boolean) {
        setOnscreenKeyboardVisible(visible, OnscreenKeyboardType.Default)
    }

    override fun setOnscreenKeyboardVisible(visible: Boolean, type: OnscreenKeyboardType) {
        var type: OnscreenKeyboardType? = type
        if (textfield == null) createDefaultTextField()
        softkeyboardActive = visible
        if (visible) {
            var preferredInputType: UIKeyboardType
            if (type == null) type = OnscreenKeyboardType.Default
            textfield!!.keyboardType = getIosInputType(type)
            textfield!!.becomeFirstResponder()
            textfield!!.delegate = textDelegate
        } else {
            textfield!!.resignFirstResponder()
        }
    }

    protected fun getIosInputType(type: OnscreenKeyboardType?): UIKeyboardType {
        val preferredInputType: UIKeyboardType
        preferredInputType = when (type) {
            OnscreenKeyboardType.NumberPad -> UIKeyboardType.NumberPad
            OnscreenKeyboardType.PhonePad -> UIKeyboardType.PhonePad
            OnscreenKeyboardType.Email -> UIKeyboardType.EmailAddress
            OnscreenKeyboardType.URI -> UIKeyboardType.URL
            OnscreenKeyboardType.Password -> UIKeyboardType.Default
            else -> UIKeyboardType.Default
        }
        return preferredInputType
    }

    /**
     * Set the keyboard to close when the UITextField return key is pressed
     * @param shouldClose Whether or not the keyboard should clsoe on return key press
     */
    fun setKeyboardCloseOnReturnKey(shouldClose: Boolean) {
        keyboardCloseOnReturn = shouldClose
    }

    val keyboardTextField: UITextField?
        get() {
            if (textfield == null) createDefaultTextField()
            return textfield
        }

    private fun createDefaultTextField() {
        textfield = UITextField(CGRect(10.0, 10.0, 100.0, 50.0))
        //Parameters
        // Setting parameters
        textfield!!.keyboardType = UIKeyboardType.Default
        textfield!!.returnKeyType = UIReturnKeyType.Done
        textfield!!.autocapitalizationType = UITextAutocapitalizationType.None
        textfield!!.autocorrectionType = UITextAutocorrectionType.No
        textfield!!.spellCheckingType = UITextSpellCheckingType.No
        textfield!!.isHidden = true
        // Text field needs to have at least one symbol - so we can use backspace
        textfield!!.text = "x"
        app.uIViewController?.view?.addSubview(textfield)
    }

    /** Builds an [UIAlertController] with an added [UITextField] for inputting text.
     * @param listener Text input listener
     * @param title Dialog title
     * @param text Text for text field
     * @param type
     * @return UIAlertController
     */
    private fun buildUIAlertController(
        listener: TextInputListener,
        title: String,
        text: String,
        placeholder: String,
        type: OnscreenKeyboardType
    ): UIAlertController {
        val uiAlertController = UIAlertController(title, text, UIAlertControllerStyle.Alert)
        uiAlertController.addTextField { uiTextField ->
            uiTextField.placeholder = placeholder
            uiTextField.text = text
            uiTextField.keyboardType = getIosInputType(type)
            if (type == OnscreenKeyboardType.Password) {
                uiTextField.isSecureTextEntry = true
            }
        }
        uiAlertController.addAction(
            UIAlertAction(
                "Ok",
                UIAlertActionStyle.Default
            ) { // user clicked "Ok" button
                val textField = uiAlertController.textFields[0]
                listener.input(textField.text)
            })
        uiAlertController.addAction(
            UIAlertAction(
                "Cancel",
                UIAlertActionStyle.Cancel
            ) { // user clicked "Cancel" button
                listener.canceled()
            })
        return uiAlertController
    }

    override fun vibrate(milliseconds: Int) {
        AudioServices.playSystemSound(4095)
    }

    override fun vibrate(pattern: LongArray, repeat: Int) {
        // FIXME implement this
    }

    override fun cancelVibrate() {
        // FIXME implement this
    }

    override fun getCurrentEventTime(): Long {
        return currentEventTimeStamp
    }

    override fun setInputProcessor(processor: InputProcessor) {
        inputProcessor = processor
    }

    override fun getInputProcessor(): InputProcessor {
        return inputProcessor!!
    }

    override fun isPeripheralAvailable(peripheral: Peripheral): Boolean {
        if (peripheral == Peripheral.Accelerometer && config!!.useAccelerometer) return true
        if (peripheral == Peripheral.MultitouchScreen) return true
        if (peripheral == Peripheral.Vibrator) return hasVibrator
        if (peripheral == Peripheral.Compass) return compassSupported
        if (peripheral == Peripheral.OnscreenKeyboard) return true
        if (peripheral == Peripheral.Pressure) return pressureSupported
        return if (peripheral == Peripheral.HardwareKeyboard) if (Foundation.getMajorSystemVersion() >= 14) GCKeyboard.getCoalescedKeyboard() != null else hadHardwareKeyEvent else false
    }

    override fun getRotation(): Int {
        // we measure orientation counter clockwise, just like on Android
        return when (app.uiApp!!.statusBarOrientation) {
            UIInterfaceOrientation.LandscapeLeft -> 270
            UIInterfaceOrientation.PortraitUpsideDown -> 180
            UIInterfaceOrientation.LandscapeRight -> 90
            UIInterfaceOrientation.Portrait -> 0
            else -> 0
        }
    }

    override fun getNativeOrientation(): Input.Orientation {
        return when (app.uiApp!!.statusBarOrientation) {
            UIInterfaceOrientation.LandscapeLeft, UIInterfaceOrientation.LandscapeRight -> Input.Orientation.Landscape
            else -> Input.Orientation.Portrait
        }
    }

    override fun setCursorCatched(catched: Boolean) {}
    override fun isCursorCatched(): Boolean {
        return false
    }

    override fun setCursorPosition(x: Int, y: Int) {}
    override fun onTouch(touches: Long) {
        toTouchEvents(touches)
        Gdx.graphics.requestRendering()
    }

    override fun onKey(key: UIKey, down: Boolean): Boolean {
        if (key == null) {
            return false
        }
        val keyCode = getGdxKeyCode(key)
        if (keyCode != Input.Keys.UNKNOWN) synchronized(keyEvents) {
            hadHardwareKeyEvent = true
            var event = keyEventPool.obtain()
            val timeStamp = System.nanoTime()
            event!!.timeStamp = timeStamp
            event.keyChar = 0.toChar()
            event.keyCode = keyCode
            event.type = if (down) KeyEvent.KEY_DOWN else KeyEvent.KEY_UP
            keyEvents.add(event)
            if (!down) {
                val character: Char
                character = when (keyCode) {
                    Input.Keys.DEL -> 8.toChar()
                    Input.Keys.FORWARD_DEL -> 127.toChar()
                    Input.Keys.ENTER -> 13.toChar()
                    else -> {
                        val characters = key.characters
                        // special keys return constants like "UIKeyInputF5", so we check for length 1
                        if (characters != null && characters.length == 1) characters[0] else 0.toChar()
                    }
                }
                if (character.toInt() >= 0) {
                    event = keyEventPool.obtain()
                    event!!.timeStamp = timeStamp
                    event.type = KeyEvent.KEY_TYPED
                    event.keyCode = keyCode
                    event.keyChar = character
                    keyEvents.add(event)
                }
                if (pressedKeys[keyCode]) {
                    pressedKeyCount--
                    pressedKeys[keyCode] = false
                }
            } else {
                if (!pressedKeys[event.keyCode]) {
                    pressedKeyCount++
                    pressedKeys[event.keyCode] = true
                }
            }
        }
        return isCatchKey(keyCode)
    }

    override fun processEvents() {
        synchronized(touchEvents) {
            justTouched = false
            for (event in touchEvents) {
                currentEventTimeStamp = event.timestamp
                when (event.phase) {
                    UITouchPhase.Began -> {
                        if (inputProcessor != null) inputProcessor!!.touchDown(
                            event.x,
                            event.y,
                            event.pointer,
                            Buttons.LEFT
                        )
                        if (numTouched >= 1) justTouched = true
                    }
                    UITouchPhase.Cancelled, UITouchPhase.Ended -> if (inputProcessor != null) inputProcessor!!.touchUp(
                        event.x,
                        event.y,
                        event.pointer,
                        Buttons.LEFT
                    )
                    UITouchPhase.Moved, UITouchPhase.Stationary -> if (inputProcessor != null) inputProcessor!!.touchDragged(
                        event.x,
                        event.y,
                        event.pointer
                    )
                }
            }
            touchEventPool.freeAll(touchEvents)
            touchEvents.clear()
        }
        synchronized(keyEvents) {
            if (keyJustPressed) {
                keyJustPressed = false
                for (i in justPressedKeys.indices) {
                    justPressedKeys[i] = false
                }
            }
            for (e in keyEvents) {
                currentEventTimeStamp = e!!.timeStamp
                when (e.type) {
                    KeyEvent.KEY_DOWN -> {
                        if (inputProcessor != null) inputProcessor!!.keyDown(e.keyCode)
                        keyJustPressed = true
                        justPressedKeys[e.keyCode] = true
                    }
                    KeyEvent.KEY_UP -> if (inputProcessor != null) inputProcessor!!.keyUp(
                        e.keyCode
                    )
                    KeyEvent.KEY_TYPED ->                         // don't process key typed events if soft keyboard is active
                        // the soft keyboard hook already catches the changes
                        if (!softkeyboardActive && inputProcessor != null) inputProcessor!!.keyTyped(
                            e.keyChar
                        )
                }
            }
            keyEventPool.freeAll(keyEvents)
            keyEvents.clear()
        }
    }

    private val freePointer: Int
        private get() {
            for (i in touchDown.indices) {
                if (touchDown[i] == 0L) return i
            }
            throw GdxRuntimeException("Couldn't find free pointer id!")
        }

    private fun findPointer(touch: UITouch): Int {
        val ptr = touch.handle
        for (i in touchDown.indices) {
            if (touchDown[i] == ptr) return i
        }
        // If pointer is not found
        val sb = StringBuilder()
        for (i in touchDown.indices) {
            sb.append(i.toString() + ":" + touchDown[i] + " ")
        }
        Gdx.app.error("IOSInput", "Pointer ID lookup failed: $ptr, $sb")
        return POINTER_NOT_FOUND
    }

    private class NSSetExtensions : NSExtensions() {
       companion object {
           @JvmStatic
           @Method(selector = "allObjects")
           @Pointer
           external fun allObjects(@Pointer thiz: Long): Long
       }
    }

    private class NSArrayExtensions : NSExtensions() {
        companion object {
            @JvmStatic
            @Method(selector = "objectAtIndex:")
            @Pointer
            external fun `objectAtIndex$`(@Pointer thiz: Long, @MachineSizedUInt index: Long): Long

            @JvmStatic
            @Method(selector = "count")
            @MachineSizedUInt
            external fun count(@Pointer thiz: Long): Long
        }
    }

    private fun toTouchEvents(touches: Long) {
        val array = NSSetExtensions.allObjects(touches)
        val length = NSArrayExtensions.count(array)
            .toInt()
        val screenBounds = app.screenBounds
        for (i in 0 until length) {
            val touchHandle = NSArrayExtensions.`objectAtIndex$`(array, i.toLong())
            val touch = UI_TOUCH_WRAPPER.wrap(touchHandle)
            var locX: Int
            var locY: Int
            // Get and map the location to our drawing space
            run {
                val loc = touch.getLocationInView(app.graphics!!.view)
                locX = (loc.x - screenBounds!!.x).toInt()
                locY = (loc.y - screenBounds.y).toInt()
                if (config!!.hdpiMode == HdpiMode.Pixels) {
                    locX *= app.pixelsPerPoint.toInt()
                    locY *= app.pixelsPerPoint.toInt()
                }
            }

            // if its not supported, we will simply use 1.0f when touch is present
            var pressure = 1.0f
            if (pressureSupported) {
                pressure = touch.force.toFloat()
            }
            synchronized(touchEvents) {
                val phase = touch.phase
                val event = touchEventPool.obtain()
                event.x = locX
                event.y = locY
                event.phase = phase
                event.timestamp = (touch.timestamp * 1000000000).toLong()
                if (phase == UITouchPhase.Began) {
                    event.pointer = freePointer
                    touchDown[event.pointer] = touch.handle
                    touchX[event.pointer] = event.x
                    touchY[event.pointer] = event.y
                    deltaX[event.pointer] = 0
                    deltaY[event.pointer] = 0
                    pressures[event.pointer] = pressure
                    numTouched++
                } else if (phase == UITouchPhase.Moved || phase == UITouchPhase.Stationary) {
                    event.pointer = findPointer(touch)
                    if (event.pointer != POINTER_NOT_FOUND) {
                        deltaX[event.pointer] = event.x - touchX[event.pointer]
                        deltaY[event.pointer] = event.y - touchY[event.pointer]
                        touchX[event.pointer] = event.x
                        touchY[event.pointer] = event.y
                        pressures[event.pointer] = pressure
                    }
                } else if (phase == UITouchPhase.Cancelled || phase == UITouchPhase.Ended) {
                    event.pointer = findPointer(touch)
                    if (event.pointer != POINTER_NOT_FOUND) {
                        touchDown[event.pointer] = 0
                        touchX[event.pointer] = event.x
                        touchY[event.pointer] = event.y
                        deltaX[event.pointer] = 0
                        deltaY[event.pointer] = 0
                        pressures[event.pointer] = 0f
                        numTouched--
                    }
                }
                if (event.pointer != POINTER_NOT_FOUND) {
                    touchEvents.add(event)
                } else {
                    touchEventPool.free(event)
                }
            }
        }
    }

    class TouchEvent {
        var phase: UITouchPhase? = null
        var timestamp: Long = 0
        var x = 0
        var y = 0
        var pointer = 0
    }

    internal class KeyEvent {
        var timeStamp: Long = 0
        var type = 0
        var keyCode = 0
        var keyChar = 0.toChar()

        companion object {
            const val KEY_DOWN = 0
            const val KEY_UP = 1
            const val KEY_TYPED = 2
        }
    }

    override fun getGyroscopeX(): Float {
        // TODO Auto-generated method stub
        return 0f
    }

    override fun getGyroscopeY(): Float {
        // TODO Auto-generated method stub
        return 0f
    }

    override fun getGyroscopeZ(): Float {
        // TODO Auto-generated method stub
        return 0f
    }

    protected fun getGdxKeyCode(key: UIKey): Int {
        val keyCode: UIKeyboardHIDUsage
        keyCode = try {
            key.keyCode
        } catch (e: IllegalArgumentException) {
            return Input.Keys.UNKNOWN
        }
        return when (keyCode) {
            UIKeyboardHIDUsage.KeyboardA -> Input.Keys.A
            UIKeyboardHIDUsage.KeyboardB -> Input.Keys.B
            UIKeyboardHIDUsage.KeyboardC -> Input.Keys.C
            UIKeyboardHIDUsage.KeyboardD -> Input.Keys.D
            UIKeyboardHIDUsage.KeyboardE -> Input.Keys.E
            UIKeyboardHIDUsage.KeyboardF -> Input.Keys.F
            UIKeyboardHIDUsage.KeyboardG -> Input.Keys.G
            UIKeyboardHIDUsage.KeyboardH -> Input.Keys.H
            UIKeyboardHIDUsage.KeyboardI -> Input.Keys.I
            UIKeyboardHIDUsage.KeyboardJ -> Input.Keys.J
            UIKeyboardHIDUsage.KeyboardK -> Input.Keys.K
            UIKeyboardHIDUsage.KeyboardL -> Input.Keys.L
            UIKeyboardHIDUsage.KeyboardM -> Input.Keys.M
            UIKeyboardHIDUsage.KeyboardN -> Input.Keys.N
            UIKeyboardHIDUsage.KeyboardO -> Input.Keys.O
            UIKeyboardHIDUsage.KeyboardP -> Input.Keys.P
            UIKeyboardHIDUsage.KeyboardQ -> Input.Keys.Q
            UIKeyboardHIDUsage.KeyboardR -> Input.Keys.R
            UIKeyboardHIDUsage.KeyboardS -> Input.Keys.S
            UIKeyboardHIDUsage.KeyboardT -> Input.Keys.T
            UIKeyboardHIDUsage.KeyboardU -> Input.Keys.U
            UIKeyboardHIDUsage.KeyboardV -> Input.Keys.V
            UIKeyboardHIDUsage.KeyboardW -> Input.Keys.W
            UIKeyboardHIDUsage.KeyboardX -> Input.Keys.X
            UIKeyboardHIDUsage.KeyboardY -> Input.Keys.Y
            UIKeyboardHIDUsage.KeyboardZ -> Input.Keys.Z
            UIKeyboardHIDUsage.Keyboard1 -> Input.Keys.NUM_1
            UIKeyboardHIDUsage.Keyboard2 -> Input.Keys.NUM_2
            UIKeyboardHIDUsage.Keyboard3 -> Input.Keys.NUM_3
            UIKeyboardHIDUsage.Keyboard4 -> Input.Keys.NUM_4
            UIKeyboardHIDUsage.Keyboard5 -> Input.Keys.NUM_5
            UIKeyboardHIDUsage.Keyboard6 -> Input.Keys.NUM_6
            UIKeyboardHIDUsage.Keyboard7 -> Input.Keys.NUM_7
            UIKeyboardHIDUsage.Keyboard8 -> Input.Keys.NUM_8
            UIKeyboardHIDUsage.Keyboard9 -> Input.Keys.NUM_9
            UIKeyboardHIDUsage.Keyboard0 -> Input.Keys.NUM_0
            UIKeyboardHIDUsage.KeyboardReturnOrEnter -> Input.Keys.ENTER
            UIKeyboardHIDUsage.KeyboardEscape -> Input.Keys.ESCAPE
            UIKeyboardHIDUsage.KeyboardDeleteOrBackspace -> Input.Keys.BACKSPACE
            UIKeyboardHIDUsage.KeyboardTab -> Input.Keys.TAB
            UIKeyboardHIDUsage.KeyboardSpacebar -> Input.Keys.SPACE
            UIKeyboardHIDUsage.KeyboardHyphen -> Input.Keys.MINUS
            UIKeyboardHIDUsage.KeyboardEqualSign -> Input.Keys.EQUALS
            UIKeyboardHIDUsage.KeyboardOpenBracket -> Input.Keys.LEFT_BRACKET
            UIKeyboardHIDUsage.KeyboardCloseBracket -> Input.Keys.RIGHT_BRACKET
            UIKeyboardHIDUsage.KeyboardBackslash -> Input.Keys.BACKSLASH
            UIKeyboardHIDUsage.KeyboardNonUSPound -> Input.Keys.POUND
            UIKeyboardHIDUsage.KeyboardSemicolon -> Input.Keys.SEMICOLON
            UIKeyboardHIDUsage.KeyboardQuote -> Input.Keys.APOSTROPHE
            UIKeyboardHIDUsage.KeyboardGraveAccentAndTilde -> Input.Keys.GRAVE
            UIKeyboardHIDUsage.KeyboardComma -> Input.Keys.COMMA
            UIKeyboardHIDUsage.KeyboardPeriod -> Input.Keys.PERIOD
            UIKeyboardHIDUsage.KeyboardSlash -> Input.Keys.SLASH
            UIKeyboardHIDUsage.KeyboardF1 -> Input.Keys.F1
            UIKeyboardHIDUsage.KeyboardF2 -> Input.Keys.F2
            UIKeyboardHIDUsage.KeyboardF3 -> Input.Keys.F3
            UIKeyboardHIDUsage.KeyboardF4 -> Input.Keys.F4
            UIKeyboardHIDUsage.KeyboardF5 -> Input.Keys.F5
            UIKeyboardHIDUsage.KeyboardF6 -> Input.Keys.F6
            UIKeyboardHIDUsage.KeyboardF7 -> Input.Keys.F7
            UIKeyboardHIDUsage.KeyboardF8 -> Input.Keys.F8
            UIKeyboardHIDUsage.KeyboardF9 -> Input.Keys.F9
            UIKeyboardHIDUsage.KeyboardF10 -> Input.Keys.F10
            UIKeyboardHIDUsage.KeyboardF11 -> Input.Keys.F11
            UIKeyboardHIDUsage.KeyboardF12 -> Input.Keys.F12
            UIKeyboardHIDUsage.KeyboardF13 -> Input.Keys.F13
            UIKeyboardHIDUsage.KeyboardF14 -> Input.Keys.F14
            UIKeyboardHIDUsage.KeyboardF15 -> Input.Keys.F15
            UIKeyboardHIDUsage.KeyboardF16 -> Input.Keys.F16
            UIKeyboardHIDUsage.KeyboardF17 -> Input.Keys.F17
            UIKeyboardHIDUsage.KeyboardF18 -> Input.Keys.F18
            UIKeyboardHIDUsage.KeyboardF19 -> Input.Keys.F19
            UIKeyboardHIDUsage.KeyboardF20 -> Input.Keys.F20
            UIKeyboardHIDUsage.KeyboardF21 -> Input.Keys.F21
            UIKeyboardHIDUsage.KeyboardF22 -> Input.Keys.F22
            UIKeyboardHIDUsage.KeyboardF23 -> Input.Keys.F23
            UIKeyboardHIDUsage.KeyboardF24 -> Input.Keys.F24
            UIKeyboardHIDUsage.KeyboardPause -> Input.Keys.PAUSE
            UIKeyboardHIDUsage.KeyboardInsert -> Input.Keys.INSERT
            UIKeyboardHIDUsage.KeyboardHome -> Input.Keys.HOME
            UIKeyboardHIDUsage.KeyboardPageUp -> Input.Keys.PAGE_UP
            UIKeyboardHIDUsage.KeyboardDeleteForward -> Input.Keys.FORWARD_DEL
            UIKeyboardHIDUsage.KeyboardEnd -> Input.Keys.END
            UIKeyboardHIDUsage.KeyboardPageDown -> Input.Keys.PAGE_DOWN
            UIKeyboardHIDUsage.KeyboardRightArrow -> Input.Keys.RIGHT
            UIKeyboardHIDUsage.KeyboardLeftArrow -> Input.Keys.LEFT
            UIKeyboardHIDUsage.KeyboardDownArrow -> Input.Keys.DOWN
            UIKeyboardHIDUsage.KeyboardUpArrow -> Input.Keys.UP
            UIKeyboardHIDUsage.KeypadNumLock -> Input.Keys.NUM_LOCK
            UIKeyboardHIDUsage.KeypadSlash -> Input.Keys.NUMPAD_DIVIDE
            UIKeyboardHIDUsage.KeypadAsterisk -> Input.Keys.NUMPAD_MULTIPLY
            UIKeyboardHIDUsage.KeypadHyphen -> Input.Keys.NUMPAD_SUBTRACT
            UIKeyboardHIDUsage.KeypadPlus -> Input.Keys.NUMPAD_ADD
            UIKeyboardHIDUsage.KeypadEnter -> Input.Keys.NUMPAD_ENTER
            UIKeyboardHIDUsage.Keypad1 -> Input.Keys.NUM_1
            UIKeyboardHIDUsage.Keypad2 -> Input.Keys.NUM_2
            UIKeyboardHIDUsage.Keypad3 -> Input.Keys.NUM_3
            UIKeyboardHIDUsage.Keypad4 -> Input.Keys.NUM_4
            UIKeyboardHIDUsage.Keypad5 -> Input.Keys.NUM_5
            UIKeyboardHIDUsage.Keypad6 -> Input.Keys.NUM_6
            UIKeyboardHIDUsage.Keypad7 -> Input.Keys.NUM_7
            UIKeyboardHIDUsage.Keypad8 -> Input.Keys.NUM_8
            UIKeyboardHIDUsage.Keypad9 -> Input.Keys.NUM_9
            UIKeyboardHIDUsage.Keypad0 -> Input.Keys.NUM_0
            UIKeyboardHIDUsage.KeypadPeriod -> Input.Keys.NUMPAD_DOT
            UIKeyboardHIDUsage.KeyboardNonUSBackslash -> Input.Keys.BACKSLASH
            UIKeyboardHIDUsage.KeyboardApplication -> Input.Keys.MENU
            UIKeyboardHIDUsage.KeyboardPower -> Input.Keys.POWER
            UIKeyboardHIDUsage.KeypadEqualSign, UIKeyboardHIDUsage.KeypadEqualSignAS400 -> Input.Keys.NUMPAD_EQUALS
            UIKeyboardHIDUsage.KeyboardHelp -> Input.Keys.F1
            UIKeyboardHIDUsage.KeyboardMenu -> Input.Keys.MENU
            UIKeyboardHIDUsage.KeyboardSelect -> Input.Keys.BUTTON_SELECT
            UIKeyboardHIDUsage.KeyboardStop -> Input.Keys.MEDIA_STOP
            UIKeyboardHIDUsage.KeyboardFind -> Input.Keys.SEARCH
            UIKeyboardHIDUsage.KeyboardMute -> Input.Keys.MUTE
            UIKeyboardHIDUsage.KeyboardVolumeUp -> Input.Keys.VOLUME_UP
            UIKeyboardHIDUsage.KeyboardVolumeDown -> Input.Keys.VOLUME_DOWN
            UIKeyboardHIDUsage.KeypadComma -> Input.Keys.NUMPAD_COMMA
            UIKeyboardHIDUsage.KeyboardAlternateErase -> Input.Keys.DEL
            UIKeyboardHIDUsage.KeyboardCancel -> Input.Keys.ESCAPE
            UIKeyboardHIDUsage.KeyboardClear -> Input.Keys.CLEAR
            UIKeyboardHIDUsage.KeyboardReturn -> Input.Keys.ENTER
            UIKeyboardHIDUsage.KeyboardLeftControl -> Input.Keys.CONTROL_LEFT
            UIKeyboardHIDUsage.KeyboardLeftShift -> Input.Keys.SHIFT_LEFT
            UIKeyboardHIDUsage.KeyboardLeftAlt -> Input.Keys.ALT_LEFT
            UIKeyboardHIDUsage.KeyboardRightControl -> Input.Keys.CONTROL_RIGHT
            UIKeyboardHIDUsage.KeyboardRightShift -> Input.Keys.SHIFT_RIGHT
            UIKeyboardHIDUsage.KeyboardRightAlt -> Input.Keys.ALT_RIGHT
            UIKeyboardHIDUsage.KeyboardCapsLock -> Input.Keys.CAPS_LOCK
            UIKeyboardHIDUsage.KeyboardPrintScreen -> Input.Keys.PRINT_SCREEN
            UIKeyboardHIDUsage.KeyboardScrollLock -> Input.Keys.SCROLL_LOCK
            else -> Input.Keys.UNKNOWN
        }
    }

    companion object {
        const val MAX_TOUCHES = 20
        private const val POINTER_NOT_FOUND = -1
        private val UI_TOUCH_WRAPPER = NSObjectWrapper(
            UITouch::class.java
        )
        val UI_ACCELERATION_WRAPPER = NSObjectWrapper(
            UIAcceleration::class.java
        )
    }
}