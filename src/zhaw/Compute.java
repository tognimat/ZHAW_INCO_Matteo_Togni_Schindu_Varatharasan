package zhaw;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import zhaw.HoffmanTree.ArcType;
import zhaw.HoffmanTree.Node;

public class Compute {
	public static final boolean LDEBUG = false;

	private HashMap<Integer /* character */, CharProp> chars = null;
	private double fileCharactersCount = 0;

	// log2: Logarithm base 2
	public static double log2(double d) {
		return Math.log(d) / Math.log(2.0);
	}

	// log2 mit BigDecimal
	public static BigDecimal log2(BigDecimal bigDecimalZahl) {
		double zahl = bigDecimalZahl.doubleValue();
		double logarithmusZwei = log2(zahl);
		return new BigDecimal(logarithmusZwei);
	}

	// character exists
	public boolean characterExists(int c) {
		for (Entry<Integer, CharProp> row : chars.entrySet()) {
			if (row.getKey() == c) {
				return true;
			}
		}
		return false;
	}

	public void ReadInputTextFileCharacters(String relativeFilePath)
			throws UserErrorException {
		if (LDEBUG) {
			System.out.println("Current directory is: "
					+ System.getProperty("user.dir"));
		}

		chars = new HashMap<>();
		fileCharactersCount = 0;
		try (BufferedReader in = new BufferedReader(new FileReader(
				relativeFilePath))) {
			System.out.println("Reading the input text file "
					+ relativeFilePath + " ...");
			int c;
			while ((c = in.read()) != -1) {
				if (characterExists(c)) {
					CharProp charPropObject = chars.get(c);
					charPropObject.occurence = charPropObject.occurence + 1;
				} else {
					chars.put(c, new CharProp());
				}

				fileCharactersCount++;
			}
			// add EOF too
			chars.put(HoffmanTree.hoffmanContentEOFchar, new CharProp());
			++(chars.get(HoffmanTree.hoffmanContentEOFchar).occurence);
			++fileCharactersCount;

		} catch (FileNotFoundException ex) {
			throw new UserErrorException("input file " + relativeFilePath
					+ " does not exists.");
		} catch (IOException e) {
			throw new UserErrorException("input file " + relativeFilePath
					+ " reading failed.");
		}

		if (fileCharactersCount <= 0)
			throw new UserErrorException("input file " + relativeFilePath
					+ " has nothing inside.");
	}

	public void ComputeProbabilities(String relativeFilePath)
			throws UserErrorException {
		// you have to read the file before computing the probabilities
		if (chars == null)
			ReadInputTextFileCharacters(relativeFilePath);
		System.out.println("Computing probabilities...");
		/*
		 * ToDo: [2] implement computing of the probabilities of the existing
		 * characters. Use the precision 10 after the comma and the constant
		 * RoundingMode.HALF_UP
		 */

		for (Entry<Integer, CharProp> reihe : chars.entrySet()) {

			BigDecimal wahrscheinlichkeit = new BigDecimal(
					reihe.getValue().occurence / fileCharactersCount);
			reihe.getValue().probability = wahrscheinlichkeit;
		}
	}

	public void ComputeInformation(String relativeFilePath)
			throws UserErrorException {
		// you have to read the file before computing the information
		if (chars == null)
			ComputeProbabilities(relativeFilePath);
		System.out.println("Computing information...");
		/*
		 * ToDo: [3] implement computing of the information of the existing
		 * characters. Use the precision 10 after the comma and the constant
		 * RoundingMode.HALF_UP
		 */
		for (Entry<Integer, CharProp> reihe : chars.entrySet()) {
			BigDecimal probability = reihe.getValue().probability;
			BigDecimal einBD = new BigDecimal(1.0);
			BigDecimal divisionResultat = einBD.divide(probability,
					RoundingMode.HALF_UP);
			BigDecimal informationsGehalt = log2(divisionResultat);
			reihe.getValue().information = informationsGehalt;
		}
	}

