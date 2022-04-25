package viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import api.ApiClient
import api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.ApiResponse

class ConverterViewModel : ViewModel() {

    private var currencyLiveData: MutableLiveData<ApiResponse> = MutableLiveData()

    fun getConvertedDataObserver(): LiveData<ApiResponse> {
        return currencyLiveData
    }

    fun getConvertedData(apiKey: String, source: String?, destination: String?, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val apiInterface = ApiClient.getClient().create(ApiService::class.java)
            currencyLiveData.postValue(apiInterface.convertCurrency(apiKey, source, destination, amount))
        }
    }
}