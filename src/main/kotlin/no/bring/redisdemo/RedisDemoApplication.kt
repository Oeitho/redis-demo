package no.bring.redisdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedisDemoApplication

fun main(args: Array<String>) {
	runApplication<RedisDemoApplication>(*args)
}
