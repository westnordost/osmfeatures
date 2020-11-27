package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Index that makes finding strings that start with characters very efficient.
 *  It sorts the strings into a tree structure with configurable depth.
 *
 *  It is threadsafe because it is immutable.
 *
 *  For the strings ["f", "foobar", "foo", "fu", "funicular"], the tree may internally look f.e.
 *  like this:
 *  <pre>
 *  f ->
 *    [ "f" ]
 *    o ->
 *      o ->
 *        [ "foobar", "foo", ...]
 *    u ->
 *      [ "fu", "funicular", ... ]
 *  </pre>*/
class StartsWithStringTree
{
    private final Node root;

    public StartsWithStringTree(Collection<String> strings)
    {
        this(strings, 16, 16);
    }

    /** Create this index with the given strings.
     *
     *  The generated tree will have a max depth of maxDepth and another depth is not added to the
     *  tree if there are less than minContainerSize strings in one tree node.
     */
    public StartsWithStringTree(Collection<String> strings, int maxDepth, int minContainerSize)
    {
        if (maxDepth < 0) maxDepth = 0;
        if (minContainerSize < 1) minContainerSize = 1;
        root = buildTree(strings, 0, maxDepth, minContainerSize);
    }

    /** Get all strings which start with the given string */
    public List<String> getAll(String startsWith)
    {
        return root.getAll(startsWith, 0);
    }

    private static Node buildTree(Collection<String> strings, int currentDepth, int maxDepth, int minContainerSize)
    {
        if (currentDepth == maxDepth || strings.size() < minContainerSize)
            return new Node(null, strings);

        HashMap<Character, Node> children = new HashMap<>();

        Map<Character, Collection<String>> stringsByCharacter = getStringsByCharacter(strings, currentDepth);
        for (Map.Entry<Character, Collection<String>> entry : stringsByCharacter.entrySet()) {
            Character c = entry.getKey();
            if (c == null) continue;
            Node child = buildTree(entry.getValue(), currentDepth + 1, maxDepth, minContainerSize);
            children.put(c, child);
        }
        Collection<String> remainingStrings = stringsByCharacter.get(null);
        if (children.isEmpty()) children = null;
        return new Node(children, remainingStrings);
    }

    /** returns the given strings grouped by their nth character. Strings whose length is shorter
     *  or equal to nth go into the "null" group. */
    private static Map<Character, Collection<String>> getStringsByCharacter(Collection<String> strings, int nth)
    {
        HashMap<Character, Collection<String>> result = new HashMap<>();
        for (String string : strings) {
            Character c = string.length() > nth ? string.charAt(nth) : null;
            if (!result.containsKey(c)) result.put(c, new ArrayList<>());
            result.get(c).add(string);
        }
        return result;
    }

    private static class Node
    {
        final Map<Character, Node> children;
        final Collection<String> strings;

        private Node(Map<Character, Node> children, Collection<String> strings)
        {
            this.children = children;
            this.strings = strings;
        }

        /** Get all strings that start with the given string */
        private List<String> getAll(String startsWith, int offset)
        {
            if (startsWith.isEmpty()) return Collections.emptyList();

            List<String> result = new ArrayList<>();
            if (children != null)
            {
                for (Map.Entry<Character, Node> charToNode : children.entrySet())
                {
                    if (startsWith.length() <= offset || charToNode.getKey() == startsWith.charAt(offset))
                    {
                        result.addAll(charToNode.getValue().getAll(startsWith, offset + 1));
                    }
                }
            }
            if (strings != null)
            {
                for (String string : strings)
                {
                    if (string.startsWith(startsWith)) result.add(string);
                }
            }
            return result;
        }
    }
}
