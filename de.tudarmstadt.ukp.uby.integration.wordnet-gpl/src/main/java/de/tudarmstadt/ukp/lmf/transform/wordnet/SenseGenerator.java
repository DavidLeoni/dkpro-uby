/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.lmf.transform.wordnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.Word;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.core.Sense;
import de.tudarmstadt.ukp.lmf.model.core.TextRepresentation;
import de.tudarmstadt.ukp.lmf.model.enums.EExampleType;
import de.tudarmstadt.ukp.lmf.model.enums.ELabelTypeSemantics;
import de.tudarmstadt.ukp.lmf.model.enums.ELanguageIdentifier;
import de.tudarmstadt.ukp.lmf.model.meta.SemanticLabel;
import de.tudarmstadt.ukp.lmf.model.semantics.MonolingualExternalRef;
import de.tudarmstadt.ukp.lmf.model.semantics.SenseExample;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.transform.wordnet.util.IndexSenseReader;

/**
 * This class offers methods for generating instances of {@link Sense} class
 * from the Princeton WordNet.
 */
public class SenseGenerator {

	public final static String EXTERNAL_SYSTEM_SENSE_KEY = "senseKey";

	private final IndexSenseReader isr;

	/*
	 * Synset generator used for obtaining mappings between
	 * WordNet's synsets and synsets defined in Uby-LMF
	 */
	private final SynsetGenerator synsetGenerator;

	// used for creating IDs of SenseExamples
	private int senseExampleNumber=0;

	// Running number for creating Sense-IDs
	private int lmfSenseNumber;

	// Mappings between lexemes and associated Senses
	private final Map<Word, Sense> lexemeSenseMappings;

	private final String resourceVersion;

	private final Log logger = LogFactory.getLog(getClass());

	/**
	 * Constructs a {@link SenseGenerator} based on the consumed parameters
	 * @param synsetGenerator a SynsetGenerator used for obtaining Synsets
	 * @param isr reader used for parsing WordNet's index.sense file
	 * @param resourceVersion Verstion of the resource
	 * @see Sense
	 * @see Synset
	 * @see IndexSenseReader
	 */
	public SenseGenerator(SynsetGenerator synsetGenerator, IndexSenseReader isr, String resourceVersion){
		this.synsetGenerator = synsetGenerator;
		this.isr = isr;
		this.resourceVersion = resourceVersion;

		lexemeSenseMappings = new TreeMap<Word, Sense>(new Comparator<Word>() {
			@Override
			public int compare(Word o1, Word o2) {
				try {
                    return o1.getSenseKey().compareTo(o2.getSenseKey());
                }
                catch (JWNLException e) {
                    throw new IllegalArgumentException(e);
                }
			}
		});
	}

