package com.swarmnyc.fulton.android.model

data class TopDogPost(
        val hotdogId: String,
        val name: String,
        val location: List<Double>,
        val address: String,
        val review: String,
        val picture: String,
        val author: TopDogAuthor,
        val tags: List<TopDogTag>
)

