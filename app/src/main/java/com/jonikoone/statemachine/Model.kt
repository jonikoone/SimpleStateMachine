package com.jonikoone.statemachine

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class EducationPeriod(
    @SerializedName("start")
    val start: Date,
    @SerializedName("end")
    val end: Date
) : Serializable

enum class Temperament : Serializable {
    melancholic,
    sanguine,
    choleric,
    phlegmatic;
}

data class Contact(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("height")
    val height: Float,
    @SerializedName("biography")
    val biography: String,
    @SerializedName("temperament")
    @Expose
    val temperament: Temperament,
    @SerializedName("educationPeriod")
    @Expose
    val educationPeriod: EducationPeriod
) : Serializable