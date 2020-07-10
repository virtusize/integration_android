package com.virtusize.libsource.data.local

// TODO
enum class AoyamaRegion(val value: String) {
    COM("com") {
        override fun defaultLanguage(): AoyamaLanguage {
            return AoyamaLanguage.EN
        }
    },
    JP("jp") {
        override fun defaultLanguage(): AoyamaLanguage {
            return AoyamaLanguage.JP
        }
    },
    KR("kr") {
        override fun defaultLanguage(): AoyamaLanguage {
            return AoyamaLanguage.KR
        }
    };

    abstract fun defaultLanguage(): AoyamaLanguage
}