package com.storm.tornadoai.logic

import com.storm.tornadoai.model.BiasFilter
import com.storm.tornadoai.model.PropagandaReport
import com.storm.tornadoai.model.TechniqueHit

/**
 * Storm Protocol: fast heuristics, zero dependencies.
 * Classifies bias directionality + propaganda/opinion features.
 */
object BiasClassifier {

    private val LEFT_MARKERS = listOf(
        "equity", "systemic", "climate crisis", "gun control", "universal healthcare",
        "reproductive rights", "living wage", "white supremacy", "patriarchy"
    )
    private val RIGHT_MARKERS = listOf(
        "patriot", "globalist", "woke", "2a", "election integrity", "border invasion",
        "deep state", "socialist", "marxist", "traditional values"
    )
    private val ESTABLISHMENT_MARKERS = listOf(
        "bipartisan consensus", "experts say", "officials said", "intelligence officials",
        "according to officials", "authorities stated", "u.s. officials"
    )
    private val ANTIEST_MARKERS = listOf(
        "regime", "cartel", "uniparty", "capture", "weaponized", "corporate media",
        "mockingbird", "alphabet agencies", "shadow government"
    )

    private val OPINION_PHRASES = listOf(
        "i think", "i believe", "in my view", "imo", "imho", "personally",
        "it seems to me", "i feel", "to me", "my take"
    )

    private val WEASEL_WORDS = listOf(
        "some say", "critics argue", "many believe", "it is said", "reportedly", "allegedly"
    )

    private val LOADED_LANGUAGE = listOf(
        "disgrace", "traitor", "corrupt", "fraud", "sham", "evil", "tyranny",
        "authoritarian", "fascist", "communist", "insurrection", "coup"
    )

    private val FEAR_APPEAL = listOf(
        "apocalypse", "collapse", "doomsday", "catastrophic", "existential threat",
        "total control", "enslaved", "annihilation"
    )

    private val BANDWAGON = listOf("everyone knows", "the people want", "the majority agrees")

    private val WHATABOUTISM = listOf("what about", "but they didn’t", "and yet no one")

    private val AD_HOMINEM = listOf("idiot", "moron", "clown", "shill", "stooge", "puppet")

    private val STRAWMAN = listOf("so you’re saying", "so they claim that")

    private val APPEAL_TO_AUTH = listOf("experts say", "scientists agree", "officials confirm")

    private val URL_REGEX = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
    private val CITE_REGEX = Regex("""\[\d+\]|\([^)]+,\s*\d{4}\)""") // [1], (Smith, 2020)

    fun classifyDirection(text: String): BiasFilter {
        val t = text.lowercase()

        val left = scoreContains(t, LEFT_MARKERS)
        val right = scoreContains(t, RIGHT_MARKERS)
        val est = scoreContains(t, ESTABLISHMENT_MARKERS)
        val anti = scoreContains(t, ANTIEST_MARKERS)

        // pick the strongest non-tie; break ties with None
        val pairs = listOf(
            BiasFilter.Left to left,
            BiasFilter.Right to right,
            BiasFilter.Establishment to est,
            BiasFilter.AntiEstablishment to anti
        ).sortedByDescending { it.second }

        return if (pairs[0].second == 0) BiasFilter.None
        else if (pairs.size > 1 && pairs[0].second == pairs[1].second) BiasFilter.Unknown
        else pairs[0].first
    }

    fun analyzePropaganda(text: String): PropagandaReport {
        val t = text.lowercase()

        val techniques = mutableListOf<TechniqueHit>()

        fun hit(name: String, dict: List<String>): TechniqueHit? {
            val found = dict.filter { t.contains(it) }
            if (found.isEmpty()) return null
            return TechniqueHit(name, found.size, examples = found.take(5))
        }

        listOfNotNull(
            hit("Loaded Language", LOADED_LANGUAGE),
            hit("Fear Appeal", FEAR_APPEAL),
            hit("Bandwagon", BANDWAGON),
            hit("Whataboutism", WHATABOUTISM),
            hit("Ad Hominem", AD_HOMINEM),
            hit("Strawman", STRAWMAN),
            hit("Appeal to Authority", APPEAL_TO_AUTH),
            hit("Weasel Words", WEASEL_WORDS)
        ).forEach { techniques.add(it) }

        val opinion = OPINION_PHRASES.any { t.contains(it) }

        val links = URL_REGEX.findAll(text).map { it.value }.toList()
        val hasCites = links.isNotEmpty() || CITE_REGEX.containsMatchIn(text)

        // crude score: weighted by technique density + loaded/fear boosts, capped 100
        val density = techniques.sumOf { it.count } * 10
        val boost = (if (techniques.any { it.name == "Loaded Language" }) 10 else 0) +
                (if (techniques.any { it.name == "Fear Appeal" }) 10 else 0)
        val opinionBoost = if (opinion) 5 else 0
        val score = (density + boost + opinionBoost).coerceIn(0, 100)

        val notes = buildList {
            if (opinion) add("Opinion phrasing detected.")
            if (!hasCites) add("No citations/links detected.")
            if (links.isNotEmpty()) add("Contains ${links.size} link(s).")
        }

        return PropagandaReport(
            score = score,
            techniques = techniques.sortedByDescending { it.count },
            isOpinion = opinion,
            evidenceLinks = links,
            hasCitations = hasCites,
            notes = notes
        )
    }

    private fun scoreContains(text: String, probes: List<String>): Int =
        probes.count { text.contains(it) }
}