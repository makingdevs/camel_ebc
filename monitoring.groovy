@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-timer', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-management', version='4.8.1'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder

CamelContext camelContext = new DefaultCamelContext()

camelContext.addRoutes(new RouteBuilder() {
  @Override
  void configure() {

    from("timer://generateEvents?period=2000")
      .routeId("MonitoringRoute")
      .process { exchange ->
        def currentTime = new Date().format("yyyy-MM-dd HH:mm:ss")
        exchange.in.setBody("Evento generado a las ${currentTime}")
      }
    .log("Evento procesado: ${body()}")
    .to("direct:monitor")

    from("direct:monitor")
      .log("Monitor: ${body()}")
      .process { exchange ->
        if(Math.random() > 0.7)
          throw new RuntimeException("ERROR Simulado en el procesamiento")
      }
      .log("Procesamiento EXITOSO: ${body()}")

  }
})

camelContext.start()

addShutdownHook {
  camelContext.stop()
}
synchronized(this) { this.wait() }
