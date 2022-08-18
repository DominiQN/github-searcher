package dev.dominiqn.githubsearcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GithubSearcherApplication

fun main(args: Array<String>) {
    runApplication<GithubSearcherApplication>(*args)
}
