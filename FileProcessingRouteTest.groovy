@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-test-junit5', version='4.8.1'),
  @Grab(group='org.junit.jupiter', module='junit-jupiter-api', version='5.10.0'),
  @Grab(group='org.junit.jupiter', module='junit-jupiter-engine', version='5.10.0'),
  @Grab(group='org.apache.camel', module='camel-file', version='4.8.1'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.test.junit5.CamelTestSupport
import org.junit.jupiter.api.Test
import org.apache.camel.component.mock.MockEndpoint

class FileProcessingRouteTest extends CamelTestSupport {

  @Override
  protected RouteBuilder createRouteBuilder() {
    return new RouteBuilder() {
      @Override
      void configure() throws Exception {
        // Ruta para procesar archivos
        from("file://input?noop=true") // Leer archivos del directorio 'input'
        .transform().simple("\${bodyAs(String).toUpperCase()}") // Transformar el contenido a mayúsculas
        .to("mock:output") // Enviar al endpoint simulado
      }
    }
  }

  @Test
  void testFileProcessingRoute() throws Exception {
    // Configurar el MockEndpoint
    MockEndpoint mockEndpoint = getMockEndpoint("mock:output")
    mockEndpoint.expectedBodiesReceived("HELLO CAMEL") // Mensaje esperado

    // Enviar archivo simulado
    template.sendBodyAndHeader("file://input", "hello camel", "CamelFileName", "test.txt")

    // Validar el procesamiento
    MockEndpoint.assertIsSatisfied(context)
  }
}
