package com.truepineapps.photouploader.core.feature.legal.domain.model

import com.truepineapps.photouploader.core.util.UiText

class LegalRemoteException(val uiText: UiText) :
    Exception(uiText.toString())