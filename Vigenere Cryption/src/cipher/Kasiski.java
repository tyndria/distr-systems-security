package cipher;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import java.lang.*;
import java.math.BigInteger;
import java.io.*;

public class Kasiski {
  
    private static Collection<Repeat> findRepeats(String cipherText) {
        String stringToSearch; 
        int MIN_LENGTH = 3;
        Boolean alreadyFound, tryNextPeriod = true;
        Repeat repeat;
        HashMap results = new HashMap<String, Repeat>();
        
        for (int period = MIN_LENGTH; tryNextPeriod; period++) {
            tryNextPeriod = false;
            for (int i = 0; i <= cipherText.length() - period*2 ; i++) {
                alreadyFound = false;
                stringToSearch = getOnlyLetters(cipherText.substring(i, i + period));
                
                if (stringToSearch.length() >= MIN_LENGTH) {
                	for (int j = i + period; j <= cipherText.length() - period; j++) {
                    	String substr = getOnlyLetters(cipherText.substring(j, j + period));
                        if (substr.equals(stringToSearch)) {
                            tryNextPeriod = true;
                            if (!alreadyFound) {
                                if (!results.containsKey(stringToSearch)) {
                                	results.put(stringToSearch, new Repeat(stringToSearch, i));
                                }
                                alreadyFound = true;
                            } 

                            repeat = ((Repeat) results.get(stringToSearch));
                            if (!repeat.locations.contains(j) && !repeat.locations.contains(j + 1)) {
                            	repeat.locations.add(j);
                            }
                            j += period + 1;
                        }
                    }
                }
            }
        }

        return results.values();
    }
    
    private static String getOnlyLetters(String text) {
    	return text.replaceAll("[^a-zA-Z]","");
    }
    
    public static int getKeywordLength(String cipherText) {
    	Collection<Repeat> repeats = findRepeats(cipherText);

        List<Repeat> list = new ArrayList<Repeat>(repeats);
        list = list.stream().filter(x -> x.locations.size() > 2).collect(Collectors.toList());
        
        HashMap<Integer, Integer> gcdFrequency = new HashMap<>();
        for (Repeat repeat : list) {
        	int gcd = findDistancesGCD(repeat);
        	if (!gcdFrequency.containsKey(gcd)) {
        		gcdFrequency.put(gcd, 1);
        	} else {
        		int value = gcdFrequency.get(gcd);
        		gcdFrequency.put(gcd, ++value);
        	}
        }
        Set<Entry<Integer, Integer>> entrySet = gcdFrequency.entrySet();
        List<Entry<Integer, Integer>> sorted = entrySet.stream().sorted((a, b) -> b.getValue() - a.getValue()).collect(Collectors.toList());
        return sorted.get(0).getKey();
    }
    
    private static int findDistancesGCD(Repeat repeat) {
    	int firstDistance = repeat.locations.get(1) - repeat.locations.get(0);
		int secondDistance = repeat.locations.get(2) - repeat.locations.get(1);
		BigInteger currentGCD = new BigInteger(firstDistance + "").gcd(new BigInteger(secondDistance + ""));
    	for (int i = 2; i < repeat.locations.size() - 1; i ++) {
    		int distance = repeat.locations.get(i + 1) - repeat.locations.get(i);
    		currentGCD = currentGCD.gcd(new BigInteger(distance + ""));
    	}
    	return currentGCD.intValue();
    }
    
    private static class Repeat implements Comparable<Repeat> {
    	public String word;
        public List<Integer> locations;
         
        public Repeat(String word, int firstLocation) {
            this.word = word;
            this.locations = new LinkedList<Integer>();
            this.locations.add(firstLocation);
        }

        public int compareTo(Repeat repeat) {
            if (this.word.length() != repeat.word.length()) {
            	return repeat.word.length() - this.word.length();
            } else if (this.locations.size() != repeat.locations.size()) {
            	return repeat.locations.size() - this.locations.size();
            } else {
            	return this.word.compareTo(repeat.word);
            }
        }
    }
}