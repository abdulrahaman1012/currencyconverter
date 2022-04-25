package com.absa.currencyconverter.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.absa.currencyconverter.databinding.ActivityConverterBinding
import java.util.*
import kotlin.collections.ArrayList
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.absa.currencyconverter.BuildConfig
import com.absa.currencyconverter.R
import com.google.android.material.snackbar.Snackbar
import extension.isOnline
import utils.EMPTY_STRING
import utils.Util
import viewmodel.ConverterViewModel


class ConverterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConverterBinding
    private var selectedSourceCurrency: String? = "AFN"
    private var selectedDestinationCurrency: String? = "AFN"

    private lateinit var viewModel: ConverterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ConverterViewModel::class.java]
        initSpinners()
        binding.btnConvert.setOnClickListener {
            doConversion()
        }
    }

    private fun initSpinners() {
        val fromSpinner = binding.txtFrom
        val adapterSourceCurrency: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getAllCountries())
        fromSpinner.setAdapter(adapterSourceCurrency)
        fromSpinner.setOnItemClickListener { _, _, _, _ ->
            selectedSourceCurrency = getSymbol(getCountryCode(fromSpinner.text.toString()))
            binding.txtSourceCurrencyName.text = selectedSourceCurrency
            Util.hideKeyboard(this)
        }

        val toSpinner = binding.txtTo
        val adapterDestinationCurrency: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getAllCountries())
        toSpinner.setAdapter(adapterDestinationCurrency)
        toSpinner.setOnItemClickListener { _, _, _, _ ->
            selectedDestinationCurrency = getSymbol(getCountryCode(toSpinner.text.toString()))
            binding.txtDestinationCurrencyName.text = selectedDestinationCurrency
            Util.hideKeyboard(this)
        }
    }

    private fun getAllCountries(): ArrayList<String> {
        val locales = Locale.getAvailableLocales()
        val countries = ArrayList<String>()
        for (locale in locales) {
            val country = locale.displayCountry
            if (country.trim { it <= ' ' }.isNotEmpty() && !countries.contains(country)) {
                countries.add(country)
            }
        }
        countries.sort()
        return countries
    }

    private fun getSymbol(countryCode: String?): String? {
        val availableLocales = Locale.getAvailableLocales()
        for (i in availableLocales.indices) {
            if (availableLocales[i].country == countryCode
            ) return Currency.getInstance(availableLocales[i]).currencyCode
        }
        return EMPTY_STRING
    }

    private fun getCountryCode(countryName: String) =
        Locale.getISOCountries().find { Locale("", it).displayCountry == countryName }

    private fun doConversion() {
        Util.hideKeyboard(this)
        val amountToConvert = binding.edtAmount.text.toString()
        if (amountToConvert.isEmpty() || amountToConvert == "0") {
            Snackbar.make(
                binding.mainLayout,
                getString(R.string.blank_field),
                Snackbar.LENGTH_LONG
            )
                .setTextColor(ContextCompat.getColor(this, android.R.color.white))
                .show()

        } else if (!Util.isNetworkAvailable(this)) {
            Snackbar.make(
                binding.mainLayout,
                getString(R.string.network_error),
                Snackbar.LENGTH_LONG
            )
                .setTextColor(ContextCompat.getColor(this, android.R.color.white))
                .show()
        } else {
            getConvertedData()
        }
    }

    private fun getConvertedData() {
        binding.prgLoading.visibility = View.VISIBLE
        viewModel.getConvertedData(
            BuildConfig.API_KEY,
            selectedSourceCurrency,
            selectedDestinationCurrency,
            binding.edtAmount.text.toString().toDouble()
        )
        viewModel.getConvertedDataObserver().observe(this, { it ->
            if (it != null) {
                val map = it.rates
                map.keys.forEach {
                    val rateForAmount = map[it]?.rate_for_amount
                    val formattedString = String.format("%,.2f", rateForAmount)
                    binding.edtDestinationCurrency.setText(formattedString)

                }
                binding.prgLoading.visibility = View.GONE
            } else {
                binding.prgLoading.visibility = View.GONE
                Toast.makeText(
                    this,
                    getString(R.string.error_load_data),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        )
    }
}