@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder

CamelContext context = new DefaultCamelContext()

context.addRoutes(new RouteBuilder() {
  @Override
  void configure() {
    from("file://input")
      .setBody(body().append("${new Date()}"))
      .to("file://output")
  }
})

println "Comienzo Camel Context"

context.start()
Thread.sleep(5000)
context.stop()

println "Fin de rutas"
