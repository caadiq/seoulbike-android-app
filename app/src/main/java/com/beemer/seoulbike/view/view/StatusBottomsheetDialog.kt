package com.beemer.seoulbike.view.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.beemer.seoulbike.R
import com.beemer.seoulbike.databinding.DialogBottomsheetStatusBinding
import com.beemer.seoulbike.model.dto.NearbyStationListDto
import com.beemer.seoulbike.view.utils.DateTimeConverter.convertDateTime
import com.beemer.seoulbike.view.utils.UnitConversion.formatDistance
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class StatusBottomsheetDialog(private val item: NearbyStationListDto) : BottomSheetDialogFragment() {
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
        item.stationStatus.updateTime?.let { binding.txtUpdate.text = convertDateTime(it, "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss", Locale.KOREA) }
        binding.txtName.text = "${item.stationNo.replace("^0+".toRegex(), "")}. ${item.stationNm}"
        binding.txtParking.text = item.stationStatus.parkingCnt.toString()
        binding.txtRack.text = item.stationStatus.rackCnt.toString()
        binding.txtDistance.text = item.distance?.let { formatDistance(it) }

        binding.txtParking.setTextColor(
            if (item.stationStatus.parkingCnt == 0) {
                ContextCompat.getColor(binding.root.context, R.color.red)
            } else {
                ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
            }
        )
    }
}