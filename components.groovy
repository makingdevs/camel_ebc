@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-jms', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-http', version='4.8.1'),
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

    from("timer:fileGenerator?period=5000")
      .setBody(simple('Archivo generado a las: ${date:now:dd-MM-yyyy HH:mm:ss}'))
      .to('file://output?fileName=data-${date:now:dd-MM-yyyyHH:mm:ss}.txt')
      .log('Archivo creado: ${body}')
      .log('Archivo creado: ${headers}')
  }
})

camelContext.start()

addShutdownHook {
  camelContext.stop()
}
synchronized(this) { this.wait() }
