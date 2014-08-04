package edu.cmu.geolocator.nlp.tokenizer;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * TweetMotif is licensed under the Apache License 2.0:
 * http://www.apache.org/licenses/LICENSE-2.0.html Copyright Brendan O'Connor,
 * Michel Krieger, and David Ahn, 2009-2010.
 */

/*
 * Scala verion of TweetMotif is licensed under the Apache License 2.0:
 * http://www.apache.org/licenses/LICENSE-2.0.html Copyright Jason Baldridge,
 * and David Snyder, 2011.
 */

/*
 * A direct port to Java from Scala version of Twitter tokenizer at
 * https://bitbucket.org/jasonbaldridge/twokenize Original Python version
 * TweetMotif can be found at https://github.com/brendano/tweetmotif
 * 
 * Author: Vinh Khuc (khuc@cse.ohio-state.edu) July 2011
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;

/*
 * The tokenizer from Noah's Ark.
 * 
 * @ Modified by Wei Zhang
 */
public class EuroLangTwokenizer {

  static Pattern Contractions = Pattern
          .compile("(?i)(\\w+)(['’′]t|['’′]ve|['’′]ll|['’′]d|['’′]re|['’′]s|['’′]m)$");

  static Pattern Whitespace = Pattern.compile("[\\s\\p{Zs}]+");

  // @ Wei Zhang
  // added question marks and exclamation mark, and long dash.
  // added ( and ) as punctuations
  static String punctChars = "['\"“”‘’\\|.—¿?¡!…,:;]";

  // static String punctSeq = punctChars+"+"; //'anthem'. => ' anthem '.
  static String punctSeq = "['\"“”‘’/_]+|[.¿?¡!,…]+|[:;]+"; // 'anthem'. =>

  // ' anthem '
  // .
  static String entity = "&(?:amp|lt|gt|quot);";

  // URLs

  // BTO 2012-06: everyone thinks the daringfireball regex should be better,
  // but they're wrong.
  // If you actually empirically test it the results are bad.
  // Please see https://github.com/brendano/ark-tweet-nlp/pull/9

  static String urlStart1 = "(?:https?://|\\bwww\\.)";

  static String commonTLDs = "(?:com|org|edu|gov|net|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|pro|tel|travel|xxx)";

  static String ccTLDs = "(?:ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|"
          + "bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|"
          + "er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|"
          + "hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|"
          + "lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|"
          + "nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|"
          + "sl|sm|sn|so|sr|ss|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|"
          + "va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|za|zm|zw)"; // TODO:

  // remove
  // obscure
  // country
  // domains?
  static String urlStart2 = "\\b(?:[A-Za-z\\d-])+(?:\\.[A-Za-z0-9]+){0,3}\\." + "(?:" + commonTLDs
          + "|" + ccTLDs + ")" + "(?:\\." + ccTLDs + ")?(?=\\W|$)";

  // Wei Zhang added urlStart3 for HTTP://
  static String urlStart3 = "(?:HTTPS?://|\\bwww\\.)";

  // ////////

  static String urlBody = "(?:[^\\.\\s<>][^\\s<>]*?)?";

  static String urlExtraCrapBeforeEnd = "(?:" + punctChars + "|" + entity + ")+?";

  static String urlEnd = "(?:\\.\\.+|[<>]|\\s|$)";

  public static String url = "(?:" + urlStart1 + "|" + urlStart2 + "|" + urlStart3 + ")" + urlBody
          + "(?=(?:" + urlExtraCrapBeforeEnd + ")?" + urlEnd + ")";

  // Numeric
  static String timeLike = "\\d+(?::\\d+){1,2}";

  // static String numNum = "\\d+\\.\\d+";
  static String numberWithCommas = "(?:(?<!\\d)\\d{1,3},)+?\\d{3}" + "(?=(?:[^,\\d]|$))";

  static String numComb = "\\p{Sc}?\\d+(?:\\.\\d+)+%?";

  // Abbreviations
  static String boundaryNotDot = "(?:$|\\s|[“\\u0022?!,:;]|" + entity + ")";

  static String aa1 = "(?:[A-Za-z]\\.){2,}(?=" + boundaryNotDot + ")";

  static String aa2 = "[^A-Za-z](?:[A-Za-z]\\.){1,}[A-Za-z](?=" + boundaryNotDot + ")";

