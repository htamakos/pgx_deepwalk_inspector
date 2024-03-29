/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.github.htamakos;

import java.io.File;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.plot.BarnesHutTsne;

public class App {
    public static void main(String[] args) {
        Word2Vec w2v = WordVectorSerializer.readWord2VecModel("viz_data/word2vec.model", true);

        System.out.println(w2v.vocab().tokens());
        BarnesHutTsne tsne =
                new BarnesHutTsne.Builder()
                        .setMaxIter(1000)
                        .stopLyingIteration(250)
                        .learningRate(500)
                        .useAdaGrad(false)
                        .theta(0.5)
                        .setMomentum(0.5)
                        .normalize(true)
                        .build();

        w2v.lookupTable().plotVocab(tsne, 78, new File("viz_data/tnse_result.txt"));
    }
}
