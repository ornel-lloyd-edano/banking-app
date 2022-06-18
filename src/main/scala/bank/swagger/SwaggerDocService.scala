package bank.swagger

import bank.Controller
import com.github.swagger.akka.model.Info
import com.typesafe.config.ConfigFactory
import io.swagger.v3.oas.models.ExternalDocumentation

/**
 * Sample SwaggerDocService, replace values with those applicable your application.
 * By default, a swagger UI is made available too on the default routes. If you don't need the UI, or want
 * to load the UI in another way, replace [[SwaggerHttpWithUiService]] with [[com.github.swagger.akka.SwaggerHttpService]]
 */
object SwaggerDocService extends SwaggerHttpWithUiService {
  override val apiClasses = Set(classOf[Controller])
  val conf = ConfigFactory.load()
  val appHost = conf.getString("banking-app.host")
  val appPort = conf.getInt("banking-app.port")
  override val host = s"${appHost}:${appPort}"
  override val info: Info = Info(version = "1.0")
  override val externalDocs: Option[ExternalDocumentation] = Some(new ExternalDocumentation().description("Core Docs").url("http://acme.com/docs"))
  //use io.swagger.v3.oas.models.security.SecurityScheme to document authn requirements for API
  //override val securitySchemeDefinitions = Map("basicAuth" -> new SecurityScheme())
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}

