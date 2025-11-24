package com.example.appstorefit_grupo1.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appstorefit_grupo1.data.remote.RemoteModule
import com.example.appstorefit_grupo1.data.remote.ServiceUrls
import com.example.appstorefit_grupo1.data.remote.orders.OrdersApi
import com.example.appstorefit_grupo1.data.repository.OrdersRepository

class OrdersViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {

            val api = RemoteModule.create(
                ServiceUrls.ORDERS_BASE_URL,
                OrdersApi::class.java
            )

            val repo = OrdersRepository(api)

            return OrdersViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}