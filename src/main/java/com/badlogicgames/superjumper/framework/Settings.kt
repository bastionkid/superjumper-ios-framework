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
import java.lang.Boolean

object Settings {
    var soundEnabled = true
    var highscores = intArrayOf(100, 80, 50, 30, 10)
    const val file = ".superjumper"
    @JvmStatic
    fun load() {
        try {
            val filehandle = Gdx.files.external(file)
            val strings = filehandle.readString().split("\n").toTypedArray()
            soundEnabled = Boolean.parseBoolean(
                strings[0]
            )
            for (i in 0..4) {
                highscores[i] = strings[i + 1].toInt()
            }
        } catch (e: Throwable) {
            // :( It's ok we have defaults
        }
    }

    fun save() {
        try {
            val filehandle = Gdx.files.external(file)
            filehandle.writeString(
                """
    ${Boolean.toString(soundEnabled)}
    
    """.trimIndent(), false
            )
            for (i in 0..4) {
                filehandle.writeString(
                    """
    ${Integer.toString(highscores[i])}
    
    """.trimIndent(), true
                )
            }
        } catch (e: Throwable) {
        }
    }

    fun addScore(score: Int) {
        for (i in 0..4) {
            if (highscores[i] < score) {
                for (j in 4 downTo i + 1) highscores[j] = highscores[j - 1]
                highscores[i] = score
                break
            }
        }
    }
}