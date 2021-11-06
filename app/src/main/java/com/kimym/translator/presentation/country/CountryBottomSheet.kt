package com.kimym.translator.presentation.country

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kimym.translator.data.entity.Country
import com.kimym.translator.databinding.LayoutCountryBottomSheetBinding
import com.kimym.translator.presentation.main.MainViewModel

class CountryBottomSheet : BottomSheetDialogFragment() {
    private var _binding: LayoutCountryBottomSheetBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel by activityViewModels<MainViewModel>()
    private val countryAdapter by lazy { CountryAdapter { country -> itemClick(country) } }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutCountryBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRvCountry()
        initCountryList()
        setBtnDismissClickListener()
    }

    private fun initRvCountry() {
        with(binding.rvCountry) {
            adapter = countryAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
    }

    private fun initCountryList() {
        viewModel.countryList.observe(viewLifecycleOwner) {
            countryAdapter.submitList(it)
        }
    }

    private fun setBtnDismissClickListener() {
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }
    }

    private fun itemClick(country: Country) {
        viewModel.setLanguage(country)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BottomSheetDialog"
    }
}
