package main

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * We start here to make request, not in a silly class with a static main, but just MAIN! :D
 */
fun main(args: Array<String>) {
    val options = Options()
    val parser = DefaultParser()

    options.addOption("u", "url", true, "the requested url")
    options.addOption("H", false, "prints response headers")
    options.addOption("h", "help", false, "prints commands")
    options.addOption("X", "method", true, "the http method: supported methods are GET POST")
    val cmd = parser.parse(options, args)

    val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER)
        .connectTimeout(Duration.ofMillis(1500))
        .build()
    val requestBuilder = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_1_1)
    requestBuilder.header("User-Agent", "kUrl")

    if (cmd.hasOption("u")) {
        val url = cmd.getOptionValue("u")
        requestBuilder.uri(URI.create(url))
    }

    if (cmd.argList.isEmpty() || cmd.hasOption("h")) {
        val helpFormatter = HelpFormatter()
        helpFormatter.printHelp("kUrl", options)
    }
    if (
        cmd.hasOption("X")) {
        when{
            cmd.getOptionValue("X").equals("GET", true) -> {
                requestBuilder.GET()
            }
            cmd.getOptionValue("X").equals("POST", true) -> {
                requestBuilder.POST(HttpRequest.BodyPublishers.noBody())
            }
        }

    }

    val request = requestBuilder.build()

    println("sending request:\n${request.method()} ${request.uri()} ${request.version().get()}")

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    println("server responded: ${response.version()} ${response.statusCode()}")

    if (cmd.hasOption("H")) {
        response.headers().map().forEach { (t, u) ->
            print("$t: ")
            u.forEach { print(it) }
            println()
        }
    }

    println(response.body())
}
