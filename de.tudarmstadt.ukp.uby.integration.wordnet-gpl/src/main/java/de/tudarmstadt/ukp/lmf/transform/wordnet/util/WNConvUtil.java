/**
 * Copyright 2015
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
package de.tudarmstadt.ukp.lmf.transform.wordnet.util;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.extjwnl.data.POS;
import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.core.Lexicon;
import de.tudarmstadt.ukp.lmf.model.core.Sense;
import de.tudarmstadt.ukp.lmf.model.core.TextRepresentation;
import de.tudarmstadt.ukp.lmf.model.enums.EPartOfSpeech;
import de.tudarmstadt.ukp.lmf.model.semantics.SemanticArgument;
import de.tudarmstadt.ukp.lmf.model.semantics.SenseExample;
import de.tudarmstadt.ukp.lmf.model.semantics.SynSemCorrespondence;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.model.syntax.SubcategorizationFrame;
import de.tudarmstadt.ukp.lmf.model.syntax.SyntacticArgument;
import de.tudarmstadt.ukp.lmf.model.syntax.SyntacticBehaviour;

/**
 * This class offers methods offers some helper methods used when converting
 * WordNet UBY-LMF.
 * @author Zijad Maksuti
 *
 */
public class WNConvUtil {

    private final static Log logger = LogFactory.getLog(WNConvUtil.class);
    
    /**
     * div new
     */
    public static final String DEFAULT_PREFIX = "WN";

    /**
     * div new
     */
    public static final Map<Class, String> NAMESPACES = new HashMap<>();
    
    static {
        NAMESPACES.put(Lexicon.class, "lex");
        NAMESPACES.put(Sense.class, "s");
        NAMESPACES.put(SenseExample.class, "se");
        NAMESPACES.put(Synset.class, "ss");        
        NAMESPACES.put(LexicalEntry.class, "le");
        NAMESPACES.put(SynSemCorrespondence.class, "ssc");
        NAMESPACES.put(SubcategorizationFrame.class, "scf");
        NAMESPACES.put(SyntacticArgument.class, "sya");
        NAMESPACES.put(SemanticArgument.class, "sea");
        NAMESPACES.put(SyntacticBehaviour.class, "sb");
    }
    
    
	/**
	 * This method consumes a {@link POS}
	 * and returns corresponding {@link EPartOfSpeech}
	 * @param pos part of speech encoded in extJWNL-API
	 * @return associated part of speech defined in UBY-LMF
	 * @since 0.2.0
	 */
	public static EPartOfSpeech getPOS(POS pos) {
		switch (pos) {
			case NOUN:
				return EPartOfSpeech.noun;
			case VERB:
				return EPartOfSpeech.verb;
			case ADJECTIVE:
				return EPartOfSpeech.adjective;
			case ADVERB:
				return EPartOfSpeech.adverb;
		}
		return null;
	}

	public static List<TextRepresentation> makeTextRepresentationList(
			final String text, final String language) {
		List<TextRepresentation> result = new LinkedList<TextRepresentation>();
		TextRepresentation textRepresentation = new TextRepresentation();
		textRepresentation.setLanguageIdentifier(language);
		textRepresentation.setWrittenText(text);
		result.add(textRepresentation);
		return result;
	}

	
    /**
     * Makes a generic id (like i.e. 'wn_lex_2') partitioned by {@code clazz}
     *  using {@link #NAMESPACES} mappings.  
     */
    public static String makeId(String prefix, Class clazz, String value) {
        return prefix + "_" + NAMESPACES.get(clazz) + "_" + value;
    }

    /**
     * div new
     * 
     * Sanitizes ids so they can be valid xml ids for DTDs
     * (technically, they need to be NCNames).
     */
    public static String xmlId(String wnId){
        if (wnId == null){
            throw new IllegalArgumentException("Tried to parse a null wordnet id!");
        }
        if (wnId.trim().isEmpty()){
            throw new IllegalArgumentException("Tried to parse a blank wordnet id!");
        }
        
        Pattern p = Pattern.compile("[^\\p{L}|\\.|\\_|-]", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher m = p.matcher(wnId);
        StringBuilder sb = new StringBuilder();        
        int lastEnd = 0;
        
        while (m.find()){          
            sb.append(wnId.substring(lastEnd, m.start()));
            if (m.group().length() > 1){
                logger.warn("Found group in id of length > 1: " + m.group());
            }
            int codePoint = Character.codePointAt(wnId, m.start());
            String charName = Character.getName(codePoint);
            if (charName == null){
                sb.append("-");
            } else {               
                sb.append("_"+charName.replaceAll(p.toString(), "-").toLowerCase() + "_");    
            }
            
            lastEnd = m.end();
        }
        if (lastEnd < wnId.length()){
            sb.append(wnId.substring(lastEnd, wnId.length()));
        }
                
        return sb.toString();
    }
        

    // div new
    private WNConvUtil(){}

    /**
     * div new
     * 
     * Returns something like wn_le_n-vehicle
     */
    public static String makeLexicalEntryId(String prefix, POS pos, String lemma) {        
        return makeId(prefix, LexicalEntry.class, pos.getKey() + "-" + xmlId(lemma));
    }    
    
    /**
     * div new
     * 
     * Returns something like wn_syn_n321534
     */
    public static String makeSynsetId(String prefix, net.sf.extjwnl.data.Synset wnSynset) {
        return WNConvUtil.makeId(prefix,
                Synset.class, 
                wnSynset.getPOS().getKey() + Long.toString(wnSynset.getOffset()));
    }
    
}
