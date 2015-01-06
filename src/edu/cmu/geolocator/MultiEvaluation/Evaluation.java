package edu.cmu.geolocator.MultiEvaluation;

import java.util.ArrayList;

import edu.cmu.geolocator.model.Document;
import edu.cmu.geolocator.model.Paragraph;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;

public class Evaluation {

	private static int totalToponym = 0;
	private static int totalPredction = 0;
	private static int totalCorrectPredction = 0;
	private static int totalGPE = 0;
	private static int totalLOC = 0;

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	private double recall;
	private double precision;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Evaluation eval = new Evaluation();
		eval.setPrecision(totalCorrectPredction / totalPredction);
		eval.setRecall(totalCorrectPredction /totalToponym );
		System.out.print(eval.getPrecision());
		System.out.print(eval.getRecall());

	}

	public void evaluationTest(ArrayList<Document> doc) {

		for (Document document : doc) {
			ArrayList<Paragraph> para = document.getP();
			for (Paragraph paragraph : para) {
				ArrayList<Sentence> sents = paragraph.getSentences();
				for (Sentence sentence : sents) {
					Token[] tokens = sentence.getTokens();
					for (Token token : tokens) {
						if (token.getNE().equals("GPE")
								|| token.getNE().equals("LOC"))
							totalToponym++;
						if (token.getNE().equals(token.getNEprediction())) {
							totalPredction++;
							if (token.getNE().equals("GPE"))
								totalCorrectPredction++;
							if (token.getNE().equals("LOC"))
								totalCorrectPredction++;

						}

					}

				}
			}

		}

	}

	public String findTypes(String subtype) {
		String type = null;
		if (subtype.equals("Continent") || subtype.equals("County-or-District")
				|| subtype.equals("County-or-District")
				|| subtype.equals("County-or-District")
				|| subtype.equals("GPE-Cluster") || subtype.equals("Nation")
				|| subtype.equals("Population-Center")
				|| subtype.equals("Special")
				|| subtype.equals("State-or-Province"))
			type = "GPE";
		if (subtype.equals("Address") || subtype.equals("Boundary")
				|| subtype.equals("Celestial")
				|| subtype.equals("Land-Region-Natural")
				|| subtype.equals("Region-General") || subtype.equals("Region-International")
				|| subtype.equals("Water-Body"))
			type = "LOC";
		return type;

	}
}