  static String standardAbbreviations = "\\b(?:[Mm]r|[Mm]rs|[Mm]s|[Dd]r|[Ss]r|[Jj]r|[Rr]ep|[Ss]en|[Ss]t)\\.";

  static String arbitraryAbbrev = "(?:" + aa1 + "|" + aa2 + "|" + standardAbbreviations + ")";

  static String separators = "(?:--+|―|—|~|–|=)";

  static String decorations = "(?:[♫♪]+|[★☆]+|[♥❤♡]+|[\\u2639-\\u263b]+|[\\ue001-\\uebbb]+)";

  static String thingsThatSplitWords = "[^\\s\\.,?\"\\-]";

  static String embeddedApostrophe = thingsThatSplitWords + "+['’′]" + thingsThatSplitWords
          + "*";

  public static String OR(String... parts) {
    String prefix = "(?:";
    StringBuilder sb = new StringBuilder();
    for (String s : parts) {
      sb.append(prefix);
      prefix = "|";
      sb.append(s);
    }
    sb.append(")");
    return sb.toString();
  }

  // Emoticons
  static String normalEyes = "(?iu)[:=]"; // 8 and x are eyes but cause

  // problems
  static String wink = "[;]";

  static String noseArea = "(?:|-|[^a-zA-Z0-9 ])"; // doesn't get :'-(

  static String happyMouths = "[D\\)\\]\\}]+";

  static String sadMouths = "[\\(\\[\\{]+";

  static String tongue = "[pPd3]+";

  static String otherMouths = "(?:[oO]+|[/\\\\]+|[vV]+|[Ss]+|[|]+)"; // remove

  // forward
  // slash
  // if
  // http://'s
  // aren't
  // cleaned

  // mouth repetition examples:
  // @aliciakeys Put it in a love song :-))
  // @hellocalyclops =))=))=)) Oh well

  static String bfLeft = "(♥|0|o|°|v|\\$|t|x|;|\\u0CA0|@|ʘ|•|・|◕|\\^|¬|\\*)";

  static String bfCenter = "(?:[\\.]|[_-]+)";

  static String bfRight = "\\2";

  static String s3 = "(?:--['\"])";

  static String s4 = "(?:<|&lt;|>|&gt;)[\\._-]+(?:<|&lt;|>|&gt;)";

  static String s5 = "(?:[.][_]+[.])";

  static String basicface = "(?:(?i)" + bfLeft + bfCenter + bfRight + ")|" + s3 + "|" + s4 + "|"
          + s5;

  static String eeLeft = "[＼\\\\ƪԄ\\(（<>;ヽ\\-=~\\*]+";

  static String eeRight = "[\\-=\\);'\\u0022<>ʃ）/／ノﾉ丿╯σっµ~\\*]+";

  static String eeSymbol = "[^A-Za-z0-9\\s\\(\\)\\*:=-]";

  static String eastEmote = eeLeft + "(?:" + basicface + "|" + eeSymbol + ")+" + eeRight;

  public static String emoticon = OR(
  // Standard version :) :( :] :D :P
          "(?:>|&gt;)?"
                  + OR(normalEyes, wink)
                  + OR(noseArea, "[Oo]")
                  + OR(tongue + "(?=\\W|$|RT|rt|Rt)", otherMouths + "(?=\\W|$|RT|rt|Rt)",
                          sadMouths, happyMouths),

          // reversed version (: D: use positive lookbehind to remove
          // "(word):"
          // because eyes on the right side is more ambiguous with the
          // standard usage of : ;
          "(?<=(?: |^))" + OR(sadMouths, happyMouths, otherMouths) + noseArea
                  + OR(normalEyes, wink) + "(?:<|&lt;)?",

          // inspired by
          // http://en.wikipedia.org/wiki/User:Scapler/emoticons#East_Asian_style
          eastEmote.replaceFirst("2", "1"), basicface
  // iOS 'emoji' characters (some smileys, some symbols) [\ue001-\uebbb]
  // TODO should try a big precompiled lexicon from Wikipedia, Dan Ramage
  // told
  // me (BTO) he does this
  );

  static String Hearts = "(?:<+/?3+)+"; // the other hearts are in

  // decorations

  static String Arrows = "(?:<*[-―—=]*>+|<+[-―—=]*>*)|\\p{InArrows}+";

