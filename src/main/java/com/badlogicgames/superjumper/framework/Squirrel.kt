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

import com.badlogicgames.superjumper.framework.Assets.playSound
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.Gdx

class Squirrel(x: Float, y: Float) : DynamicGameObject(x, y, SQUIRREL_WIDTH, SQUIRREL_HEIGHT) {
    @JvmField
    var stateTime = 0f
    fun update(deltaTime: Float) {
        position.add(velocity.x * deltaTime, velocity.y * deltaTime)
        bounds.x = position.x - SQUIRREL_WIDTH / 2
        bounds.y = position.y - SQUIRREL_HEIGHT / 2
        if (position.x < SQUIRREL_WIDTH / 2) {
            position.x = SQUIRREL_WIDTH / 2
            velocity.x = SQUIRREL_VELOCITY
        }
        if (position.x > World.WORLD_WIDTH - SQUIRREL_WIDTH / 2) {
            position.x = World.WORLD_WIDTH - SQUIRREL_WIDTH / 2
            velocity.x = -SQUIRREL_VELOCITY
        }
        stateTime += deltaTime
    }

    companion object {
        const val SQUIRREL_WIDTH = 1f
        const val SQUIRREL_HEIGHT = 0.6f
        const val SQUIRREL_VELOCITY = 3f
    }

    init {
        velocity[SQUIRREL_VELOCITY] = 0f
    }
}