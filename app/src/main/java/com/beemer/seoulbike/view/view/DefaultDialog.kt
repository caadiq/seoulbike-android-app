package com.beemer.seoulbike.view.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.beemer.seoulbike.databinding.DialogDefaultBinding

class DefaultDialog(
    private val title: String? = null,
    private val message: String,
    private val canCancel: Boolean = false,
    private val cancelText: String = "취소",
    private val confirmText: String = "확인",
    private val onConfirm: () -> Unit,
    private val onCancel: (() -> Unit)? = null
) : DialogFragment() {
    private var _binding: DialogDefaultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDefaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialog()
        setupView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupDialog() {
        dialog?.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.attributes?.width = (context.resources.displayMetrics.widthPixels.times(0.8)).toInt()
        }
    }

    private fun setupView() {
        binding.txtTitle.apply {
            visibility = if (title.isNullOrEmpty()) View.GONE else View.VISIBLE
            text = title
        }

        binding.txtMessage.text = message

        binding.txtCancel.apply {
            text = cancelText
            visibility = if (canCancel) View.VISIBLE else View.GONE
            setOnClickListener {
                onCancel?.invoke()
                dismiss()
            }
        }

        binding.txtConrifm.apply {
            text = confirmText
            setOnClickListener {
                onConfirm()
                dismiss()
            }
        }
    }
}