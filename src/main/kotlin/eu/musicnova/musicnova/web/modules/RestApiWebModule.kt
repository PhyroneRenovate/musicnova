package eu.musicnova.musicnova.web.modules

import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import eu.musicnova.musicnova.module.WebModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import org.springframework.stereotype.Component

@Component
class RestApiWebModule : WebModule {


    override fun Application.invoke() {
        val swaggerUIPath = "/api/swaggerUI"
        val openAPIJsonPath = "/api/openapi.json"
        install(OpenAPIGen) {
            serveSwaggerUi = true
            swaggerUiPath = swaggerUIPath
        }
        routing {
            get(swaggerUIPath) {
                call.respondRedirect("$swaggerUIPath/index.html?url=$openAPIJsonPath", true)
            }
            get(openAPIJsonPath) {
                call.respond(openAPIGen.api.serialize())
            }
        }
        apiRouting {
            route("v1") {
                route("test") {
                    get<EmptyRequest, TestResponse> {
                        respond(TestResponse("Not Implemented YET!"))
                    }
                }
                get<TestRequest, TestResponse> { request ->
                    respond(TestResponse("Value is: \"${request.value}\""))
                }
            }
        }
    }

    class EmptyRequest

    @Path("test/{value}")
    class TestRequest(@PathParam("") val value: String)
    class TestResponse(val value: String)
}