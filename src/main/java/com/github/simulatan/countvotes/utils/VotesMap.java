package com.github.simulatan.countvotes.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VotesMap extends HashMap<Candidate, List<Vote>> {

	public List<String> getCandidateStartingWith(String prefix) {
		return this
			.keySet()
			.stream()
			.map(Candidate::getName)
			.filter(Objects::nonNull)
			.filter(key -> key.startsWith(prefix))
			.collect(Collectors.toList());
	}

	void addVote(Vote vote) {
		final List<Vote> voteList = this.getOrDefault(vote.getCandidate(), new ArrayList<>());
		voteList.add(vote);
		this.put(vote.getCandidate(), voteList);
	}

	void removeVote(Vote vote) {
		final List<Vote> voteList = this.getOrDefault(vote.getCandidate(), new ArrayList<>());
		voteList.remove(vote);
		this.put(vote.getCandidate(), voteList);
	}
}