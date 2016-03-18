package Model;

import java.util.ArrayList;

/**
 * @author Robin Duda
 * <p/>
 * Contains a list of votings for use in transit.
 */
public class VotingList {
    private ArrayList<Voting> votings = new ArrayList<>();

    public ArrayList<Voting> getVotings() {
        return votings;
    }

    public void setVotings(ArrayList<Voting> votings) {
        this.votings = votings;
    }

    public void add(Voting voting) {
        votings.add(voting);
    }
}