  // BTO 2011-06: restored Hashtag, AtMention protection (dropped in
  // original
  // scala port) because it fixes
  // "hello (#hashtag)" ==> "hello (#hashtag )" WRONG
  // "hello (#hashtag)" ==> "hello ( #hashtag )" RIGHT
  // "hello (@person)" ==> "hello (@person )" WRONG
  // "hello (@person)" ==> "hello ( @person )" RIGHT
  // ... Some sort of weird interaction with edgepunct I guess, because
  // edgepunct
  // has poor content-symbol detection.

  // This also gets #1 #40 which probably aren't hashtags .. but good as
  // tokens.
  // If you want good hashtag identification, use a different regex.
  static String Hashtag = "#[a-zA-Z0-9_]+"; // optional: lookbehind for \b

  // optional: lookbehind for \b, max length 15
  static String AtMention = "[@＠][a-zA-Z0-9_]+";

  // I was worried this would conflict with at-mentions
  // but seems ok in sample of 5800: 7 changes all email fixes
  // http://www.regular-expressions.info/email.html
  static String Bound = "(?:\\W|^|$)";

  public static String Email = "(?<=" + Bound
          + ")[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(?=" + Bound + ")";

  // We will be tokenizing using these regexps as delimiters
  // Additionally, these things are "protected", meaning they shouldn't be
  // further split themselves.
  static Pattern Protected = Pattern.compile(OR(Hearts, url, Email, timeLike,
          // numNum,
          numberWithCommas, numComb, emoticon, Arrows, entity, punctSeq, arbitraryAbbrev,
          separators, decorations, embeddedApostrophe, Hashtag, AtMention));

  // Edge punctuation
  // Want: 'foo' => ' foo '
  // While also: don't => don't
  // the first is considered "edge punctuation".
  // the second is word-internal punctuation -- don't want to mess with it.
  // BTO (2011-06): the edgepunct system seems to be the #1 source of
  // problems
  // these days.
  // I remember it causing lots of trouble in the past as well. Would be
  // good
  // to revisit or eliminate.

  // Note the 'smart quotes' (http://en.wikipedia.org/wiki/Smart_quotes)
  static String edgePunctChars = "'\"“”‘’«»{}\\(\\)\\[\\]\\*&"; // add

  // \\p{So}?
  // (symbols)
  static String edgePunct = "[" + edgePunctChars + "]";

  static String notEdgePunct = "[a-zA-Z0-9]"; // content characters

  // @Wei Zhang: Fixed bug ??(peru) ->?? ( peru ).
  // added ! and ? and ¿ and ¡.
  static String offEdge = "(^|$|:|;|!|\\?|¡|¿|\\s|\\.|,)"; // colon here

  // gets
  // "(hello):"
  // ==>
  // "( hello ):"
  static Pattern EdgePunctLeft = Pattern.compile(offEdge + "(" + edgePunct + "+)(" + notEdgePunct
          + ")");

  static Pattern EdgePunctRight = Pattern.compile("(" + notEdgePunct + ")(" + edgePunct + "+)"
          + offEdge);

  public static String splitEdgePunct(String input) {
    Matcher m1 = EdgePunctLeft.matcher(input);
    input = m1.replaceAll("$1$2 $3");
    m1 = EdgePunctRight.matcher(input);
    input = m1.replaceAll("$1 $2$3");
    return input;
  }

  private static class Pair<T1, T2> {
    public T1 first;

    public T2 second;

    public Pair(T1 x, T2 y) {
      first = x;
      second = y;
    }
  }

