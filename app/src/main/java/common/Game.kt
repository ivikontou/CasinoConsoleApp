package common

import java.io.Serializable

data class Game(
    val gameName: String,
    val providerName: String,
    val stars: Int,
    val noOfVotes: Int,
    val gameLogo: String,
    val minBet: Double,
    val maxBet: Double,
    val riskLevel: String,
    val hashKey: String,
    val betCategory: String,
    val jackpot: Int
) : Serializable