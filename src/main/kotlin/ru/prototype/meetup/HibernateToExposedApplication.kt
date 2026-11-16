package ru.prototype.meetup

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HibernateToExposedApplication

fun main(args: Array<String>) {
	runApplication<HibernateToExposedApplication>(*args)
}
