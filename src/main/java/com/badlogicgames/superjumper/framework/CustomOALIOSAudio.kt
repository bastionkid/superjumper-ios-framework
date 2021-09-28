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
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle

class CustomOALIOSAudio(config: CustomIOSApplicationConfiguration) : IOSAudio {
    override fun newAudioDevice(samplingRate: Int, isMono: Boolean): AudioDevice? {
        // TODO Auto-generated method stub
        return null
    }

    override fun newAudioRecorder(samplingRate: Int, isMono: Boolean): AudioRecorder? {
        // TODO Auto-generated method stub
        return null
    }

    override fun newSound(fileHandle: FileHandle): Sound {
        return IOSSound(fileHandle)
    }

    override fun newMusic(fileHandle: FileHandle): Music {
        val path = fileHandle.file().path.replace('\\', '/')
        val track = OALAudioTrack.create()
        if (track != null) {
            if (track.preloadFile(path)) {
                return IOSMusic(track)
            }
        }
        throw GdxRuntimeException("Error opening music file at $path")
    }

    init {
        if (config.useAudio) {
            val audio = OALSimpleAudio.sharedInstance()
            if (audio != null) {
                audio.isAllowIpod = config.allowIpod
                audio.isHonorSilentSwitch = !config.overrideRingerSwitch
            } else Gdx.app.error(
                "IOSAudio",
                "No OALSimpleAudio instance available, audio will not be availabe"
            )
        }
    }
}