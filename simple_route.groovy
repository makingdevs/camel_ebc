
@Grapes(
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1')
)

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder

CamelContext context = new DefaultCamelContext()

context.addRoutes()

println "Fin de rutas"
context.start()
Thread.sleep(5000)
context.stop()

println "Fin de rutas"
