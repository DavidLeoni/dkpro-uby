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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static de.tudarmstadt.ukp.lmf.transform.wordnet.util.WNConvUtil.xmlId;
import static de.tudarmstadt.ukp.lmf.transform.wordnet.util.WNConvUtil.makeLexicalEntryId;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.Exc;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import org.junit.Test;

import de.tudarmstadt.ukp.lmf.model.enums.EPartOfSpeech;
import junit.framework.Assert;

/**
 * Tests methods of {@link WNConvUtil} class.<br>
 *
 * Tests are made for WordNet 3.0
 * data and UBY-LMF DTD version 0.2.0.
 *
 * @author Zijad Maksuti
 *
 * @since 0.2.0
 *
 */
 public class WNConvUtilTest {
     
     private final static Log logger = LogFactory.getLog(WNConvUtilTest.class);

	@Test
	public void testGetPos() {
		assertEquals(EPartOfSpeech.noun, WNConvUtil.getPOS(POS.NOUN));
		assertEquals(EPartOfSpeech.verb, WNConvUtil.getPOS(POS.VERB));
		assertEquals(EPartOfSpeech.adjective, WNConvUtil.getPOS(POS.ADJECTIVE));
		assertEquals(EPartOfSpeech.adverb, WNConvUtil.getPOS(POS.ADVERB));
	}

	
    /**
     * div new
     */
    @Test
    public void testLexicalEntryId(){
        
        assertEquals("wn_le_n_hello", makeLexicalEntryId("wn", POS.NOUN, "hello"));
        assertEquals("wn_le_n_-hood", makeLexicalEntryId("wn", POS.NOUN, "'hood"));
    }	
	
	/**
	 * div new
	 */
	@Test
	public void testXmlId(){
	    	    
	    assertEquals("_EURO-SIGN_", xmlId("€"));
	    assertEquals("_", xmlId("_"));
	    assertEquals("_.", xmlId("."));
	    assertEquals("a_COLON_b", xmlId("a:b"));
	    assertEquals("_-", xmlId("-"));
	    assertEquals("_.", xmlId("."));
	    assertEquals("_..", xmlId(".."));
	    // spaces
	    assertEquals("a-b", xmlId("a b"));
	    assertEquals("a-b", xmlId("a\tb"));
	    assertEquals("a-b", xmlId("a\nb"));
	    assertEquals("a", xmlId(" a"));
	    assertEquals("_7", xmlId("7"));
	    assertEquals("_78", xmlId("78"));
	    assertEquals("a7", xmlId("a7"));
	    assertEquals("a-", xmlId("a'"));
	    assertEquals("_-a", xmlId("'a"));
	    assertEquals("_-", xmlId("'"));
	    assertEquals("_REVERSE-SOLIDUS_z_EURO-SIGN_", xmlId("\\z€"));
	    
       try {
            assertEquals("", xmlId(null));
            Assert.fail("Shouldn't arrive here!");
        } catch (IllegalArgumentException ex){          
        }
       
       try {
           assertEquals("", xmlId(" "));
           Assert.fail("Shouldn't arrive here!");
       } catch (IllegalArgumentException ex){          
       }
        
        try {
            assertEquals("", xmlId(""));
            Assert.fail("Shouldn't arrive here!");
        } catch (IllegalArgumentException ex){          
        }

	}
	
	@Test
	public void testMakeSenseId() throws JWNLException{
	    Dictionary d = Dictionary.getDefaultResourceInstance();
	    
	    Word w = d.getSynsetIterator(POS.NOUN).next().getWords().get(0);
	    
	    logger.debug("Sense key : " + w.getSenseKey());
	    logger.debug("lmf id    : " + WNConvUtil.makeSenseId("wn30",w, -3));
	    logger.debug("lmf id    : " + WNConvUtil.makeSenseId("wn30",w, 2));
	    
	}
	
	
	/**
     * div new
     */
	// chlamydes chlamys
	// chlamyses chlamys
	// Chlamys: http://wordnetweb.princeton.edu/perl/webwn?s=chlamys&sub=Search+WordNet&o2=1&o0=1&o8=1&o1=1&o7=1&o5=1&o9=&o6=1&o3=1&o4=1&h=00
	
	// https://commons.wikimedia.org/wiki/Category:Chlamydes
    @Test
    public void testExceptions() throws JWNLException{
        
        Dictionary d = Dictionary.getDefaultResourceInstance();
        Map<POS, HashMap<String, List<String>>> m = WNConvUtil.buildExceptionMap(d);
        
        List<String> weirdForms = m.get(POS.NOUN).get("mouse");
        assertEquals(1, weirdForms.size());
        
        assertEquals("mice", weirdForms.get(0));        
        
       // with a space like the writtenForm !
        List<String> weirdForms2 = m.get(POS.VERB).get("goose step");
        assertNotNull(weirdForms2);
        assertEquals(2, weirdForms2.size());
        
        assertEquals("goose-stepped", weirdForms2.get(0));
        
        
        
        
    }
    
	/**
	 * div new
	 */
	@Test
	public void testExtJwnlExceptions() throws JWNLException{
	    Dictionary d = Dictionary.getDefaultResourceInstance();
        
	    
	    String mouse = "mouse";
        ArrayList<String> vmouse = new ArrayList<>();
	    vmouse.add(mouse);

        String mice = "mice";
        ArrayList<String> vmice = new ArrayList<>();
        vmice.add(mice);
        
        
        IndexWord w1 = d.lookupIndexWord(POS.VERB,"goose-step");
        IndexWord w2 = d.lookupIndexWord(POS.VERB,"goose step");
        IndexWord w3 = d.lookupIndexWord(POS.VERB, "join battle");
        IndexWord w4 = d.lookupIndexWord(POS.NOUN,"'s Gravenhage");
        IndexWord w5 = d.lookupIndexWord(POS.NOUN,"s-Gravenhage");
        IndexWord w6 = d.lookupIndexWord(POS.NOUN,"high-hat");
        IndexWord w7 = d.lookupIndexWord(POS.NOUN,"high hat");
        
	    
        // NOTE: key is _the same_ as lemma
        logger.info("   lemma: " + w1.getLemma());
        logger.info("   lemma: " + w2.getLemma());
        logger.info("   lemma: " + w3.getLemma());
        logger.info("   lemma: " + w4.getLemma());
        logger.info("   lemma: " + w5.getLemma());
        logger.info("   lemma: " + w6.getLemma());
        logger.info("   lemma: " + w7.getLemma());
        
	    assertEquals(vmouse, d.getMorphologicalProcessor().lookupAllBaseForms(POS.NOUN, mouse));	    	    
	    assertEquals(vmouse, d.getMorphologicalProcessor().lookupAllBaseForms(POS.NOUN, mice));
	    	    
	    assertEquals(vmouse, d.getMorphologicalProcessor().lookupAllBaseForms(POS.VERB, "mousing"));
	    
	    Iterator<Exc> it = d.getExceptionIterator(POS.NOUN);
	    boolean foundMice = false;	   
	    
        while(it.hasNext()){
            Exc exc = it.next();
                       
            // the 'lemma' is actually the exception!
            if ("mice".equals(exc.getLemma())){
                foundMice = true;
                assertEquals("mouse", exc.getExceptions().get(0));                
            }            
        }
        
        assertTrue(foundMice);
        	    
        assertEquals(null, d.getException(POS.NOUN, mouse));               
        
        Exc mexc = d.getException(POS.NOUN, mice);
        
        assertEquals(mice, mexc.getLemma());
        assertEquals(mouse, mexc.getExceptions().get(0));               
        
	}
	
	
}
