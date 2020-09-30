package fault_localization.reports

interface FLReport {
    fun mostLikelyFaulty(upTo: Int): Sequence<Sequence<FLComponent>>
}
