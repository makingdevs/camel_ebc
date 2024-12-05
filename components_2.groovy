@GrabConfig(systemClassLoader=true)
@Grapes([
  @Grab(group='org.apache.camel', module='camel-core', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-csv', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-jdbc', version='4.8.1'),
  @Grab(group='org.apache.camel', module='camel-jetty', version='4.8.1'),
  @Grab(group='org.xerial', module='sqlite-jdbc', version='3.47.1.0'),
  @Grab(group='org.apache.commons', module='commons-dbcp2', version='2.13.0'),
  @Grab(group='ch.qos.logback', module='logback-classic', version='1.5.12')
])

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.commons.dbcp2.BasicDataSource

def dbUrl = "jdbc:sqlite:orders.db"
def dbDriver = "org.sqlite.JDBC"
def dataSource = new BasicDataSource(driverClassName: dbDriver, url: dbUrl)

CamelContext camelContext = new DefaultCamelContext()

camelContext.registry.bind("orders_db", dataSource)

camelContext.addRoutes(new RouteBuilder() {
  @Override
  void configure() {
    from("file://input?noop=true")
      .log('Archivo leído: ${body}')
      .unmarshal().csv()
      .split(body())
      .log('Registro: ${body}')
      .process { exchange ->
        def record = exchange.in.getBody(List)
        def sql = "INSERT INTO orders(id, product_name, quantity, price) values(${record[0]}, '${record[1]}', ${record[2]}, ${record[3]})"
        exchange.in.setBody(sql)
      }
    .to("jdbc:orders_db")
    .log('Registro insertado: ${body}')

    from("jetty:http://0.0.0.0:8081/service/?sessionSupport=true")
      .log('Request leído ${body}')
  }
})

camelContext.start()

addShutdownHook {
  camelContext.stop()
}
synchronized(this) { this.wait() }
