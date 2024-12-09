@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-jackson', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-jacksonxml', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-xml-jaxb', version='4.8.1'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder

CamelContext camelContext = new DefaultCamelContext()

camelContext.addRoutes(new RouteBuilder() {
  @Override
  void configure() {
    from("file://input?noop=true")
      .log('Archivo recibido: ${header.CamelFileName}')
      .unmarshal().json()
      .log("Contenido JSON deserializado: ${body()}")
      .marshal().jacksonXml()
      .log("Contenido XML serializado: ${body()}")
      .enrich("direct:priceService", new PriceEnrichmentStrategy())
      .to('file://output?fileName=${header.CamelFileName}.xml')
      .log('Archivo enriquecido y creado: ${header.CamelFileName}')

    from("direct:priceService")
      .process { exchange ->
        exchange.in.setBody("""
          <priceInfo>
            <price>100.00</price>
          </priceInfo>
          """)
      }
      .log("Información de precios generada: ${body()}")
  }
})

class PriceEnrichmentStrategy implements org.apache.camel.AggregationStrategy {

  @Override
  Exchange aggregate(Exchange original, Exchange resource) {
    def originalBody = original.in.getBody(String)
    def priceInfo = resource.in?.getBody(String)

    if(!priceInfo) {
      // original.in.setBody(originalBody)
      return original
    }

    def enrichBody = originalBody.replace("</LinkedHashMap>", "${priceInfo}</LinkedHashMap>")
    original.in.setBody(enrichBody)

    original
  }
}

camelContext.start()

addShutdownHook {
  camelContext.stop()
}
synchronized(this) { this.wait() }
