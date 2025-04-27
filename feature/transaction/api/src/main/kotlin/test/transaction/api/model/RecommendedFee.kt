package test.transaction.api.model

data class RecommendedFee(
    val fastestFeeInSat : Long,
    val halfHourFeeInSat : Long,
    val hourFeeInSat : Long,
    val economyFeeInSat : Long,
    val minimumFeeInSat : Long,
)