	public BigDecimal ComputeEntropy(String relativeFilePath)
			throws UserErrorException {
		// you have to read the file before computing the entropy
		if (chars == null)
			ComputeInformation(relativeFilePath);
		System.out.println("Computing entropy...");
		/*
		 * ToDo: [5] implement computing of the entropy of the existing
		 * characters. Send the entropy value back as a result.
		 */

		BigDecimal sum = new BigDecimal(0.0);
		for (Entry<Integer, CharProp> reihe : chars.entrySet()) {
			BigDecimal probability = reihe.getValue().probability;
			BigDecimal informationsGehalt = reihe.getValue().information;
			sum = sum.add(probability.multiply(informationsGehalt));
		}

		return sum;
	}

	public void PrintOutCharProps() {
		System.out.println("Character types in file: " + chars.size());
		System.out.println("Number of character in file: "
				+ fileCharactersCount);
		for (int c : chars.keySet()) {
			String chr = "" + (char) c;
			if (Character.isWhitespace(c))
				chr = "(" + c + ")";
			try (Formatter ft = new Formatter()) {
				System.out.println(ft.format("%1$5s : %2$s", chr, chars.get(c))
						.toString());
			}
		}
	}

	public HoffmanTree CreateHoffmanTree() throws UserErrorException {
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("Creating HoffmanTree...");
		if (chars == null)
			throw new UserErrorException(
					"You have to request computation of probabilities before you request creating of Hffman Tree.");
		HoffmanTree res = new HoffmanTree();
		// create the ordered by probabilities hash map for temporary container
		// in order to build the tree,
		// e.g. it always keeps the node probability sorted in ascendent order.
		// the first in the sorted map "sm" is with the lowest probability
		CacheTreeMap wertListe = new CacheTreeMap();
		for (int currChr : chars.keySet()) {
			CharProp cp = chars.get(currChr);
			try (Formatter ft = new Formatter()) {
				wertListe.put(cp.probability,
						res.new Node("" + (char) currChr /*
														 * ft.format("%s",
														 * currChr).toString()
														 */));

				// sm.put( cp.probability, res.new Node(
				// Character.toString((char)currChr) /* ft.format("%s",
				// currChr).toString()*/));
			}
		}
//		Entry<BigDecimal, List<Node>> firstEntry = wertListe.firstEntry();
//		wertListe.remove(firstEntry.getKey());
		// do build the HoffmanTree till there is at least two elements in the
		// sorted cache map
		Node root = null;
		Entry<BigDecimal, List<Node>> aEntry = null;
		Entry<BigDecimal, List<Node>> bEntry = null;
		Node a = null;
		Node b = null;
		while (wertListe.elements() > 1) {

			/*
			 * ToDo: [6] Having the sorted cache map "sm" so that: Create a
			 * parent node which has the the lowest value element as a right
			 * child node and the second lowest as a left child node. Also do
			 * not forget to add back into the sorted map the parent node.
			 */
			a = wertListe.popNodeWithLowesProbability(ArcType.RIGHT);
//			if(wertListe.elements() > 1){
				b = wertListe.popNodeWithLowesProbability(ArcType.RIGHT);
//			}
//			else {
//				break;
//			}

			/*
			 * Wenn compareResult == -1, dann ist a kleiner als b Wenn
			 * compareResult == 0, dann ist a gleich b Wenn compareResult == 1,
			 * dann ist a groesser als b
			 */
			int compareResult = a.probability.compareTo(b.probability);
			if (compareResult == -1 || compareResult == 0) {
				// a kleiner als b oder a gleich b
				a.code = ArcType.RIGHT;
				b.code = ArcType.LEFT;
			} else {
				// a groesser als b
				a.code = ArcType.LEFT;
				b.code = ArcType.RIGHT;
			}
			
			root = res.CreateParentForNodes(a, b);
			wertListe.put(root.probability, root);
		}
		
		System.out.println("Anzahl wahrscheinlichkeitelementen: " + wertListe.elements());
		
		/*
		 * ToDo: [7] Add the last element as the root of the tree.
		 */
		root.code = ArcType.NONE;
		res.root = root;
		System.out.println("res.root.value/probability: " + res.root.value + " " + res.root.probability);
		return res;
	}

}
