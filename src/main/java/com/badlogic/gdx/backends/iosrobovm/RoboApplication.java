package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALAudioSession;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALIOSAudio;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSBundle;
import org.robovm.apple.foundation.NSMutableDictionary;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSProcessInfo;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIPasteboard;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.rt.bro.Bro;

import java.io.File;
import java.util.Iterator;

public class RoboApplication implements Application {
    UIApplication uiApp;
    CGRect bounds;
    ApplicationListener listener;
    IOSViewControllerListener viewControllerListener;
    IOSApplicationConfiguration config;
    RoboGraphics graphics;
    IOSAudio audio;
    Files files;
    RoboInput input;
    RoboNet net;
    int logLevel = Application.LOG_DEBUG;
    ApplicationLogger applicationLogger;

    /**
     * The display scale factor (1.0f for normal; 2.0f to use retina coordinates/dimensions).
     */
    float pixelsPerPoint;

    private IOSScreenBounds lastScreenBounds = null;

    final Array<Runnable> runnables = new Array<>();
    Array<Runnable> executedRunnables = new Array<>();
    final Array<LifecycleListener> lifecycleListeners = new Array<>();

    RoboApplication(ApplicationListener listener, IOSApplicationConfiguration config) {
        this.listener = listener;
        this.config = config;
    }

    void initialise(UIApplication uiApp, CGRect bounds, String frameworkId) {
        setApplicationLogger(new IOSApplicationLogger());
        Gdx.app = this;
        this.uiApp = uiApp;
        this.bounds = bounds;

        // enable or disable screen dimming
        uiApp.setIdleTimerDisabled(config.preventScreenDimming);

        Gdx.app.debug("RoboApplication", "iOS version: " + UIDevice.getCurrentDevice().getSystemVersion());
        Gdx.app.debug("RoboApplication", "Running in " + (Bro.IS_64BIT ? "64-bit" : "32-bit") + " mode");

        // iOS counts in "points" instead of pixels. Points are logical pixels
        pixelsPerPoint = (float) UIScreen.getMainScreen().getNativeScale();
        Gdx.app.debug("RoboApplication", "Pixels per point: " + pixelsPerPoint);

        // setup libgdx
        this.input = createInput();
        this.graphics = createGraphics();
        Gdx.gl = Gdx.gl20 = graphics.gl20;
        Gdx.gl30 = graphics.gl30;
        this.files = createFiles(frameworkId);
        this.audio = createAudio(config);
        this.net = new RoboNet(this, config);

        Gdx.files = this.files;
        Gdx.graphics = this.graphics;
        Gdx.audio = this.audio;
        Gdx.input = this.input;
        Gdx.net = this.net;

        this.input.setupPeripherals();
        Gdx.app.debug("RoboApplication", "created");
    }

    protected Files createFiles(String frameworkId) {
        NSBundle bundle = NSBundle.getBundle(frameworkId);
        String fwBundlePath = bundle.getBundlePath();
        return new RoboFiles(fwBundlePath);
    }

    protected IOSAudio createAudio(IOSApplicationConfiguration config) {
        return new OALIOSAudio(config);
    }

    protected RoboGraphics createGraphics() {
        return new RoboGraphics(this, config, input, config.useGL30);
    }

    protected RoboGLViewController createUIViewController(RoboGraphics graphics) {
        return new RoboGLViewController(this, graphics);
    }

    protected RoboInput createInput() {
        return new RoboInput(this);
    }

    /**
     * Returns device ppi using a best guess approach when device is unknown. Overwrite to customize strategy.
     */
    protected int guessUnknownPpi() {
        int ppi;
        if (UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad)
            ppi = 132 * (int) pixelsPerPoint;
        else
            ppi = 164 * (int) pixelsPerPoint;
        error("RoboApplication", "Device PPI unknown. PPI value has been guessed to " + ppi + " but may be wrong");
        return ppi;
    }

    /**
     * Return the UI view controller of IOSApplication
     *
     * @return the view controller of IOSApplication
     */
    public UIViewController getUIViewController() {
        return graphics.viewController;
    }

    /** Return the UI Window of IOSApplication
     * @return the window */
//    public UIWindow getUIWindow () {
//        return uiWindow;
//    }

    /**
     * @return logical dimensions of space we draw to, adjusted for device orientation
     * @see IOSScreenBounds for detailed explanation
     */
    protected IOSScreenBounds computeBounds() {
        CGRect screenBounds = this.bounds;
        final CGRect statusBarFrame = uiApp.getStatusBarFrame();
        double statusBarHeight = statusBarFrame.getHeight();

        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        if (statusBarHeight != 0.0) {
            debug("RoboApplication", "Status bar is visible (height = " + statusBarHeight + ")");
            screenHeight -= statusBarHeight;
        } else {
            debug("RoboApplication", "Status bar is not visible");
        }
        final int offsetX = 0;
        final int offsetY = (int) Math.round(statusBarHeight);
//        final int offsetY = 0;

        final int width = (int) Math.round(screenWidth);
        final int height = (int) Math.round(screenHeight);

        final int backBufferWidth = (int) Math.round(screenWidth * pixelsPerPoint);
        final int backBufferHeight = (int) Math.round(screenHeight * pixelsPerPoint);

        debug("RoboApplication", "Computed bounds are x=" + offsetX + " y=" + offsetY + " w=" + width + " h=" + height + " bbW= "
                + backBufferWidth + " bbH= " + backBufferHeight);

        return lastScreenBounds = new IOSScreenBounds(offsetX, offsetY, width, height, backBufferWidth, backBufferHeight);
    }

