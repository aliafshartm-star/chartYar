package com.chartyar.app.analysis

enum class StrategyType {
    SCALPING,
    SWING,
    POSITION,
    MULTI_AI,
    PRO,
    PUMP
}

data class StrategyInfo(
    val type: StrategyType,
    val displayName: String,
    val emoji: String,
    val description: String,
    val shortDescription: String,
    val timeframes: List<String>,
    val iconColor: Long,
    val indicatorCount: Int = 15
)

object StrategyEngine {

    val allStrategies: List<StrategyInfo> = listOf(
        StrategyInfo(
            type = StrategyType.SCALPING,
            displayName = "اسکالپ",
            emoji = "🏃",
            description = "معاملات سریع در چند دقیقه تا چند ساعت. سیگنال‌های زیاد با ریسک بالا.",
            shortDescription = "سریع، پرسیگنال",
            timeframes = listOf("1m", "5m", "15m", "30m"),
            iconColor = 0xFFFF4D4D,
            indicatorCount = 15
        ),
        StrategyInfo(
            type = StrategyType.SWING,
            displayName = "سوئینگ",
            emoji = "🌊",
            description = "معاملات چند روزه تا چند هفته. تعادل بین دقت و تعداد سیگنال.",
            shortDescription = "متعادل، برای اکثر کاربرا",
            timeframes = listOf("1H", "4H"),
            iconColor = 0xFF2962FF,
            indicatorCount = 15
        ),
        StrategyInfo(
            type = StrategyType.POSITION,
            displayName = "پوزیشن",
            emoji = "🎯",
            description = "معاملات بلندمدت چند هفته تا چند ماه. سیگنال‌های کم ولی بسیار دقیق.",
            shortDescription = "بلندمدت، کم‌ریسک",
            timeframes = listOf("1D", "1W"),
            iconColor = 0xFF00C896,
            indicatorCount = 15
        ),
        StrategyInfo(
            type = StrategyType.MULTI_AI,
            displayName = "Multi-AI",
            emoji = "🤖",
            description = "ترکیب ۵ موتور هوش مصنوعی مستقل با رأی‌گیری. نیاز به توافق اکثریت.",
            shortDescription = "هوش مصنوعی، ۵ موتور رأی‌گیری",
            timeframes = listOf("15m", "1H", "4H", "1D"),
            iconColor = 0xFF8B5CF6,
            indicatorCount = 15
        ),
        StrategyInfo(
            type = StrategyType.PRO,
            displayName = "PRO",
            emoji = "💎",
            description = "دقیق‌ترین استراتژی با ۲۵ اندیکاتور پیشرفته. تحلیل چندلایه با Ichimoku، Supertrend، Aroon و ... . مناسب تریدرهای حرفه‌ای.",
            shortDescription = "۲۵ اندیکاتور، حرفه‌ای",
            timeframes = listOf("15m", "1H", "4H", "1D"),
            iconColor = 0xFFFFB800,
            indicatorCount = 25
        )
    )

    fun getStrategy(type: StrategyType): StrategyInfo {
        return allStrategies.first { it.type == type }
    }

    fun getDefaultTimeframe(type: StrategyType): String {
        return when (type) {
            StrategyType.SCALPING -> "15m"
            StrategyType.SWING -> "4H"
            StrategyType.POSITION -> "1D"
            StrategyType.MULTI_AI -> "4H"
            StrategyType.PRO -> "4H"
            StrategyType.PUMP -> "4H"
        }
    }

    fun getWeights(strategy: StrategyType, timeframe: String): Map<String, Double> {
        return when (strategy) {
            StrategyType.SCALPING -> scalpingWeights(timeframe)
            StrategyType.SWING -> swingWeights(timeframe)
            StrategyType.POSITION -> positionWeights(timeframe)
            StrategyType.MULTI_AI -> multiAiWeights(timeframe)
            StrategyType.PRO -> proWeights(timeframe)
            StrategyType.PUMP -> pumpWeights(timeframe)
        }
    }

