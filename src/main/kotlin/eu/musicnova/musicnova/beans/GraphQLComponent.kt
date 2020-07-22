package eu.musicnova.musicnova.beans

import com.tgirard12.graphqlkotlindsl.SchemaDsl
import com.tgirard12.graphqlkotlindsl.graphqljava.*
import com.tgirard12.graphqlkotlindsl.schemaDsl
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.*

@Component
class GraphQLComponent {

    @Bean
    fun graphQLSchemaDSL() = schemaDsl {
        scalar<Double> { GqlJavaScalars.double }
        scalar<UUID> { GqlJavaScalars.uuid }
        query<String> {
            name = "CommingSoon"
        }
    }

    @Bean
    fun graphQLSchema(dsl: SchemaDsl, runtimeWiring: RuntimeWiring): GraphQLSchema = dsl.graphQLSchema(runtimeWiring)

    @Bean
    fun graphQLWiring(): RuntimeWiring = RuntimeWiring.newRuntimeWiring().apply { buildRuntimeWiring() }.build()


    private fun RuntimeWiring.Builder.buildRuntimeWiring() {
        scalarUUID()
        scalarDouble()
        queryType {
            dataFetcher("CommingSoon") { environment ->
                return@dataFetcher "Not Implemented YET"
            }
        }
    }
}