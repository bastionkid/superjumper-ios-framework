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

class Animation(val frameDuration: Float, vararg val keyFrames: TextureRegion) {
    fun getKeyFrame(stateTime: Float, mode: Int): TextureRegion {
        var frameNumber = (stateTime / frameDuration).toInt()
        frameNumber =
            if (mode == ANIMATION_NONLOOPING) {
                Math.min(keyFrames.size - 1, frameNumber)
            } else {
                frameNumber % keyFrames.size
            }
        return keyFrames[frameNumber]
    }

    companion object {
        const val ANIMATION_LOOPING = 0
        const val ANIMATION_NONLOOPING = 1
    }

}