package dev.dominiqn.githubsearcher

import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author Dongmin Nam
 */
@Configuration
class GithubConfig {
    @Bean
    fun github(): GitHub = GitHubBuilder
        .fromEnvironment()
        .build()
}
