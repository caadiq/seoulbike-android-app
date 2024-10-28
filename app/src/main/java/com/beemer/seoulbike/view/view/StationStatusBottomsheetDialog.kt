package com.beemer.seoulbike.view.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beemer.seoulbike.databinding.BottomsheetdialogStationStatusBinding
import com.beemer.seoulbike.model.dto.StationListDto
import com.beemer.seoulbike.view.utils.DateTimeConverter.convertDateTime
import com.beemer.seoulbike.view.utils.UnitConversion.formatDistance
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class StationStatusBottomsheetDialog(private val item: StationListDto) : BottomSheetDialogFragment() {
    private var _binding: BottomsheetdialogStationStatusBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetdialogStationStatusBinding.inflate(inflater, container, false)
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
        binding.txtQrBike.text = item.stationStatus.qrBikeCnt.toString()
        binding.txtElecBike.text = item.stationStatus.elecBikeCnt.toString()
        binding.txtDistance.text = item.distance?.let { formatDistance(it) }
    }
}