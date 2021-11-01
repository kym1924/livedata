package com.kimym.translator.presentation.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.kimym.translator.R
import com.kimym.translator.databinding.ActivityMainBinding
import com.kimym.translator.presentation.country.CountryBottomSheet
import com.kimym.translator.util.copy
import com.kimym.translator.util.hideKeyboard
import com.kimym.translator.util.rotationAnimation
import com.kimym.translator.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this@MainActivity
        setObserve()
        setClickListener()
    }

    private fun setObserve() {
        viewModel.isSrcBottomSheet.observe(this@MainActivity) {
            it.getContentIfNotHandled()?.let {
                hideKeyboard()
                CountryBottomSheet().show(supportFragmentManager, CountryBottomSheet.TAG)
            }
        }

        viewModel.snackBar.observe(this@MainActivity) {
            it.getContentIfNotHandled()?.let { msg ->
                binding.root.showSnackBar(msg)
            }
        }

        viewModel.swapLang.observe(this@MainActivity) {
            it.getContentIfNotHandled()?.let { swap ->
                when (swap) {
                    true -> {
                        binding.btnLanguageChange.rotationAnimation()
                    }
                }
            }
        }
    }

    private fun setClickListener() {
        binding.layout.setOnClickListener {
            hideKeyboard()
        }

        binding.btnCopyText.setOnClickListener {
            copy(binding.tvTranslated.text.toString())
            viewModel.setSnackBar(getString(R.string.make_copy))
        }
    }
}
