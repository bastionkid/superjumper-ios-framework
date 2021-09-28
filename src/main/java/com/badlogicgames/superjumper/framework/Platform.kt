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

class Platform(var type: Int, x: Float, y: Float) :
    DynamicGameObject(x, y, PLATFORM_WIDTH, PLATFORM_HEIGHT) {
    @JvmField
    var state: Int
    @JvmField
    var stateTime: Float
    fun update(deltaTime: Float) {
        if (type == PLATFORM_TYPE_MOVING) {
            position.add(velocity.x * deltaTime, 0f)
            bounds.x = position.x - PLATFORM_WIDTH / 2
            bounds.y = position.y - PLATFORM_HEIGHT / 2
            if (position.x < PLATFORM_WIDTH / 2) {
                velocity.x = -velocity.x
                position.x = PLATFORM_WIDTH / 2
            }
            if (position.x > World.WORLD_WIDTH - PLATFORM_WIDTH / 2) {
                velocity.x = -velocity.x
                position.x = World.WORLD_WIDTH - PLATFORM_WIDTH / 2
            }
        }
        stateTime += deltaTime
    }

    fun pulverize() {
        state = PLATFORM_STATE_PULVERIZING
        stateTime = 0f
        velocity.x = 0f
    }

    companion object {
        const val PLATFORM_WIDTH = 2f
        const val PLATFORM_HEIGHT = 0.5f
        const val PLATFORM_TYPE_STATIC = 0
        const val PLATFORM_TYPE_MOVING = 1
        const val PLATFORM_STATE_NORMAL = 0
        const val PLATFORM_STATE_PULVERIZING = 1
        const val PLATFORM_PULVERIZE_TIME = 0.2f * 4
        const val PLATFORM_VELOCITY = 2f
    }

    init {
        state = PLATFORM_STATE_NORMAL
        stateTime = 0f
        if (type == PLATFORM_TYPE_MOVING) {
            velocity.x = PLATFORM_VELOCITY
        }
    }
}