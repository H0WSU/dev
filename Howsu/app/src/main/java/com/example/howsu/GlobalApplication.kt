package com.example.howsu // (1. 본인 패키지 이름)

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

// (Application을 상속받도록 수정)
class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 2. 카카오 SDK 초기화
        KakaoSdk.init(this, "1a7e2a324c2da34b577405598eef2d03")
    }
}