import static oracle.pgx.api.beta.frames.functions.ColumnRenaming.renaming
import static oracle.pgx.api.beta.frames.schema.ColumnDescriptor.columnDescriptor
import oracle.pgx.api.beta.frames.schema.*
import oracle.pgx.api.beta.frames.schema.datatypes.*
import groovy.json.*

g = session.readGraphWithProperties("graphs/connections.edge_list.json")

fs = [
  columnDescriptor("x", DataTypes.DOUBLE_TYPE),
  columnDescriptor("y", DataTypes.DOUBLE_TYPE),
  columnDescriptor("id", DataTypes.STRING_TYPE)
].toArray(new ColumnDescriptor[0])

tsneFrame = session.readFrame().\
    name("tsneFrame").\
    columns(fs).\
    csv().\
    separator(',' as char).\
    load("viz_data/tnse_result.txt")

results = tsneFrame.toPgqlResultSet().results
x = results.collect { it.x }
y = results.collect { it.y }
id = results.collect { Integer.parseInt(it.id.trim()) }
name = results.collect { g.getVertex(Integer.parseInt(it.id.trim())).getProperty("name") }
type = results.collect { g.getVertex(Integer.parseInt(it.id.trim())).getProperty("type") }

json = new JsonBuilder()
json {
  x x
  y y
  id id
  name name
  ntype type
}
jsonString = JsonOutput.prettyPrint(json.toPrettyString())
file = new File("viz_data/tsne_result.js")
file.write("var tsne_result = \n" + jsonString)

