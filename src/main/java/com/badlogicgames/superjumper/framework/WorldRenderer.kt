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

import com.badlogicgames.superjumper.framework.Settings.load
import com.badlogicgames.superjumper.framework.Assets.load
import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2

class WorldRenderer(batch: SpriteBatch, var world: World) {
    var cam: OrthographicCamera
    var batch: SpriteBatch
    fun render() {
        if (world.bob.position.y > cam.position.y) cam.position.y = world.bob.position.y
        cam.update()
        batch.projectionMatrix = cam.combined
        renderBackground()
        renderObjects()
    }

    fun renderBackground() {
        batch.disableBlending()
        batch.begin()
        batch.draw(
            Assets.backgroundRegion,
            cam.position.x - FRUSTUM_WIDTH / 2,
            cam.position.y - FRUSTUM_HEIGHT / 2,
            FRUSTUM_WIDTH,
            FRUSTUM_HEIGHT
        )
        batch.end()
    }

    fun renderObjects() {
        batch.enableBlending()
        batch.begin()
        renderBob()
        renderPlatforms()
        renderItems()
        renderSquirrels()
        renderCastle()
        batch.end()
    }

    private fun renderBob() {
        val keyFrame: TextureRegion?
        keyFrame = when (world.bob.state) {
            Bob.BOB_STATE_FALL -> Assets.bobFall!!.getKeyFrame(
                world.bob.stateTime, Animation.ANIMATION_LOOPING
            )
            Bob.BOB_STATE_JUMP -> Assets.bobJump!!.getKeyFrame(
                world.bob.stateTime, Animation.ANIMATION_LOOPING
            )
            Bob.BOB_STATE_HIT -> Assets.bobHit
            else -> Assets.bobHit
        }
        val side: Float = if (world.bob.velocity.x < 0) -1f else 1f
        if (side < 0) batch.draw(
            keyFrame,
            world.bob.position.x + 0.5f,
            world.bob.position.y - 0.5f,
            side * 1,
            1f
        ) else batch.draw(
            keyFrame,
            world.bob.position.x - 0.5f,
            world.bob.position.y - 0.5f,
            side * 1,
            1f
        )
    }

    private fun renderPlatforms() {
        val len = world.platforms.size
        for (i in 0 until len) {
            val platform = world.platforms[i]
            var keyFrame = Assets.platform
            if (platform!!.state == Platform.PLATFORM_STATE_PULVERIZING) {
                keyFrame = Assets.brakingPlatform!!.getKeyFrame(
                    platform.stateTime,
                    Animation.ANIMATION_NONLOOPING
                )
            }
            batch.draw(keyFrame, platform.position.x - 1, platform.position.y - 0.25f, 2f, 0.5f)
        }
    }

    private fun renderItems() {
        var len = world.springs.size
        for (i in 0 until len) {
            val spring = world.springs[i]
            batch.draw(Assets.spring, spring!!.position.x - 0.5f, spring.position.y - 0.5f, 1f, 1f)
        }
        len = world.coins.size
        for (i in 0 until len) {
            val coin = world.coins[i]
            val keyFrame = Assets.coinAnim!!.getKeyFrame(
                coin!!.stateTime, Animation.ANIMATION_LOOPING
            )
            batch.draw(keyFrame, coin.position.x - 0.5f, coin.position.y - 0.5f, 1f, 1f)
        }
    }

    private fun renderSquirrels() {
        val len = world.squirrels.size
        for (i in 0 until len) {
            val squirrel = world.squirrels[i]
            val keyFrame = Assets.squirrelFly!!.getKeyFrame(
                squirrel!!.stateTime, Animation.ANIMATION_LOOPING
            )
            val side: Float = if (squirrel.velocity.x < 0) -1f else 1f
            if (side < 0) batch.draw(
                keyFrame,
                squirrel.position.x + 0.5f,
                squirrel.position.y - 0.5f,
                side * 1,
                1f
            ) else batch.draw(
                keyFrame,
                squirrel.position.x - 0.5f,
                squirrel.position.y - 0.5f,
                side * 1,
                1f
            )
        }
    }

    private fun renderCastle() {
        val castle = world.castle
        batch.draw(Assets.castle, castle!!.position.x - 1, castle.position.y - 1, 2f, 2f)
    }

    companion object {
        const val FRUSTUM_WIDTH = 10f
        const val FRUSTUM_HEIGHT = 15f
    }

    init {
        cam = OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT)
        cam.position[FRUSTUM_WIDTH / 2, FRUSTUM_HEIGHT / 2] = 0f
        this.batch = batch
    }
}