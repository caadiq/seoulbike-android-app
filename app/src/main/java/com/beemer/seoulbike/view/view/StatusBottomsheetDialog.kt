package com.beemer.seoulbike.view.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.DialogBottomsheetStatusBinding
import com.beemer.seoulbike.model.dto.NearbyStationListDto
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class StatusBottomsheetDialog(private val item: NearbyStationListDto,  private val findDirection: () -> Unit,) : BottomSheetDialogFragment() {
    private var _binding: DialogBottomsheetStatusBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogBottomsheetStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupView() {
        binding.txtName.text = "${item.stationNo.replace("^0+".toRegex(), "")}. ${item.stationNm}"
        binding.txtParking.text = item.stationStatus.parkingCnt.toString()
        binding.txtRack.text = item.stationStatus.rackCnt.toString()
        binding.txtDistance.text = String.format(Locale.getDefault(), "%.1fm", item.distance)

        binding.txtParking.setTextColor(
            if (item.stationStatus.parkingCnt == 0) {
                ContextCompat.getColor(binding.root.context, R.color.red)
            } else {
                ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
            }
        )

        binding.imgDirection.setOnClickListener {
            findDirection()
            dismiss()
        }
    }
}