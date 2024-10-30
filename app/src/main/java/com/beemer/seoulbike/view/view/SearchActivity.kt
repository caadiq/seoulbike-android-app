package com.beemer.seoulbike.view.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.beemer.seoulbike.databinding.ActivitySearchBinding
import com.beemer.seoulbike.viewmodel.SearchFragmentType
import com.beemer.seoulbike.viewmodel.SearchHistoryViewModel
import com.beemer.seoulbike.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }

    private val searchViewModel by viewModels<SearchViewModel>()
    private val searchHistoryViewModel by viewModels<SearchHistoryViewModel>()

    private val imm by lazy { getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (searchViewModel.query.value.isNullOrEmpty()) {
                finish()
            } else {
                when (searchViewModel.currentFragmentType.value) {
                    SearchFragmentType.SEARCH1 -> {
                        searchViewModel.setCurrentFragment(1)
                        binding.editSearch.clearFocus()
                    }
                    SearchFragmentType.SEARCH2 -> finish()
                    else -> finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setupView()
        setupFragment()
        setupViewModel()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {
        binding.editSearch.apply {
            requestFocus()
            setOnEditorActionListener { _, _, _ ->
                val text = text.toString().trim()
                if (text.isNotBlank()) {
                    imm.hideSoftInputFromWindow(windowToken, 0)
                    clearFocus()

                    searchHistoryViewModel.checkTitleExists(text)
                    searchViewModel.updateQuery(text)
                } else
                    Toast.makeText(this@SearchActivity, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
                true
            }
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    searchViewModel.setCurrentFragment(0)
                }
                false
            }
            postDelayed({
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }
    }

    private fun setupFragment() {
        supportFragmentManager.beginTransaction().apply {
            add(binding.containerView.id, Search1Fragment(), SearchFragmentType.SEARCH1.tag)
            add(binding.containerView.id, Search2Fragment(), SearchFragmentType.SEARCH2.tag)
            commit()
        }

        supportFragmentManager.executePendingTransactions()
        supportFragmentManager.beginTransaction().apply {
            hide(supportFragmentManager.findFragmentByTag(SearchFragmentType.SEARCH2.tag)!!)
            commit()
        }
    }

    private fun setupViewModel() {
        searchViewModel.apply {
            currentFragmentType.observe(this@SearchActivity) { fragmentType ->
                val currentFragment = supportFragmentManager.findFragmentByTag(fragmentType.tag)
                supportFragmentManager.beginTransaction().apply {
                    supportFragmentManager.fragments.forEach { fragment ->
                        if (fragment == currentFragment)
                            show(fragment)
                        else
                            hide(fragment)
                    }
                }.commit()
            }

            query.observe(this@SearchActivity) { query ->
                query?.let {
                    binding.editSearch.apply {
                        imm.hideSoftInputFromWindow(windowToken, 0)
                        clearFocus()

                        setText(query)
                    }
                    searchViewModel.setCurrentFragment(1)
                }
            }
        }
    }
}