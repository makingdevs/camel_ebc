@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-micrometer', version='4.8.1'),
  @Grab(group='io.micrometer', module='micrometer-registry-prometheus', version='1.11.3'),
  @Grab(group='io.prometheus', module='simpleclient_httpserver', version='0.16.0'),
  @Grab(group='io.micrometer', module='micrometer-registry-jmx', version='1.11.3'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12'),
  @Grab(group='org.apache.camel', module='camel-timer', version='4.8.1')
])

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder
import io.prometheus.client.exporter.HTTPServer
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import org.apache.camel.component.micrometer.MicrometerComponent
import org.apache.camel.support.processor.idempotent.FileIdempotentRepository

CamelContext camelContext = new DefaultCamelContext()

PrometheusMeterRegistry registry =  new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
new HTTPServer(new InetSocketAddress(8080), registry.getPrometheusRegistry(), true)

camelContext.registry.bind("prometheusRegistry", registry)

def micrometerComponent = new MicrometerComponent()
micrometerComponent.setMetricsRegistry(registry)
camelContext.addComponent("micrometer", micrometerComponent)


camelContext.addRoutes(new RouteBuilder() {
  @Override
  void configure() {

    from("timer://generateEvents?period=2000")
      .log('HEADERS: ${headers}')
      .setHeader("Message-ID", simple('${date:now:dd-MM-yyyyHH:mm:ss}'))
      .idempotentConsumer(header("Message-ID"), FileIdempotentRepository.fileIdempotentRepository(new File("idempotentrepository.txt")))
      .routeId("MetricsPrometheusRoute")
      .log("Procesando evento...")
      .to("micrometer:counter:processedEvents?increment=1")
      .to("micrometer:timer:processingTime?action=start")
      .process { exchange ->
        Thread.sleep((Math.random() * 1000).toLong())
      }
      .to("micrometer:timer:processingTime?action=stop")
    .log("Evento procesado: ${body()}")

  }
})

camelContext.start()

addShutdownHook {
  camelContext.stop()
}
synchronized(this) { this.wait() }
