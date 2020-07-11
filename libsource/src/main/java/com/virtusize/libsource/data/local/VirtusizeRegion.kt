package com.virtusize.libsource.data.local

// TODO
enum class VirtusizeRegion(val value: String) {
    COM("com") {
        override fun defaultLanguage(): VirtusizeLanguage {
            return VirtusizeLanguage.EN
        }
    },
    JP("jp") {
        override fun defaultLanguage(): VirtusizeLanguage {
            return VirtusizeLanguage.JP
        }
    },
    KR("kr") {
        override fun defaultLanguage(): VirtusizeLanguage {
            return VirtusizeLanguage.KR
        }
    };

    abstract fun defaultLanguage(): VirtusizeLanguage
}