	/**
	 * This method consumes a {@link Set} of lexemes and generates a list of Senses. <br>
	 * Every {@link Sense} in the returned list is associated with one lexeme in the consumed Set.
	 *
	 * @param lexemeGroup a group of lexemes with equal lemma and part of speech
	 *
	 * @param lexicalEntry a {@link LexicalEntry} instance that contains generated Senses.
	 *
	 * @return list of Sense-instances, based on the consumed group of lexemes
	 *
	 * @since UBY 0.2.0
	 *
	 * @see Word
	 *
	 */
	public List<Sense> generateSenses(Set<Word> lexemeGroup, LexicalEntry lexicalEntry){
		List<Sense> result = new ArrayList<Sense>();

		// a list of Senses that need a dummy sense number
		List<Sense> needDummySenseNumber = new ArrayList<Sense>();
		int nextIndex = 1; // dummy index

		// every lexeme has a sense of it's own
		for(Word lexeme : lexemeGroup){
			Sense sense = new Sense();
			lexemeSenseMappings.put(lexeme, sense);
			//set ID
			sense.setId(getNewID());
			sense.setLexicalEntry(lexicalEntry);
			// setting index of the Sense (lexeme's Position in the WN-Synset)
			String senseNumber;
            try {
                senseNumber = isr.getSenseNumber(lexeme.getSenseKey());
            }
            catch (JWNLException e) {
                throw new IllegalArgumentException(e);
            }
			if(senseNumber != null){
				int index = Integer.parseInt(senseNumber);
				if(nextIndex <= index) {
                    nextIndex = index+1;
                }
				sense.setIndex(index);
			}
			else{
				// sense needs a dummy value for index
				needDummySenseNumber.add(sense);
				StringBuffer sb = new StringBuffer(128);
				sb.append("IndexSenseReader did not provide sense number for senseKey ");
				try {
                    sb.append(lexeme.getSenseKey()).append('\n');
                }
                catch (JWNLException e) {
                    throw new IllegalArgumentException(e);
                }
				sb.append("adding a dummy value of sense number");
				logger.warn(sb.toString());
			}

			net.sf.extjwnl.data.Synset lexemeSynset = lexeme.getSynset(); // lexemes Synset

			//set Synset
			Synset lmfSynset = synsetGenerator.getLMFSynset(lexemeSynset);
			if(lmfSynset == null){
				StringBuffer sb = new StringBuffer(512);
				sb.append("Synset generator did not provide Uby-LMF Synset for WordNet's Synset ");
				sb.append(lexemeSynset).append('\n');
				sb.append("Closing VM");
				logger.error(sb.toString());
				System.exit(1);
			}

			sense.setSynset(lmfSynset);
			// set semanticLabel
			List<SemanticLabel> semanticLabels = new LinkedList<SemanticLabel>();
			SemanticLabel semanticLabel = new SemanticLabel();
			semanticLabels.add(semanticLabel);
			semanticLabel.setLabel(lexemeSynset.getLexFileName());
			semanticLabel.setType(ELabelTypeSemantics.semanticField);

			sense.setSemanticLabels(semanticLabels);

			// Creating MonolingualExternalRef for a Sense
			MonolingualExternalRef monolingualExternalRef = new MonolingualExternalRef();
			/**/
			StringBuffer sb = new StringBuffer(32);
			sb.append(lexeme.getSynset().getPOS());
			sb.append(" ");
			try {
                sb.append(lexeme.getSenseKey());
            }
            catch (JWNLException e) {
                throw new IllegalArgumentException(e);
            }
			monolingualExternalRef.setExternalSystem("WordNet 3.0 part of speech and sense key");
			monolingualExternalRef.setExternalReference(sb.toString());
			/**/
			//TODO: Check implications!
//		monolingualExternalRef.setExternalSystem(resourceVersion + "_" + EXTERNAL_SYSTEM_SENSE_KEY);
//		monolingualExternalRef.setExternalReference(lexeme.getSenseKey());
			List<MonolingualExternalRef> monolingualExternalRefs = new LinkedList<MonolingualExternalRef>();
			monolingualExternalRefs.add(monolingualExternalRef);
			sense.setMonolingualExternalRefs(monolingualExternalRefs);

			//*** create sense examples of the sense *** //
			List<SenseExample> senseExamples = new ArrayList<SenseExample>();
			List<String> exampleStrings = synsetGenerator.getExamples(lexeme);
			if(exampleStrings != null) {
                for(String exampleSentence : exampleStrings){
					SenseExample senseExample = new SenseExample();

					// Create an id for the senseExample
					StringBuffer id = new StringBuffer(32);
					id.append("WN_SenseExample_").append(senseExampleNumber++);
					senseExample.setId(id.toString());
					senseExample.setExampleType(EExampleType.other);
					TextRepresentation textRepresentation = new TextRepresentation();
					textRepresentation.setLanguageIdentifier(ELanguageIdentifier.ENGLISH);
					textRepresentation.setWrittenText(exampleSentence);
					senseExample.setTextRepresentations(new ArrayList<TextRepresentation>(Arrays.asList(textRepresentation)));
					senseExamples.add(senseExample);
				}
            }
			// setting senseExamples
			sense.setSenseExamples(senseExamples);

			// Add the created Sense to the result
			result.add(sense);
			}

		/*
		 * Adding dummy indexes to senses if needed
		 */
		for(Sense sense : needDummySenseNumber) {
            sense.setIndex(nextIndex++);
        }

		return result;
	}

	/**
	 * This method generates a Sense-ID. <br>
	 * Every time the method is called, it increments the running number used for the creation of the ID.
	 * @return an ID of a Sense-instance
	 * @see Sense
	 */
	private String getNewID() {
		StringBuffer sb = new StringBuffer(64);
		sb.append("WN_Sense_").append(Integer.toString(lmfSenseNumber));
		lmfSenseNumber++;
		return sb.toString();
	}

	/**
	 * Returns the Sense-instance associated with the consumed lexeme
	 * @param lexeme a lexeme for which the generated Sense-intance should be returned
	 * @return Sense-instance associated with the consumed lexeme,<br>
	 * or null if this generator has not generated a Sense for the consumed lexeme
	 * @see Sense
	 * @see Word
	 */
	public Sense getSense(Word lexeme){
		return lexemeSenseMappings.get(lexeme);
	}

	/**
	 * Returns all lexemes processed by this {@link SenseGenerator}
	 * @return all lexemes processed by this SenseGenerator
	 * @see Word
	 */
	public Set<Word> getProcessedLexemes(){
		return lexemeSenseMappings.keySet();
	}
}
