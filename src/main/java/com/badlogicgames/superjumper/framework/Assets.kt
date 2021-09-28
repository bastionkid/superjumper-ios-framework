/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.badlogicgames.superjumper.framework

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture

object Assets {
    var background: Texture? = null
    @JvmField
	var backgroundRegion: TextureRegion? = null
    var items: Texture? = null
    @JvmField
	var mainMenu: TextureRegion? = null
    @JvmField
	var pauseMenu: TextureRegion? = null
    @JvmField
	var ready: TextureRegion? = null
    @JvmField
	var gameOver: TextureRegion? = null
    @JvmField
	var highScoresRegion: TextureRegion? = null
    @JvmField
	var logo: TextureRegion? = null
    @JvmField
	var soundOn: TextureRegion? = null
    @JvmField
	var soundOff: TextureRegion? = null
    @JvmField
	var arrow: TextureRegion? = null
    @JvmField
	var pause: TextureRegion? = null
    @JvmField
	var spring: TextureRegion? = null
    @JvmField
	var castle: TextureRegion? = null
    @JvmField
	var coinAnim: Animation? = null
    @JvmField
	var bobJump: Animation? = null
    @JvmField
	var bobFall: Animation? = null
    @JvmField
	var bobHit: TextureRegion? = null
    @JvmField
	var squirrelFly: Animation? = null
    @JvmField
	var platform: TextureRegion? = null
    @JvmField
	var brakingPlatform: Animation? = null
    @JvmField
	var font: BitmapFont? = null
    @JvmField
	var music: Music? = null
    @JvmField
	var jumpSound: Sound? = null
    @JvmField
	var highJumpSound: Sound? = null
    @JvmField
	var hitSound: Sound? = null
    @JvmField
	var coinSound: Sound? = null
    @JvmField
	var clickSound: Sound? = null
    @JvmStatic
	fun loadTexture(file: String?): Texture {
        return Texture(Gdx.files.internal(file))
    }

    @JvmStatic
	fun load() {
        background = loadTexture("data/background.png")
        backgroundRegion = TextureRegion(background, 0, 0, 320, 480)
        items = loadTexture("data/items.png")
        mainMenu = TextureRegion(items, 0, 224, 300, 110)
        pauseMenu = TextureRegion(items, 224, 128, 192, 96)
        ready = TextureRegion(items, 320, 224, 192, 32)
        gameOver = TextureRegion(items, 352, 256, 160, 96)
        highScoresRegion = TextureRegion(items, 0, 257, 300, 110 / 3)
        logo = TextureRegion(items, 0, 352, 274, 142)
        soundOff = TextureRegion(items, 0, 0, 64, 64)
        soundOn = TextureRegion(items, 64, 0, 64, 64)
        arrow = TextureRegion(items, 0, 64, 64, 64)
        pause = TextureRegion(items, 64, 64, 64, 64)
        spring = TextureRegion(items, 128, 0, 32, 32)
        castle = TextureRegion(items, 128, 64, 64, 64)
        coinAnim = Animation(
            0.2f, TextureRegion(items, 128, 32, 32, 32), TextureRegion(items, 160, 32, 32, 32),
            TextureRegion(items, 192, 32, 32, 32), TextureRegion(items, 160, 32, 32, 32)
        )
        bobJump = Animation(
            0.2f,
            TextureRegion(items, 0, 128, 32, 32),
            TextureRegion(items, 32, 128, 32, 32)
        )
        bobFall = Animation(
            0.2f,
            TextureRegion(items, 64, 128, 32, 32),
            TextureRegion(items, 96, 128, 32, 32)
        )
        bobHit = TextureRegion(items, 128, 128, 32, 32)
        squirrelFly = Animation(
            0.2f,
            TextureRegion(items, 0, 160, 32, 32),
            TextureRegion(items, 32, 160, 32, 32)
        )
        platform = TextureRegion(items, 64, 160, 64, 16)
        brakingPlatform = Animation(
            0.2f, TextureRegion(items, 64, 160, 64, 16), TextureRegion(items, 64, 176, 64, 16),
            TextureRegion(items, 64, 192, 64, 16), TextureRegion(items, 64, 208, 64, 16)
        )
        font = BitmapFont(
            Gdx.files.internal("data/font.fnt"),
            Gdx.files.internal("data/font.png"),
            false
        )
        music = Gdx.audio.newMusic(Gdx.files.internal("data/music.mp3"))
        music?.isLooping = true
        music?.volume = 0.5f
        if (Settings.soundEnabled) music?.play()
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("data/jump.wav"))
        highJumpSound = Gdx.audio.newSound(Gdx.files.internal("data/highjump.wav"))
        hitSound = Gdx.audio.newSound(Gdx.files.internal("data/hit.wav"))
        coinSound = Gdx.audio.newSound(Gdx.files.internal("data/coin.wav"))
        clickSound = Gdx.audio.newSound(Gdx.files.internal("data/click.wav"))
    }

    @JvmStatic
	fun playSound(sound: Sound) {
        if (Settings.soundEnabled) sound.play(1f)
    }
}