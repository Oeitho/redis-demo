package no.bring.redisdemo.resources

import no.bring.redisdemo.services.RedisService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import no.bring.redisdemo.services.ReportCreationService
import kotlin.random.Random

@RestController
@RequestMapping("/api")
class RedisResource(
    private val reportCreationService: ReportCreationService,
    private val redisService: RedisService,
) {

    @GetMapping("/addReport/{reportName}")
    fun addReport(
        @PathVariable reportName: String,
        @RequestParam(required = false) columns: Int?,
        @RequestParam(required = false) rows: Int?
    ): List<Map<String, String>> {
        val report = reportCreationService.createReport(
            columns = Random.nextInt(0, ReportCreationService.POSSIBLE_FIELDS.size + 1),
            rows = rows ?: Random.nextInt(10, 100)
        )
        redisService.addReport(reportName, report)
        return report
    }

    @GetMapping("/addReport/{reportName}/allColumns")
    fun addReport(
        @PathVariable reportName: String,
        @RequestParam(required = false) rows: Int?
    ): List<Map<String, String>> {
        val report = reportCreationService.createReport(
            columns = ReportCreationService.POSSIBLE_FIELDS.size,
            rows = rows ?: Random.nextInt(10, 100)
        )
        redisService.addReport(reportName, report)
        return report
    }

    @GetMapping("/getReport/{reportName}")
    fun getReport(
        @PathVariable reportName: String,
        @RequestParam(required = false) sortField: String?,
        @RequestParam(required = false, defaultValue = "true") ascending: Boolean,
        @RequestParam(required = false, defaultValue = "1") page: Long,
        @RequestParam(required = false, defaultValue = "50") pageSize: Long,
    ): List<Map<String, String>> {
        return if (sortField == null) {
            redisService.fetchReportsWithoutSort(reportName, ascending, page, pageSize)
        } else {
            redisService.fetchReportsWithSort(reportName, sortField, ascending, page, pageSize)
        }
    }

}