@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-test-junit5', version='4.8.1'),
  @Grab(group='org.spockframework', module='spock-core', version='2.3-groovy-4.0'),
  @Grab(group='org.spockframework', module='spock-junit4', version='2.3-groovy-4.0'),
  @Grab(group='org.apache.camel', module='camel-file', version='4.8.1'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.test.junit5.CamelTestSupport
import org.apache.camel.component.mock.MockEndpoint
import spock.lang.Specification

class FileProcessingRouteSpec extends Specification {

  def "Validar procesamiento de archivo a mayúsculas"() {
    setup: "Configurar la ruta Camel"
    def context = new DefaultCamelContext()
    context.addRoutes(new RouteBuilder() {
      @Override
      void configure() throws Exception {
        from("file://input?noop=true")
        .transform().simple("\${bodyAs(String).toUpperCase()}") // Transformar el contenido a mayúsculas
        .to("mock:output")
      }
    })
    context.start()

    and: "Configurar MockEndpoint"
    MockEndpoint mockEndpoint = context.getEndpoint("mock:output", MockEndpoint)
    mockEndpoint.expectedMessageCount(1)
    mockEndpoint.expectedBodiesReceived("HOLA CAMEL")

    when: "Se envía un archivo al directorio de entrada"
    context.createProducerTemplate().sendBodyAndHeader("file://input", "hola camel", "CamelFileName", "test.txt")

    then: "El mensaje procesado debe ser el esperado"
    MockEndpoint.assertIsSatisfied(context)

    cleanup: "Detener CamelContext"
    context.stop()
  }
}
