@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-jms', version='4.8.1'),
  @Grab(group='org.apache.activemq', module='activemq-all', version='5.16.5'),
  @Grab(group='org.glassfish.jaxb', module='jaxb-runtime', version='4.0.5'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12'),
  @Grab(group='org.glassfish.jaxb', module='jaxb-runtime', version='4.0.5'),
  @Grab(group='javax.xml.bind', module='jaxb-api', version='2.3.1')
])

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy
import org.apache.camel.component.jms.JmsComponent
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.*

// Configuración de ActiveMQ
String brokerUrl = "tcp://localhost:61616"
// Crear una conexión a ActiveMQ
ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl)

CamelContext camelContext = new DefaultCamelContext()
camelContext.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory))

camelContext.addRoutes(new RouteBuilder() {
  @Override
  void configure() {
    // from("jms:queue:orders")
    //   .log("Mensaje recibido: ${body()}")
    //   .split(body())
    //   .log("Mensaje partido: ${body()}")
    //   .aggregate(header("group"), new GroupedExchangeAggregationStrategy())
    //   .completionSize(3)
    //   .log("Mensaje agrupado: ${body()}")
    //   .setBody(simple("Agrupado: ${body()}"))
    //   .to("jms:queue:aggregateOrders")

    from("jms:queue:orders")
      .choice()
        .when(simple("\${header.type} == 'electronics'"))
          .to("jms:queue:electronics")
          .log("Mensaje de electronica: ${body()}")
        .when(simple("\${header.type} == 'clothing'"))
          .to("jms:queue:clothing")
          .log("Mensaje de ropa: ${body()}")
        .otherwise()
          .to("jms:queue:other")
          .log("Mensaje de otros: ${body()}")
  }
})

camelContext.start()

// def producerTemplate = camelContext.createProducerTemplate()
// producerTemplate.sendBodyAndHeader("direct:orders", orders, "group", "A")

addShutdownHook {
  camelContext.stop()
}
synchronized(this) { this.wait() }