    // ============ اسکالپ ============
    private fun scalpingWeights(timeframe: String): Map<String, Double> {
        return mapOf(
            "Market Structure" to 0.7,
            "RSI" to 1.0,
            "Divergence" to 0.8,
            "MACD" to 1.3,
            "EMA" to 0.9,
            "ADX" to 0.7,
            "Stochastic" to 1.6,
            "Bollinger" to 1.2,
            "Price Action" to 1.6,
            "Patterns" to 1.0,
            "Vision" to 0.6,
            "Volume" to 1.5,
            "VWAP" to 1.3,
            "CCI" to 1.2,
            "WilliamsR" to 1.3
        )
    }

    // ============ سوئینگ ============
    private fun swingWeights(timeframe: String): Map<String, Double> {
        return mapOf(
            "Market Structure" to 1.5,
            "RSI" to 1.2,
            "Divergence" to 1.4,
            "MACD" to 1.0,
            "EMA" to 1.3,
            "ADX" to 1.1,
            "Stochastic" to 0.8,
            "Bollinger" to 0.9,
            "Price Action" to 1.0,
            "Patterns" to 1.5,
            "Vision" to 1.3,
            "Volume" to 1.0,
            "VWAP" to 0.9,
            "CCI" to 0.7,
            "WilliamsR" to 0.6
        )
    }

    // ============ پوزیشن ============
    private fun positionWeights(timeframe: String): Map<String, Double> {
        return mapOf(
            "Market Structure" to 2.0,
            "RSI" to 1.0,
            "Divergence" to 1.6,
            "MACD" to 1.1,
            "EMA" to 1.6,
            "ADX" to 1.5,
            "Stochastic" to 0.5,
            "Bollinger" to 0.6,
            "Price Action" to 0.8,
            "Patterns" to 1.7,
            "Vision" to 1.5,
            "Volume" to 0.7,
            "VWAP" to 0.6,
            "CCI" to 0.5,
            "WilliamsR" to 0.4
        )
    }

    // ============ Multi-AI ============
    private fun multiAiWeights(timeframe: String): Map<String, Double> {
        return mapOf(
            "Market Structure" to 1.5,
            "RSI" to 1.2,
            "Divergence" to 1.3,
            "MACD" to 1.2,
            "EMA" to 1.3,
            "ADX" to 1.2,
            "Stochastic" to 1.0,
            "Bollinger" to 1.0,
            "Price Action" to 1.3,
            "Patterns" to 1.5,
            "Vision" to 1.3,
            "Volume" to 1.2,
            "VWAP" to 1.1,
            "CCI" to 0.9,
            "WilliamsR" to 0.8
        )
    }

    // ============ PRO — ۲۵ اندیکاتور ============
    private fun proWeights(timeframe: String): Map<String, Double> {
        return mapOf(
            // ۱۵ اندیکاتور پایه
            "Market Structure" to 1.6,
            "RSI" to 1.1,
            "Divergence" to 1.5,
            "MACD" to 1.2,
            "EMA" to 1.4,
            "ADX" to 1.3,
            "Stochastic" to 0.8,
            "Bollinger" to 0.9,
            "Price Action" to 1.2,
            "Patterns" to 1.5,
            "Vision" to 1.4,
            "Volume" to 1.1,
            "VWAP" to 1.0,
            "CCI" to 0.7,
            "WilliamsR" to 0.7,
            // ۱۰ اندیکاتور پیشرفته
            "Ichimoku" to 1.7,
            "OBV" to 1.2,
            "MFI" to 1.1,
            "PSAR" to 1.4,
            "Aroon" to 1.0,
            "Supertrend" to 1.6,
            "Keltner" to 0.9,
            "Donchian" to 1.1,
            "PivotPoints" to 0.8,
            "ElderRay" to 1.0
        )
    }

