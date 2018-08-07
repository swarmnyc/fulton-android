package com.swarmnyc.fulton.android.promise

enum class PromiseState {
    Pending,
    Fulfilled,
    Rejected,
    RejectedOnCancel,
    Canceled;

    companion object {
        private val map = PromiseState.values().associateBy(PromiseState::ordinal)

        fun valueOf(value: Int): PromiseState? = map[value]
    }
}