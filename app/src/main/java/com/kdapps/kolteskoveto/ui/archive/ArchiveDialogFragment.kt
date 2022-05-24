package com.kdapps.kolteskoveto.ui.archive

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kdapps.kolteskoveto.R
import com.kdapps.kolteskoveto.databinding.DialogArchiveAllBinding

class ArchiveDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "ArchiveDialogFragment"
    }

    interface ArchiveAllDialogListener {
        fun onArchiveCreated(name: String)
    }

    private lateinit var listener: ArchiveAllDialogListener

    private lateinit var binding: DialogArchiveAllBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? ArchiveAllDialogListener
            ?: throw RuntimeException("Activity must implement the ArchiveAllDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogArchiveAllBinding.inflate(LayoutInflater.from(context))

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btnYes.setOnClickListener {
            if (binding.etName.editText?.text.toString().isNotEmpty()){
                listener.onArchiveCreated(binding.etName.editText?.text.toString())
                super.dismiss()
            }else{
                binding.etName.error = getString(R.string.et_required_error)
            }
        }

        binding.btnNo.setOnClickListener {
            super.dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext(), R.style.DarkDialogBackground)
            .setTitle(getString(R.string.confirm_archive_all))
            .setView(binding.root)
            .create()
    }
}