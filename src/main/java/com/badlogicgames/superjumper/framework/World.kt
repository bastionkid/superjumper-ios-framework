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
import java.util.*

class World(listener: WorldListener) {
    interface WorldListener {
        fun jump()
        fun highJump()
        fun hit()
        fun coin()
    }

    val bob: Bob
    val platforms: MutableList<Platform?>
    val springs: MutableList<Spring?>
    val squirrels: MutableList<Squirrel?>
    val coins: MutableList<Coin?>
    var castle: Castle? = null
    val listener: WorldListener
    val rand: Random
    var heightSoFar: Float
    var score: Int
    var state: Int
    private fun generateLevel() {
        var y = Platform.PLATFORM_HEIGHT / 2
        val maxJumpHeight = Bob.BOB_JUMP_VELOCITY * Bob.BOB_JUMP_VELOCITY / (2 * -gravity.y)
        while (y < WORLD_HEIGHT - WORLD_WIDTH / 2) {
            val type =
                if (rand.nextFloat() > 0.8f) Platform.PLATFORM_TYPE_MOVING else Platform.PLATFORM_TYPE_STATIC
            val x =
                rand.nextFloat() * (WORLD_WIDTH - Platform.PLATFORM_WIDTH) + Platform.PLATFORM_WIDTH / 2
            val platform = Platform(type, x, y)
            platforms.add(platform)
            if (rand.nextFloat() > 0.9f && type != Platform.PLATFORM_TYPE_MOVING) {
                val spring = Spring(
                    platform.position.x,
                    platform.position.y + Platform.PLATFORM_HEIGHT / 2 + Spring.SPRING_HEIGHT / 2
                )
                springs.add(spring)
            }
            if (y > WORLD_HEIGHT / 3 && rand.nextFloat() > 0.8f) {
                val squirrel = Squirrel(
                    platform.position.x + rand.nextFloat(), platform.position.y
                            + Squirrel.SQUIRREL_HEIGHT + rand.nextFloat() * 2
                )
                squirrels.add(squirrel)
            }
            if (rand.nextFloat() > 0.6f) {
                val coin = Coin(
                    platform.position.x + rand.nextFloat(), platform.position.y + Coin.COIN_HEIGHT
                            + rand.nextFloat() * 3
                )
                coins.add(coin)
            }
            y += maxJumpHeight - 0.5f
            y -= rand.nextFloat() * (maxJumpHeight / 3)
        }
        castle = Castle(WORLD_WIDTH / 2, y)
    }

    fun update(deltaTime: Float, accelX: Float) {
        updateBob(deltaTime, accelX)
        updatePlatforms(deltaTime)
        updateSquirrels(deltaTime)
        updateCoins(deltaTime)
        if (bob.state != Bob.BOB_STATE_HIT) checkCollisions()
        checkGameOver()
    }

    private fun updateBob(deltaTime: Float, accelX: Float) {
        if (bob.state != Bob.BOB_STATE_HIT && bob.position.y <= 0.5f) bob.hitPlatform()
        if (bob.state != Bob.BOB_STATE_HIT) bob.velocity.x = -accelX / 10 * Bob.BOB_MOVE_VELOCITY
        bob.update(deltaTime)
        heightSoFar = Math.max(bob.position.y, heightSoFar)
    }

    private fun updatePlatforms(deltaTime: Float) {
        var len = platforms.size
        for (i in 0 until len) {
            val platform = platforms[i]
            platform!!.update(deltaTime)
            if (platform.state == Platform.PLATFORM_STATE_PULVERIZING && platform.stateTime > Platform.PLATFORM_PULVERIZE_TIME) {
                platforms.remove(platform)
                len = platforms.size
            }
        }
    }

    private fun updateSquirrels(deltaTime: Float) {
        val len = squirrels.size
        for (i in 0 until len) {
            val squirrel = squirrels[i]
            squirrel!!.update(deltaTime)
        }
    }

    private fun updateCoins(deltaTime: Float) {
        val len = coins.size
        for (i in 0 until len) {
            val coin = coins[i]
            coin!!.update(deltaTime)
        }
    }

    private fun checkCollisions() {
        checkPlatformCollisions()
        checkSquirrelCollisions()
        checkItemCollisions()
        checkCastleCollisions()
    }

    private fun checkPlatformCollisions() {
        if (bob.velocity.y > 0) return
        val len = platforms.size
        for (i in 0 until len) {
            val platform = platforms[i]
            if (bob.position.y > platform!!.position.y) {
                if (bob.bounds.overlaps(platform.bounds)) {
                    bob.hitPlatform()
                    listener.jump()
                    if (rand.nextFloat() > 0.5f) {
                        platform.pulverize()
                    }
                    break
                }
            }
        }
    }

    private fun checkSquirrelCollisions() {
        val len = squirrels.size
        for (i in 0 until len) {
            val squirrel = squirrels[i]
            if (squirrel!!.bounds.overlaps(bob.bounds)) {
                bob.hitSquirrel()
                listener.hit()
            }
        }
    }

    private fun checkItemCollisions() {
        var len = coins.size
        for (i in 0 until len) {
            val coin = coins[i]
            if (bob.bounds.overlaps(coin!!.bounds)) {
                coins.remove(coin)
                len = coins.size
                listener.coin()
                score += Coin.COIN_SCORE
            }
        }
        if (bob.velocity.y > 0) return
        len = springs.size
        for (i in 0 until len) {
            val spring = springs[i]
            if (bob.position.y > spring!!.position.y) {
                if (bob.bounds.overlaps(spring.bounds)) {
                    bob.hitSpring()
                    listener.highJump()
                }
            }
        }
    }

    private fun checkCastleCollisions() {
        if (castle!!.bounds.overlaps(bob.bounds)) {
            state = WORLD_STATE_NEXT_LEVEL
        }
    }

    private fun checkGameOver() {
        if (heightSoFar - 7.5f > bob.position.y) {
            state = WORLD_STATE_GAME_OVER
        }
    }

    companion object {
        const val WORLD_WIDTH = 10f
        const val WORLD_HEIGHT = (15 * 20).toFloat()
        const val WORLD_STATE_RUNNING = 0
        const val WORLD_STATE_NEXT_LEVEL = 1
        const val WORLD_STATE_GAME_OVER = 2
        val gravity = Vector2(0f, -12f)
    }

    init {
        bob = Bob(5f, 1f)
        platforms = ArrayList()
        springs = ArrayList()
        squirrels = ArrayList()
        coins = ArrayList()
        this.listener = listener
        rand = Random()
        generateLevel()
        heightSoFar = 0f
        score = 0
        state = WORLD_STATE_RUNNING
    }
}