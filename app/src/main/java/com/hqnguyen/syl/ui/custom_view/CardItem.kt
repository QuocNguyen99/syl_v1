package com.hqnguyen.syl.ui.custom_view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.hqnguyen.syl.R
import com.hqnguyen.syl.databinding.CardItemBinding

class CardItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding =
        CardItemBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.withStyledAttributes(attrs, R.styleable.CardItem) {
            val title = getString(R.styleable.CardItem_carTitle)
            title?.let {
                setTitle(it)
            }

            val description = getString(R.styleable.CardItem_cardDescription)
            setDescription(description)

            val logo = getDrawable(R.styleable.CardItem_cardLogo)
            logo?.let {
                setLogo(it)
            }

            val isShowSwitch = getBoolean(R.styleable.CardItem_cardSwitch, false)
            isShowSwitch?.let {
                switchShow(it)
            }
        }
    }

    private fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    private fun setDescription(description: String?) {
        if (description.isNullOrEmpty()) {
            binding.tvDescription.isVisible = false
        } else {
            binding.tvDescription.text = description
        }
    }

    private fun setLogo(logo: Drawable) {
        binding.imgLogo.setImageDrawable(logo)
    }

    private fun switchShow(isShow: Boolean) {
        binding.sw.isVisible = isShow
        binding.iconArrow.isVisible = !isShow
    }

    public fun setOnClick(callback: OnClickListener) {
        binding.root.setOnClickListener {
            callback.onClick(it)
        }
    }

}
