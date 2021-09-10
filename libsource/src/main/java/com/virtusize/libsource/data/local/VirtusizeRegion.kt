package com.virtusize.libsource.data.local

/**
 * This enum contains all the possible regions that set the region of the config url domains within the Virtusize web app
 */
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

    /**
     * The abstract method to get the default display language of the Virtusize web app corresponding to the [VirtusizeRegion] value
     */
    abstract fun defaultLanguage(): VirtusizeLanguage
}
