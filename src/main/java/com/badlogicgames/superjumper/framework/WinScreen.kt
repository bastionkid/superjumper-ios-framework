package com.badlogicgames.superjumper.framework

import com.badlogicgames.superjumper.framework.Settings.load
import com.badlogicgames.superjumper.framework.Assets.load
import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align

class WinScreen(var game: SuperJumper) : ScreenAdapter() {
    var cam: OrthographicCamera
    var princess: TextureRegion
    var messages = arrayOf(
        "Princess: Oh dear!\n What have you done?",
        "Bob: I came to \nrescue you!",
        "Princess: you are\n mistaken\nI need no rescueing",
        "Bob: So all this \nwork for nothing?",
        "Princess: I have \ncake and tea!\nWould you like some?",
        "Bob: I'd be my \npleasure!",
        "And they ate cake\nand drank tea\nhappily ever \nafter\n\n\n\n\n\n\nKära Emma!\nDu är fantastisk!\nDu blev ferdig\n med spelet!"
    )
    var currentMessage = 0
    override fun render(delta: Float) {
        if (Gdx.input.justTouched()) {
            currentMessage++
            if (currentMessage == messages.size) {
                currentMessage--
                game.screen = MainMenuScreen(game)
            }
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        cam.update()
        game.batcher!!.projectionMatrix = cam.combined
        game.batcher!!.begin()
        game.batcher!!.draw(Assets.backgroundRegion, 0f, 0f)
        game.batcher!!.draw(Assets.castle, 60f, 120f, 200f, 200f)
        game.batcher!!.draw(
            Assets.bobFall!!.getKeyFrame(0f, Animation.ANIMATION_LOOPING),
            120f,
            200f
        )
        Assets.font!!.draw(
            game.batcher,
            messages[currentMessage],
            0f,
            400f,
            320f,
            Align.center,
            false
        )
        game.batcher!!.draw(princess, 150f, 200f)
        game.batcher!!.end()
    }

    init {
        cam = OrthographicCamera()
        cam.setToOrtho(false, 320f, 480f)
        princess = TextureRegion(Assets.arrow!!.texture, 210, 122, -40, 38)
    }
}