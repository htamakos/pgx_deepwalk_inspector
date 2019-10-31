import oracle.pgx.engine.mllib.MlLibUtils
import oracle.pgx.engine.CoreMlLibImpl
import oracle.pgx.engine.Server
import oracle.pgx.engine.exec.FunctionRequest
import oracle.pgx.engine.exec.TaskType
import oracle.pgx.api.internal.BuiltinAlgorithms
import oracle.pgx.common.VersionInfo
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import groovy.json.*

// PGX Server の種別とバージョンの Check
if(!session.serverInstance.embeddedInstance){
  println("This script must be executed on Embedded PGX Server")
  System.exit(1)
}
if(session.serverInstance.getVersion().releaseVersion != "19.3.1"){
  println("This script must be executed on PGX Server 19.3.1")
  System.exit(1)
}

// Graph の読み込み
g = session.readGraphWithProperties("../graphs/connections.edge_list.json")

// connection Graph を JSON 化(d3.js に食べさせるため)
json = new JsonBuilder()
nodeArray = g.getVertices().collect { it ->
  [ "id": it.getId(), "label": it.getProperty("name") ]
}

edgeArray = g.getEdges().collect { it ->
  [ "from": it.getSource().id, "to": it.getDestination().id]
}

json {
  nodes nodeArray
  edges edgeArray
}
jsonString = JsonOutput.prettyPrint(json.toPrettyString())
file = new File("../viz_data/connections.json")
file.write(jsonString)

// DeepWalk モデルを仮で作成する
model = analyst.deepWalkModelBuilder().setNumEpochs(5).build()

// 非公開API を実行するために必要な各種オブジェクトを取得
eSession = Server.enqueue(
             new FunctionRequest(session.id, TaskType.GET_ML_MODEL,
                                 { session, req -> session })
           ).get()

core = CoreMlLibImpl.getCore()
algo = new BuiltinAlgorithms(core)

// DeepWalk 内部で実行される Random Walk 結果を格納するプロパティ名を設定
deepwalkPropName = "dwprop"
deepwalkProp = g.getOrCreateVertexProperty(PropertyType.STRING, deepwalkPropName)

// DeepWalk 内部で実行される Random Walk を手動実行
walkLength = 5
walksPerVertex = 6
algo.pgxBuiltinM2Randomwalk(session.id, g.name, walkLength, walksPerVertex, deepwalkProp.name)


// DeepWalk 内部で実行された Random Walk 結果を確認するために必要なオブジェクトを取得
gmp = MlLibUtils.extractGraphWithProperties(core.instanceManager, eSession, g.name)

dlServerModel = Server.enqueue(new FunctionRequest(session.id,
                                                   TaskType.GET_ML_MODEL,
                                                   { session, req -> session.getCache().getMlModels() })
                              ).get().values()[0]
dl4jModel = dlServerModel.model
dl4jModelConfig = dl4jModel.config

// Random Walk 結果を格納
walkResult = dl4jModel.fetchWalks(gmp, deepwalkProp.name)

json = new JsonBuilder()
json {
  results walkResult.collect { it.split("\r").collect { c -> Integer.parseInt(c)} }
}
jsonString = JsonOutput.prettyPrint(json.toPrettyString())
file = new File("../viz_data/walk_result.js")
file.write("var walk_result = \n" + jsonString)

// word2vec オブジェクトの保存
model.fit(g)
WordVectorSerializer.writeWord2VecModel(dl4jModel.cachedVertexVectors, "../viz_data/word2vec.model")

