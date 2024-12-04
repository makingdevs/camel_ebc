@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy

CamelContext camelContext = new DefaultCamelContext()

camelContext.addRoutes(new RouteBuilder() {
  @Override
  void configure() {
    from("direct:orders")
      .log("Mensaje recibido: ${body()}")
      .split(body())
      .log("Mensaje partido: ${body()}")
      .aggregate(header("group"), new GroupedExchangeAggregationStrategy())
      .completionSize(3)
      .log("Mensaje agrupado: ${body()}")
      .to("mock:result")
  }
})

camelContext.start()

def orders = [
  [id: 1, type: 'electronic', group: 'A'],
  [id: 2, type: 'clothes', group: 'B'],
  [id: 3, type: 'electronic', group: 'A'],
  [id: 4, type: 'clothes', group: 'B'],
  [id: 5, type: 'electronic', group: 'A'],
  [id: 6, type: 'clothes', group: 'B']
]

def producerTemplate = camelContext.createProducerTemplate()
producerTemplate.sendBodyAndHeader("direct:orders", orders, "group", "A")

addShutdownHook {
  camelContext.stop()
}
synchronized(this) { this.wait() }
