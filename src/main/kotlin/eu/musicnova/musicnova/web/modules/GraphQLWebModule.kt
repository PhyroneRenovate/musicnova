package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.module.WebModule
import graphql.schema.GraphQLSchema
import io.ktor.application.Application
import io.ktor.routing.route
import io.ktor.routing.routing
import ktor.graphql.Config
import ktor.graphql.graphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GraphQLWebModule : WebModule {

    @Autowired
    lateinit var graphQLSchema: GraphQLSchema

    override fun Application.invoke() {
        routing {
            route("api") {
                graphQL("graphQL", graphQLSchema) {
                    return@graphQL Config(showExplorer = false)
                }
            }
        }
    }

}