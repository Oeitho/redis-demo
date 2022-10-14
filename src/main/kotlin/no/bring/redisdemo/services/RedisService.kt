package no.bring.redisdemo.services

import no.bring.redisdemo.services.ReportCreationService.Companion.POSSIBLE_FIELDS
import no.bring.redisdemo.services.ReportCreationService.Type.ALPHANUMERIC
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.query.SortQueryBuilder
import org.springframework.stereotype.Service
import java.util.stream.IntStream
import kotlin.math.min

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, String>,
) {


    fun addReport(id: String, rows: List<Map<String, String>>) {
        val listOfIndices = (rows.indices)
            .map { it.toString() }

        redisTemplate.keys("$REPORTS_STRING_PREFIX:$id*")
            .forEach {
                redisTemplate.delete(it)
            }

        redisTemplate.opsForList().rightPushAll("$REPORTS_STRING_PREFIX:$id", listOfIndices)

        IntStream
            .range(0, rows.size)
            .parallel()
            .forEach {
                redisTemplate.opsForHash<String, String>().putAll("$REPORTS_STRING_PREFIX:$id:$it", rows[it])
            }

    }

    fun fetchReportsWithoutSort(id: String, ascending: Boolean, page: Long, pageSize: Long): List<Map<String, String>> {
        val numberOfRows = redisTemplate.opsForList().size("$REPORTS_STRING_PREFIX:$id") ?: 0

        val start = min((page - 1) * pageSize, numberOfRows)
        val end = min((page) * pageSize - 1, numberOfRows)

        val rows = redisTemplate.opsForList().range("$REPORTS_STRING_PREFIX:$id", start, end) ?: emptyList()

        return IntStream
            .range(0, rows.size)
            .parallel()
            .map {
                if (ascending) {
                    it
                } else {
                    it - rows.size - 1
                }
            }
            .mapToObj {
                redisTemplate.opsForHash<String, String>().entries("$REPORTS_STRING_PREFIX:$id:$it")
            }
            .toList()
    }

    fun fetchReportsWithSort(id: String, field: String, ascending: Boolean = true, page: Long, pageSize: Long): List<Map<String, String>> {
        val rows = fetchSortedRows(id, field, page, pageSize)

        return IntStream
            .range(0, rows.size)
            .parallel()
            .map {
                if (ascending) {
                    it
                } else {
                    rows.size - it - 1
                }
            }
            .mapToObj {
                redisTemplate.opsForHash<String, String>().entries("$REPORTS_STRING_PREFIX:$id:${rows[it]}")
            }
            .toList()
    }

    fun fetchSortedRows(id: String, field: String, page: Long, pageSize: Long): List<String> {
        return if (redisTemplate.hasKey("$REPORTS_STRING_PREFIX:$id:$REPORTS_SORTED_FIELD_PREFIX:$field")) {
            fetchRowsFromSortedOrder(id, field, page, pageSize)
        } else {
            createAndReturnSortOrder(id, field, page, pageSize)
        }
    }

    private fun fetchRowsFromSortedOrder(id: String, field: String, page: Long, pageSize: Long): List<String> {
        val numberOfRows = redisTemplate.opsForList().size("$REPORTS_STRING_PREFIX:$id") ?: 0

        val start = min((page - 1) * pageSize, numberOfRows)
        val end = min((page) * pageSize - 1, numberOfRows)

        return redisTemplate.opsForList().range("$REPORTS_STRING_PREFIX:$id:$REPORTS_SORTED_FIELD_PREFIX:$field", start, end)
            ?: emptyList()
    }

    fun createAndReturnSortOrder(id: String, field: String, page: Long, pageSize: Long): List<String> {
        val query = SortQueryBuilder
            .sort("$REPORTS_STRING_PREFIX:$id")
            .by("$REPORTS_STRING_PREFIX:$id:*->$field")
            .alphabetical(field in STRING_FIELDS)
            .build()
        val rows = redisTemplate.sort(query)

        redisTemplate.delete("$REPORTS_STRING_PREFIX:$id:$REPORTS_SORTED_FIELD_PREFIX:$field:filter:length_cm<5>5:width_cm<10>5")

        redisTemplate
            .opsForList()
            .rightPushAll("$REPORTS_STRING_PREFIX:$id:$REPORTS_SORTED_FIELD_PREFIX:$field", rows)

        val start = min(((page - 1) * pageSize).toInt(), rows.size)
        val end = min(((page) * pageSize - 1).toInt(), rows.size)

        return rows.subList(start, end)
    }

    companion object {
        private const val REPORTS_STRING_PREFIX = "reports"
        private const val REPORTS_SORTED_FIELD_PREFIX = "sort"
        private val STRING_FIELDS = POSSIBLE_FIELDS
            .filter { it.second == ALPHANUMERIC }
            .map { it.first }
    }

}