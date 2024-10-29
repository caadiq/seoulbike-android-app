package com.beemer.seoulbike.view.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.beemer.seoulbike.databinding.BottomsheetdialogStationStatusBinding
import com.beemer.seoulbike.model.dto.StationListDto
import com.beemer.seoulbike.model.entity.FavoriteStationEntity
import com.beemer.seoulbike.view.utils.DateTimeConverter.convertDateTime
import com.beemer.seoulbike.view.utils.UnitConversion.formatDistance
import com.beemer.seoulbike.viewmodel.FavoriteStationViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class StationStatusBottomsheetDialog(private val item: StationListDto) : BottomSheetDialogFragment() {
    private var _binding: BottomsheetdialogStationStatusBinding? = null
    private val binding get() = _binding!!

    private val favoriteStationViewModel by viewModels<FavoriteStationViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetdialogStationStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupViewModel()
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

        binding.lottie.apply {
            setOnClickListener {
                if (binding.lottie.progress == 1.0f) {
                    DefaultDialog(
                        title = null,
                        message = "즐겨찾기에서 삭제하시겠습니까?",
                        onConfirm = {
                            favoriteStationViewModel.deleteFavoriteStation(item.stationId)
                            progress = 0.0f
                            cancelAnimation()
                        }
                    ).show(childFragmentManager, "DefaultDialog")
                } else if (binding.lottie.progress == 0.0f) {
                    favoriteStationViewModel.insertFavoriteStation(FavoriteStationEntity(stationId = item.stationId))
                    playAnimation()
                }
            }
        }
    }

    private fun setupViewModel() {
        favoriteStationViewModel.apply {
            checkFavoriteExists(item.stationId)

            isFavoriteExists.observe(viewLifecycleOwner) { exists ->
                if (exists)
                    binding.lottie.playAnimation()
            }
        }
    }
}