package com.virtusize.libsource.data.parsers

import android.content.Context
import com.virtusize.libsource.R
import com.virtusize.libsource.data.remote.I18nLocalization
import com.virtusize.libsource.util.I18nConstants
import org.json.JSONObject

/**
 * This class parses a JSONObject to the [I18nLocalization] object
 */
internal class I18nLocalizationJsonParser(val context: Context): VirtusizeJsonParser {

    enum class TrimType {
        ONELINE, MULTIPLELINES
    }

    override fun parse(json: JSONObject): I18nLocalization? {
        val aoyamaJSONObject = json.optJSONObject(FIELD_KEYS)?.optJSONObject(FIELD_APPS)?.optJSONObject(FIELD_AOYAMA)
        val inpageJSONObject = aoyamaJSONObject?.optJSONObject(FIELD_INPAGE)
        val detailsScreenJSONObject = aoyamaJSONObject?.optJSONObject(FIELD_DETAILS_SCREEN)
        val sizingJSONObject = detailsScreenJSONObject?.optJSONObject(FIELD_SIZING)
        val itemBrandJSONObject = sizingJSONObject?.optJSONObject(FIELD_ITEM_BRAND)
        val mostBrandsJSONObject = sizingJSONObject?.optJSONObject(FIELD_MOST_BRANDS)
        val fitJSONObject = detailsScreenJSONObject?.optJSONObject(FIELD_FIT)

        val defaultText = detailsScreenJSONObject?.optString(
            FIELD_DEFAULT_TEXT,
            context.getString(R.string.inpage_default_text)
        ) ?: ""
        val defaultAccessoryText = inpageJSONObject?.optString(
            FIELD_DEFAULT_ACCESSORY_TEXT,
            context.getString(R.string.inpage_default_accessory_text)
        ) ?: ""

        val sizingItemBrandLargeText = itemBrandJSONObject?.optString(
            FIELD_LARGE_SIZE,
            context.getString(R.string.inpage_sizing_itemBrand_large_text)
        ) ?: ""
        val sizingItemBrandTrueText = itemBrandJSONObject?.optString(
            FIELD_TRUE_SIZE,
            context.getString(R.string.inpage_sizing_itemBrand_true_text)
        ) ?: ""
        val sizingItemBrandSmallText = itemBrandJSONObject?.optString(
            FIELD_SMALL_SIZE,
            context.getString(R.string.inpage_sizing_itemBrand_small_text)
        ) ?: ""

        val sizingMostBrandsLargeText = mostBrandsJSONObject?.optString(
            FIELD_LARGE_SIZE,
            context.getString(R.string.inpage_sizing_mostBrands_large_text)
        ) ?: ""
        val sizingMostBrandsTrueText = mostBrandsJSONObject?.optString(
            FIELD_TRUE_SIZE,
            context.getString(R.string.inpage_sizing_mostBrands_true_text)
        ) ?: ""
        val sizingMostBrandsSmallText = mostBrandsJSONObject?.optString(
            FIELD_SMALL_SIZE,
            context.getString(R.string.inpage_sizing_mostBrands_small_text)
        ) ?: ""

        val fitLooseText = fitJSONObject?.optString(
            FIELD_LOOSE_FIT,
            context.getString(R.string.inpage_fit_loose_text)
        ) ?: ""
        val fitRegularText = fitJSONObject?.optString(
            FIELD_REGULAR_FIT,
            context.getString(R.string.inpage_fit_regular_text)
        ) ?: ""
        val fitTightText = fitJSONObject?.optString(
            FIELD_TIGHT_FIT,
            context.getString(R.string.inpage_fit_tight_text)
        ) ?: ""

        return I18nLocalization(
            defaultText,
            defaultAccessoryText,
            sizingItemBrandLargeText,
            sizingItemBrandTrueText,
            sizingItemBrandSmallText,
            sizingMostBrandsLargeText,
            sizingMostBrandsTrueText,
            sizingMostBrandsSmallText,
            fitLooseText,
            fitRegularText,
            fitTightText
        )
    }

    companion object {
        private const val FIELD_KEYS = "keys"
        private const val FIELD_APPS = "apps"
        private const val FIELD_AOYAMA = "aoyama"
        private const val FIELD_INPAGE = "inpage"
        private const val FIELD_DETAILS_SCREEN = "detailsScreen"
        private const val FIELD_SIZING = "sizing"
        private const val FIELD_ITEM_BRAND = I18nConstants.BRAND_ITEM_BRAND_KEY
        private const val FIELD_MOST_BRANDS = I18nConstants.BRAND_MOST_BRANDS_KEY
        private const val FIELD_FIT = "fit"
        private const val FIELD_DEFAULT_TEXT = "defaultText"
        private const val FIELD_DEFAULT_ACCESSORY_TEXT = "defaultAccessoryText"
        private const val FIELD_LARGE_SIZE = I18nConstants.SIZING_LARGE_KEY
        private const val FIELD_TRUE_SIZE = I18nConstants.SIZING_TRUE_KEY
        private const val FIELD_SMALL_SIZE = I18nConstants.SIZING_SMALL_KEY
        private const val FIELD_LOOSE_FIT = I18nConstants.GENERAL_FIT_LOOSE_KEY
        private const val FIELD_REGULAR_FIT = I18nConstants.GENERAL_FIT_REGULAR_KEY
        private const val FIELD_TIGHT_FIT = I18nConstants.GENERAL_FIT_TIGHT_KEY
    }
}