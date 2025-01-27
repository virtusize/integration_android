package com.virtusize.android.auth.views

import androidx.lifecycle.*
import com.virtusize.android.auth.data.SnsType
import com.virtusize.android.auth.data.VirtusizeUser
import com.virtusize.android.auth.repositories.FacebookRepository
import com.virtusize.android.auth.repositories.GoogleRepository
import kotlinx.coroutines.launch

internal class VirtusizeAuthViewModel(
    private val facebookRepository: FacebookRepository,
    private val googleRepository: GoogleRepository
) : ViewModel() {

    private val _virtusizeUser = MutableLiveData<VirtusizeUser>()
    val virtusizeUser: MutableLiveData<VirtusizeUser> = _virtusizeUser

    fun getUserInfo(snsType: SnsType?, accessToken: String) {
        viewModelScope.launch {
            if (snsType == SnsType.FACEBOOK) {
                _virtusizeUser.value = facebookRepository.getUser(accessToken)
            } else if (snsType == SnsType.GOOGLE) {
                _virtusizeUser.value = googleRepository.getUser(accessToken)
            }

        }
    }
}