    // ============ PUMP — ۵ نیرو ============
    private fun pumpWeights(timeframe: String): Map<String, Double> {
        return mapOf(
            "Volume" to 1.5,
            "Trend" to 1.4,
            "News" to 1.0,
            "Momentum" to 1.2,
            "Regime" to 1.1
        )
    }

    // ============================================================
    // رأی‌گیری Multi-AI — ۵ موتور
    // ============================================================
    data class EngineVote(
        val engineName: String,
        val score: Int,
        val vote: Int,
        val description: String
    )

    data class MultiAiResult(
        val votes: List<EngineVote>,
        val bullishCount: Int,
        val bearishCount: Int,
        val neutralCount: Int,
        val consensus: Int,
        val finalSignal: SignalType
    )

    fun evaluateMultiAi(
        structureScore: Int,
        momentumScore: Int,
        reversalScore: Int,
        volumeScore: Int,
        volatilityScore: Int
    ): MultiAiResult {
        val votes = mutableListOf<EngineVote>()

        val trendVote = when {
            structureScore >= 30 -> 1
            structureScore <= -30 -> -1
            else -> 0
        }
        votes.add(EngineVote(
            engineName = "Trend AI",
            score = structureScore,
            vote = trendVote,
            description = when (trendVote) {
                1 -> "روند صعودی تأیید شده"
                -1 -> "روند نزولی تأیید شده"
                else -> "بدون روند مشخص"
            }
        ))

        val momentumVote = when {
            momentumScore >= 30 -> 1
            momentumScore <= -30 -> -1
            else -> 0
        }
        votes.add(EngineVote(
            engineName = "Momentum AI",
            score = momentumScore,
            vote = momentumVote,
            description = when (momentumVote) {
                1 -> "مومنتوم صعودی"
                -1 -> "مومنتوم نزولی"
                else -> "مومنتوم خنثی"
            }
        ))

        val reversalVote = when {
            reversalScore >= 30 -> 1
            reversalScore <= -30 -> -1
            else -> 0
        }
        votes.add(EngineVote(
            engineName = "Reversal AI",
            score = reversalScore,
            vote = reversalVote,
            description = when (reversalVote) {
                1 -> "احتمال برگشت صعودی"
                -1 -> "احتمال برگشت نزولی"
                else -> "بدون سیگنال برگشتی"
            }
        ))

        val volumeVote = when {
            volumeScore >= 30 -> 1
            volumeScore <= -30 -> -1
            else -> 0
        }
        votes.add(EngineVote(
            engineName = "Volume AI",
            score = volumeScore,
            vote = volumeVote,
            description = when (volumeVote) {
                1 -> "حجم تأییدکننده صعود"
                -1 -> "حجم تأییدکننده نزول"
                else -> "حجم خنثی"
            }
        ))

        val volatilityVote = when {
            volatilityScore >= 30 -> 1
            volatilityScore <= -30 -> -1
            else -> 0
        }
        votes.add(EngineVote(
            engineName = "Volatility AI",
            score = volatilityScore,
            vote = volatilityVote,
            description = when (volatilityVote) {
                1 -> "نوسان در جهت صعود"
                -1 -> "نوسان در جهت نزول"
                else -> "نوسان خنثی"
            }
        ))

        val bullish = votes.count { it.vote == 1 }
        val bearish = votes.count { it.vote == -1 }
        val neutral = votes.count { it.vote == 0 }

        val consensus = maxOf(bullish, bearish)

        val finalSignal = when {
            consensus < 4 -> SignalType.NEUTRAL
            bullish >= 4 && bullish - bearish >= 2 -> SignalType.STRONG_BUY
            bearish >= 4 && bearish - bullish >= 2 -> SignalType.STRONG_SELL
            bullish >= 4 -> SignalType.BUY
            bearish >= 4 -> SignalType.SELL
            else -> SignalType.NEUTRAL
        }

        return MultiAiResult(
            votes = votes,
            bullishCount = bullish,
            bearishCount = bearish,
            neutralCount = neutral,
            consensus = consensus,
            finalSignal = finalSignal
        )
    }
}