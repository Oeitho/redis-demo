package no.bring.redisdemo.services

import org.springframework.stereotype.Service
import no.bring.redisdemo.services.ReportCreationService.Type.NUMERIC
import no.bring.redisdemo.services.ReportCreationService.Type.ALPHANUMERIC
import kotlin.random.Random

@Service
class ReportCreationService {

    enum class Type {
        ALPHANUMERIC,
        NUMERIC
    }

    fun createReport(
        columns: Int,
        rows: Int
    ): List<Map<String, String>> {
        require(columns > 0) {
            "Number of columns needs to be larger than 0!"
        }
        require(columns <= POSSIBLE_FIELDS.size) {
            "Number of columns may not be larger than ${POSSIBLE_FIELDS.size}!"
        }
        require(rows > 0) {
            "Number of rows needs to be 1 or larger!"
        }

        val fieldsUsed = randomFields(columns)

        val report: MutableList<Map<String, String>> = mutableListOf()

        for (i in (0 until rows)) {
            val row = fieldsUsed.associate {
                if (it.second == ALPHANUMERIC) {
                    it.first to randomString()
                } else {
                    it.first to randomNumber()
                }
            }
            report.add(row)
        }

        return report
    }

    private fun randomFields(numberOfFields: Int): List<Pair<String, Type>> {
        val fieldsLeft = POSSIBLE_FIELDS.toMutableList()
        val usedFields = ArrayList<Pair<String, Type>>(numberOfFields)
        usedFields.add(Pair(MANDATORY_FIELD, NUMERIC))
        for (i in (1..numberOfFields)) {
            val fieldIndex = Random.nextInt(0, fieldsLeft.size)
            val field = fieldsLeft[fieldIndex]
            fieldsLeft.removeAt(fieldIndex)
            usedFields.add(field)
        }
        return usedFields
    }

    private fun randomNumber(): String {
        return Random.nextInt().toString()
    }

    private fun randomString(): String {
        val stringBuilder = StringBuilder()
        for (i in 1..Random.nextInt(10, 26)) {
            val charIndex = Random.nextInt(0, charPool.size - 1)
            stringBuilder.append(charPool[charIndex])
        }
        return stringBuilder.toString()
    }

    companion object {
        const val MANDATORY_FIELD = "recipient-reference"
        val POSSIBLE_FIELDS: List<Pair<String, Type>> = listOf(
            Pair("senders-customer-number", NUMERIC),
            Pair("senders-name", ALPHANUMERIC),
            Pair("recipient-name", ALPHANUMERIC),
            Pair("recipient-address", ALPHANUMERIC),
            Pair("delivery-address", ALPHANUMERIC),
            Pair("recipient-postal-code", NUMERIC),
            Pair("delivery-address-postal-town", ALPHANUMERIC),
            Pair("delivery-address-country", ALPHANUMERIC),
            Pair("recipient-phone", NUMERIC),
            Pair("recipient-mobile-number", NUMERIC),
            Pair("recipient-email", ALPHANUMERIC),
            Pair("shipment-number", NUMERIC),
            Pair("package-number", NUMERIC),
            Pair("product", ALPHANUMERIC),
            Pair("weight_kg", NUMERIC),
            Pair("length_cm", NUMERIC),
            Pair("width_cm", NUMERIC),
            Pair("height_cm", NUMERIC),
            Pair("pre-notification-received-date-and-time", NUMERIC),
            Pair("received-by-bring-date-and-time", NUMERIC),
            Pair("delivered-date-and-time", NUMERIC),
            Pair("delivery-note-on-sms_email", ALPHANUMERIC),
            Pair("arrived-at-pickup-point", NUMERIC),
            Pair("Coordinates-delivered", NUMERIC)
        )

        private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    }

}