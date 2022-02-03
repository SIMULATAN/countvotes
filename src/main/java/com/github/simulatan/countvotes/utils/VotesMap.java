package com.github.simulatan.countvotes.utils;

import java.util.*;
import java.util.stream.Collectors;

public class VotesMap extends HashMap<Candidate, List<Vote>> {

	public List<String> getCandidateStartingWith(String prefix) {
		return this
			.keySet()
			.stream()
			.map(Candidate::getName)
			.filter(Objects::nonNull)
			.filter(key -> key.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
			.collect(Collectors.toList());
	}

	void addVote(Vote vote) {
		final List<Vote> voteList = this.getOrDefault(vote.candidate(), new ArrayList<>());
		voteList.add(vote);
		this.put(vote.candidate(), voteList);
	}

	void removeVote(Vote vote) {
		final List<Vote> voteList = this.getOrDefault(vote.candidate(), new ArrayList<>());
		voteList.remove(vote);
		this.put(vote.candidate(), voteList);
	}
}