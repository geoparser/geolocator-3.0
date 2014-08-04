package edu.cmu.geolocator.io;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.ParagraphAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Dependency;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class PipeLineAnnotate {

	static Properties props;
	static StanfordCoreNLP pipeline;
	static {
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma" 
//		+		", ner, parse" 
//				+", dcoref" +
				);
		pipeline = new StanfordCoreNLP(props);
	}

	Annotation document;
	List<CoreMap> sentences;

	public PipeLineAnnotate(String text) {
		this.document = new Annotation(text);
		pipeline.annotate(document);
		this.sentences = document.get(SentencesAnnotation.class);
	}

	public List<CoreMap> getSentences() {
		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		return this.sentences;
	}

	public Tree getTree(CoreMap sentence) {
		return sentence.get(TreeAnnotation.class);
	}

	public Set<SemanticGraphEdge> getDependencies(CoreMap sentence) {
		SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		Set<SemanticGraphEdge> eset = dependencies.getEdgeSet();
		return eset;
	}

	public void prettyPrint() {

		for (CoreMap sentence : this.sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);
				System.out.println(token+" "+token.beginPosition()+"-"+token.endPosition()	);
			}

			// this is the parse tree of the current sentence
			Tree tree = sentence.get(TreeAnnotation.class);
			// this is the Stanford dependency graph of the current sentence
			SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
//			Set<SemanticGraphEdge> eset = dependencies.getEdgeSet();
//			for (SemanticGraphEdge e : eset){
//			System.out.println(e.getSource()+"_"+e.getTarget()+"_"+e.getRelation());
//			}
				
		}

		// This is the coreference link graph
		// Each chain stores a set of mentions that link to each other,
		// along with a method for getting the most representative mention
		// Both sentence and token offsets start at 1!
		Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);

	}

	public static void main(String argv[]) {
		PipeLineAnnotate pla = new PipeLineAnnotate(
				"We trashed on up the spine of Norway through spectacular mountain scenery to Trondheim."
						+ " The influx of cheap arab labor from the occupied territories allowed "
						+ "israeli economy to expand rapidly.");
		pla.prettyPrint();
	}
}
