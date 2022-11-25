package spamdetector.zani.util

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class AnimationManager() {
    companion object {
        fun animateMoveAndResize(v: View, dist_x: Float, dist_y: Float, value: Float, duration: Long) {
            var translationX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, dist_x)
            var translationY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dist_y)
            var scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, value)
            var animator = ObjectAnimator.ofPropertyValuesHolder(v, translationX, translationY, scaleY)
            animator.duration = duration
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.start()
        }
        fun animateResize(v: View, value: Float, duration: Long) {
            var scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, value)
            var scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, value)
            var animator = ObjectAnimator.ofPropertyValuesHolder(v, scaleX, scaleY)
            animator.duration = duration
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.start()
        }
        fun animateMove(v: View, dist_x: Float, dist_y: Float, duration: Long) {
            var translationX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, dist_x)
            var translationY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dist_y)
            var animator = ObjectAnimator.ofPropertyValuesHolder(v, translationX, translationY)
            animator.duration = duration
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.start()
        }
    }
}