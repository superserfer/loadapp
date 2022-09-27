package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates
import androidx.core.content.withStyledAttributes

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var heightTextLocation = 0f
    private var title = "Download"
    private var disabled = false
    var progressPercent = 0f

    private var valueAnimator = ValueAnimator()

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new) {
            ButtonState.Clicked -> {
                title = "Clicked!"
                invalidate()
            }
            ButtonState.Loading -> {
                disabled = true
                title = "Loading!"
                invalidate()
                valueAnimator = ValueAnimator.ofFloat(0f, 100f)
                valueAnimator.duration = 5000
                valueAnimator.addUpdateListener { animation ->
                    progressPercent = animation.animatedValue as Float
                    invalidate()
                }
                valueAnimator.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        progressPercent = 0f
                        if(buttonState == ButtonState.Loading){
                            buttonState = ButtonState.Loading
                        }
                    }
                })
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
                disabled = false
                progressPercent = 0f
                title = "Download"
                invalidate()
            }
        }
    }

    private val paintRect = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
        color = ContextCompat.getColor(context, R.color.white)
    }

    private val paintLoader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    }

    private val paintArc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    init {
        isClickable = true
        context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0).apply {
            try {
                progressPercent = getFloat(R.styleable.LoadingButton_progressPercent, 0f)
            } finally {
                recycle()
            }
        }
    }

    override fun performClick(): Boolean {
        if (disabled) return false else {
            buttonState = ButtonState.Clicked
            super.performClick()
            return true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        widthSize = w;
        heightSize = h;
        heightTextLocation = (h/2 + resources.getDimension(R.dimen.default_text_size)/3)
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRect(0f,0f, widthSize.toFloat(), heightSize.toFloat(), paintRect)
        canvas?.drawRect(0f,0f, widthSize.toFloat()/100f * progressPercent, heightSize.toFloat(), paintLoader)
        canvas?.drawText(title, (widthSize/2).toFloat(), heightTextLocation, paintText)
        val oval: RectF = RectF(widthSize/2f + 160f, heightSize/2f - 40f, widthSize/2f + 240f, heightSize/2f + 40f)
        canvas?.drawArc(oval, 0f, 360f/100f * progressPercent, true, paintArc)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}