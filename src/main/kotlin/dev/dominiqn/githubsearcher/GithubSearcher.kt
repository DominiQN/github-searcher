package dev.dominiqn.githubsearcher

import java.lang.Thread
import java.time.Duration
import org.kohsuke.github.GHContent
import org.kohsuke.github.GitHub
import org.kohsuke.github.PagedIterator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * @author Dongmin Nam
 */
@Component
class GithubSearcher(
    private val github: GitHub,
    private val printer: FilePrinter,
    @Value("\${search.result-file-path}")
    private val filePath: String,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val repoNames: MutableList<String> = mutableListOf()

    override fun run(args: ApplicationArguments?) {
        requireNotNull(args)

        val query = parseQuery(args)
        val organization = parseOrganization(args)
        keepAllRepoNames(organization = organization)
        search(query = query)
    }

    private fun parseQuery(args: ApplicationArguments): String {
        val query = requireNotNull(args.nonOptionArgs.firstOrNull()) { "Query must exists!!" }
        logger.info("Query='$query'")
        return query
    }

    private fun parseOrganization(args: ApplicationArguments): String {
        val organization = requireNotNull(args.getOptionValues("org").firstOrNull()) { "Organization must exists!!" }
        logger.info("Organization='$organization'")
        return organization
    }

    private fun keepAllRepoNames(organization: String) {
        repoNames += queryReposInOrg(organization)
            .map { it.fullName }
    }

    private fun search(query: String) {
        print("################### START ####################")
        logger.info(
            repoNames.joinToString(
                separator = "\n",
                prefix = "============== Repo list ==============",
                postfix = "=======================================",
            )
        )

        repoNames.forEach { repoName ->
            tryPrintCodePathInRepo(repoName, query = query, secondsBetweenRequests = 1)
        }
        print("#################### END #####################")
    }

    private fun tryPrintCodePathInRepo(
        repoName: String,
        query: String,
        secondsBetweenRequests: Int,
    ) {
        try {
            waitFor(secondsBetweenRequests)
            val codePathsInRepo = buildString {
                appendLine("----------------- $repoName")
                logger.info("Searching in '$repoName'...")

                val contents = github.searchContent()
                    .repo(repoName)
                    .q(query)
                    .list()
                    .withPageSize(100)
                    .iterator()

                while (contents.hasNext()) {
                    getCodeFiles(contents, repoName)
                        .forEach { codeFile -> appendLine(codeFile.path) }
                }
            }
            print(codePathsInRepo)
        } catch (e: Throwable) {
            logger.error("Error occurred!", e)
            tryPrintCodePathInRepo(repoName, query, secondsBetweenRequests = secondsBetweenRequests * 2)
        }
    }

    private fun getCodeFiles(
        contents: PagedIterator<GHContent>,
        repoName: String
    ): List<GithubCodePath> {
        val content = contents.nextPage()
        return content.map { GithubCodePath(repoName, it.path) }
    }

    private fun waitFor(seconds: Int) {
        logger.info("Wait for $seconds seconds...")
        Thread.sleep(Duration.ofSeconds(seconds.toLong()).toMillis())
    }

    private fun queryReposInOrg(organization: String) = github.getOrganization(organization)
        .listRepositories()

    private fun print(string: String) {
        logger.debug(string)
        printer.print(string, filePath = filePath)
    }
}
