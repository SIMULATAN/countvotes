package com.github.simulatan.countvotes.utils;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class VotesManager {

	public static final VotesMap votes = new VotesMap();
	public static final List<Vote> recentVotes = new ArrayList<>();

	public static void removeVote(Vote vote) {
		votes.removeVote(vote);
		recentVotes.remove(vote);
	}

	public static void addVote(Vote vote) {
		votes.addVote(vote);
		recentVotes.add(vote);
	}

	/**
	 * Dummy
	 */
	private static final List<Vote> EMPTY_LIST = ImmutableList.of();
	public static int getVotes(Candidate candidate) {
		return votes.getOrDefault(candidate, EMPTY_LIST).size();
	}

	public static void clearVotes() {
		votes.clear();
	}
}