    /**
     * @return area of screen in UIKit points on which libGDX draws, with 0,0 being upper left corner
     */
    public IOSScreenBounds getScreenBounds() {
        if (lastScreenBounds == null)
            return computeBounds();
        else
            return lastScreenBounds;
    }

    final void didBecomeActive() {
        Gdx.app.debug("RoboApplication", "resumed");
        // workaround for ObjectAL crash problem
        // see: https://groups.google.com/forum/?fromgroups=#!topic/objectal-for-iphone/ubRWltp_i1Q
        OALAudioSession audioSession = OALAudioSession.sharedInstance();
        if (audioSession != null) {
            audioSession.forceEndInterruption();
        }
        if (config.allowIpod) {
            OALSimpleAudio audio = OALSimpleAudio.sharedInstance();
            if (audio != null) {
                audio.setUseHardwareIfAvailable(false);
            }
        }
        graphics.makeCurrent();
        graphics.resume();
    }

    final void willEnterForeground() {
        // workaround for ObjectAL crash problem
        // see: https://groups.google.com/forum/?fromgroups=#!topic/objectal-for-iphone/ubRWltp_i1Q
        OALAudioSession audioSession = OALAudioSession.sharedInstance();
        if (audioSession != null) {
            audioSession.forceEndInterruption();
        }
    }

    final void willResignActive() {
        Gdx.app.debug("RoboApplication", "paused");
        graphics.makeCurrent();
        graphics.pause();
        Gdx.gl.glFinish();
    }

    final void willTerminate() {
        Gdx.app.debug("RoboApplication", "disposed");
        graphics.makeCurrent();
        synchronized (lifecycleListeners) {
            for (LifecycleListener listener : lifecycleListeners) {
                listener.pause();
            }
        }
        listener.dispose();
        Gdx.gl.glFinish();
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return listener;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public Files getFiles() {
        return files;
    }

    @Override
    public Net getNet() {
        return net;
    }

    @Override
    public void debug(String tag, String message) {
        if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message, exception);
    }

    @Override
    public void log(String tag, String message) {
        if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message, exception);
    }

    @Override
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public void setApplicationLogger(ApplicationLogger applicationLogger) {
        this.applicationLogger = applicationLogger;
    }

    @Override
    public ApplicationLogger getApplicationLogger() {
        return applicationLogger;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.iOS;
    }

    @Override
    public int getVersion() {
        return (int) NSProcessInfo.getSharedProcessInfo().getOperatingSystemVersion().getMajorVersion();
    }

    @Override
    public long getJavaHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getNativeHeap() {
        return getJavaHeap();
    }

    @Override
    public Preferences getPreferences(String name) {
        File libraryPath = new File(System.getenv("HOME"), "Library");
        File finalPath = new File(libraryPath, name + ".plist");

        @SuppressWarnings("unchecked")
        NSMutableDictionary<NSString, NSObject> nsDictionary = (NSMutableDictionary<NSString, NSObject>) NSMutableDictionary
                .read(finalPath);

        // if it fails to get an existing dictionary, create a new one.
        if (nsDictionary == null) {
            nsDictionary = new NSMutableDictionary<NSString, NSObject>();
            nsDictionary.write(finalPath, false);
        }
        return new IOSPreferences(nsDictionary, finalPath.getAbsolutePath());
    }

    @Override
    public void postRunnable(Runnable runnable) {
        synchronized (runnables) {
            runnables.add(runnable);
            Gdx.graphics.requestRendering();
        }
    }

    public void processRunnables() {
        synchronized (runnables) {
            executedRunnables.clear();
            executedRunnables.addAll(runnables);
            runnables.clear();
        }
        for (int i = 0; i < executedRunnables.size; i++) {
            try {
                executedRunnables.get(i).run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public Clipboard getClipboard() {
        return new Clipboard() {
            @Override
            public void setContents(String content) {
                UIPasteboard.getGeneralPasteboard().setString(content);
            }

            @Override
            public boolean hasContents() {
                if (Foundation.getMajorSystemVersion() >= 10) {
                    return UIPasteboard.getGeneralPasteboard().hasStrings();
                }

                String contents = getContents();
                return contents != null && !contents.isEmpty();
            }

            @Override
            public String getContents() {
                return UIPasteboard.getGeneralPasteboard().getString();
            }
        };
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.removeValue(listener, true);
        }
    }

    /**
     * Add a listener to handle events from the libgdx root view controller
     *
     * @param listener The {#link IOSViewControllerListener} to add
     */
    public void addViewControllerListener(IOSViewControllerListener listener) {
        viewControllerListener = listener;
    }
}
