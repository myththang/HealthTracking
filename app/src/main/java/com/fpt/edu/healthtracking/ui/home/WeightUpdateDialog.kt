package com.fpt.edu.healthtracking.ui.home

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class WeightUpdateDialog : DialogFragment() {
    private var onUpdateClickListener: (() -> Unit)? = null

    fun setOnUpdateClickListener(listener: () -> Unit) {
        onUpdateClickListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cập nhật cân nặng")
            .setMessage("Đã hơn 1 tuần kể từ lần cập nhật cân nặng gần nhất. Bạn nên cập nhật cân nặng để theo dõi tiến độ tốt hơn.")
            .setPositiveButton("Cập nhật ngay") { _, _ ->
                onUpdateClickListener?.invoke()
            }
            .setNegativeButton("Để sau") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}