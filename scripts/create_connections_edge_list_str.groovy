import groovy.json.JsonOutput
import groovy.json.JsonSlurper

g = session.readGraphWithProperties("graphs/connections.edge_list.json")

builder = session.createGraphBuilder(IdType.STRING)
g.getVertices().forEach { v ->
  name        = v.getProperty("name")
  type        = v.getProperty("type")
  company     = v.getProperty("company")
  music_genre = v.getProperty("music genre")
  show        = v.getProperty("show")
  country     = v.getProperty("country")
  team        = v.getProperty("team")
  genre       = v.getProperty("genre")
  occupation  = v.getProperty("occupation")
  role        = v.getProperty("role")

  id = v.getId() 
  sid = "v" + id
  builder.addVertex(sid).setProperty("name", name)
  builder.addVertex(sid).setProperty("type", type)
  builder.addVertex(sid).setProperty("company", company)
  builder.addVertex(sid).setProperty("music_genre", music_genre)
  builder.addVertex(sid).setProperty("show", show)
  builder.addVertex(sid).setProperty("country", country)
  builder.addVertex(sid).setProperty("team", team)
  builder.addVertex(sid).setProperty("genre", genre)
  builder.addVertex(sid).setProperty("occupation", occupation)
  builder.addVertex(sid).setProperty("role", role)
}

g.queryPgql("SELECT id(e) as eid, id(n1) as id1, id(n2) as id2 MATCH (n1)-[e]->(n2)").results.forEach {
  builder.addEdge("v" + it.get("id1"), "v" + it.get("id2"))
}

g = builder.build()
config = g.store(Format.EDGE_LIST, "graphs/connections_str.edge_list", true)
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

jsonSlurper = new JsonSlurper()
configJson = jsonSlurper.parseText(config.toString())
json_graph_config = JsonOutput.prettyPrint(JsonOutput.toJson(configJson))
gc_file_path = "graphs/connections_str.edge_list.json"
gc_file = new File(gc_file_path)
gc_file.write(json_graph_config)