  // The main work of tokenizing a tweet.
  private static List<String> simpleTokenize(String text) {

    // Do the no-brainers first
    String splitPunctText = splitEdgePunct(text);

    int textLength = splitPunctText.length();

    // BTO: the logic here got quite convoluted via the Scala porting
    // detour
    // It would be good to switch back to a nice simple procedural style
    // like in the Python version
    // ... Scala is such a pain. Never again.

    // Find the matches for subsequences that should be protected,
    // e.g. URLs, 1.0, U.N.K.L.E., 12:53
    Matcher matches = Protected.matcher(splitPunctText);
    // Storing as List[List[String]] to make zip easier later on
    List<List<String>> bads = new ArrayList<List<String>>(); // linked
    // list?
    List<Pair<Integer, Integer>> badSpans = new ArrayList<Pair<Integer, Integer>>();
    while (matches.find()) {
      // The spans of the "bads" should not be split.
      if (matches.start() != matches.end()) { // unnecessary?
        List<String> bad = new ArrayList<String>(1);
        bad.add(splitPunctText.substring(matches.start(), matches.end()));
        bads.add(bad);
        badSpans.add(new Pair<Integer, Integer>(matches.start(), matches.end()));
      }
    }

    // Create a list of indices to create the "goods", which can be
    // split. We are taking "bad" spans like
    // List((2,5), (8,10))
    // to create
    // / List(0, 2, 5, 8, 10, 12)
    // where, e.g., "12" here would be the textLength
    // has an even length and no indices are the same
    List<Integer> indices = new ArrayList<Integer>(2 + 2 * badSpans.size());
    indices.add(0);
    for (Pair<Integer, Integer> p : badSpans) {
      indices.add(p.first);
      indices.add(p.second);
    }
    indices.add(textLength);

    // Group the indices and map them to their respective portion of the
    // string
    List<List<String>> splitGoods = new ArrayList<List<String>>(indices.size() / 2);
    for (int i = 0; i < indices.size(); i += 2) {
      String goodstr = splitPunctText.substring(indices.get(i), indices.get(i + 1));
      List<String> splitstr = Arrays.asList(goodstr.trim().split(" "));
      splitGoods.add(splitstr);
    }

    // Reinterpolate the 'good' and 'bad' Lists, ensuring that
    // additonal tokens from last good item get included
    List<String> zippedStr = new ArrayList<String>();
    int i;
    for (i = 0; i < bads.size(); i++) {
      zippedStr = addAllnonempty(zippedStr, splitGoods.get(i));
      zippedStr = addAllnonempty(zippedStr, bads.get(i));
    }
    zippedStr = addAllnonempty(zippedStr, splitGoods.get(i));

    // BTO: our POS tagger wants "ur" and "you're" to both be one token.
    // Uncomment to get "you 're"

    ArrayList<String> splitStr = new ArrayList<String>(zippedStr.size());
    for (String tok : zippedStr)
      splitStr.addAll(splitToken(tok));
    zippedStr = splitStr;

    return zippedStr;
  }

  private static List<String> addAllnonempty(List<String> master, List<String> smaller) {
    for (String s : smaller) {
      String strim = s.trim();
      if (strim.length() > 0)
        master.add(strim);
    }
    return master;
  }

  /** "foo   bar " => "foo bar" */
  public static String squeezeWhitespace(String input) {
    return Whitespace.matcher(input).replaceAll(" ").trim();
  }

  // Final pass tokenization based on special patterns
  private static List<String> splitToken(String token) {

    Matcher m = Contractions.matcher(token);
    if (m.find()) {
      String[] contract = { m.group(1), m.group(2) };
      // System.out.println(Arrays.asList(contract).toString());
      return Arrays.asList(contract);
    }
    String[] contract = { token };
    // System.out.println(Arrays.asList(contract).toString());

    return Arrays.asList(contract);
  }

  // @Wei Zhang
  // modified return type from list to string array
  /** Assume 'text' has no HTML escaping. **/
  public static List<String> tokenize(String text) {
    return simpleTokenize(squeezeWhitespace(text));
  }
/**
 * Take charge of generating tokens in a sentence. A raw sentence won't have tokens until it is tokenized.
 * @param sentence
 * @return
 */
  public static edu.cmu.geolocator.model.Sentence tokenize(edu.cmu.geolocator.model.Sentence sentence) {
    String sSent = sentence.getSentenceString();
    List<String> tokenized = tokenize(sSent);
    List<Token> tokens = new ArrayList<Token>();
    Token t;
    for (int i = 0; i < tokenized.size(); i++) {
      tokens.add(new Token(tokenized.get(i),sentence.getId(),i));
    }
    sentence.setTokens(tokens.toArray(new Token[]{}));
    return sentence;
  }

  public static void main(String[] args) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
    System.out.print("> ");
    // Read user input
    String inputStr = br.readLine();
    while (!inputStr.equals("")) {

      // inputStr = "terremoto/earthquake";
      List<String> tokens = EuroLangTwokenizer.tokenize(inputStr);
      for (String token : tokens) {
        System.out.print("[" + token + "]");

      }
      System.out.print("\n> ");
      inputStr = br.readLine();
    }
    br.close();
  }

}