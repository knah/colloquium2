package ru.ifmo.md.colloquium2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kna on 11.11.14.
 */
public class VotingData {
    private static Map<String, Integer> data = new HashMap<String, Integer>();
    private static ArrayList<String> candidates = new ArrayList<String>();
    private static int numVotes = 0;

    public static void removeCandidate(String name) {
        data.remove(name);

        candidates.clear();
        candidates.addAll(data.keySet());
    }

    public static boolean renameCandidate(String oldName, String newName) {
        if(oldName.isEmpty()) {
            return false;
        }

        for(int i = 0; i < candidates.size(); i++) {
            if(candidates.get(i).equals(oldName)) {
                candidates.set(i, newName);
                data.remove(oldName);
                data.put(newName, 0);
                return true;
            }
        }

        return false;
    }

    static void clearVotes() {
        for(String s : data.keySet()) {
            data.put(s, 0);
        }
            numVotes = 0;
    }

    public static boolean addCandidate(String name) {
        if(data.containsKey(name) || name.isEmpty()) {
            return false;
        }

        candidates.add(name);
        data.put(name, 0);
        return true;
    }

    public static boolean addVote(String name) {
        if(!data.containsKey(name)) {
            return false;
        }

        numVotes++;
        data.put(name, data.get(name) + 1);
        return true;
    }

    public static int getCount() {
        return candidates.size();
    }

    public static String getCandidateName(int idx) {
        return candidates.get(idx);
    }

    public static int getCandidateVotes(String name) {
        return data.get(name);
    }

    public static void clear() {
        candidates.clear();
        data.clear();
        numVotes = 0;
    }

    public static String getPercentage(String candidateName) {
        if(!data.containsKey(candidateName))
            return "invalid";

        if(numVotes == 0)
            return "no votes";

        return data.get(candidateName) * 100 / numVotes + "%";
    }

    public static void sortByVotes() {
        Collections.sort(candidates, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int vl = data.get(lhs);
                int vr = data.get(rhs);
                if(vl == vr) {
                    return 0;
                } else if(vl > vr) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    public static String writeData() {
        StringBuilder builder = new StringBuilder();
        builder.append(numVotes).append("\n");
        for(String s : data.keySet()) {
            builder.append(s).append("\r").append(data.get(s)).append("\n");
        }
        return builder.toString();
    }

    public static void readData(String dataString) {
        String[] lines = dataString.split("\n");
        for(String l : lines) {
            String[] inline = l.split("\r");
            if(inline.length == 1) {
                numVotes = Integer.parseInt(inline[0]);
            } else {
                data.put(inline[0], Integer.parseInt(inline[1]));
                candidates.add(inline[0]);
            }
        }
    }
}
