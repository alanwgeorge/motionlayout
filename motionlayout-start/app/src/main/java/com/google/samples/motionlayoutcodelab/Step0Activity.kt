/*
 *   Copyright (C) 2019 The Android Open Source Project
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.google.samples.motionlayoutcodelab

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.samples.motionlayoutcodelab.databinding.ActivityStep0Binding

import kotlinx.android.synthetic.main.activity_step1.*
import java.util.*
import kotlin.collections.ArrayList

class Step0Activity : AppCompatActivity() {
    lateinit var binding: ActivityStep0Binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityStep0Binding>(this, R.layout.activity_step0).apply {
            binding = this
            dynamicLayout.addTransitionListener(object: MotionLayout.TransitionListener {
                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                    Log.d("Step0", "onTransitionStarted: $p1 $p2}")
                }

                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                    Log.d("Step0", "onTransitionChange $p3")
                }

                override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                    Log.d("Step0", "onTransitionCompleted: $p1}")
                }

                override fun onTransitionTrigger(
                    p0: MotionLayout?,
                    p1: Int,
                    p2: Boolean,
                    p3: Float
                ) {
                    Log.d("Step0", "onTransitionTrigger")
                }
            })

            button.setOnClickListener {
                dynamicLayout.animateWidget()
            }
        }
    }
}

