package com.github.simulatan.countvotes.utils;

public class Vote {

	private final Candidate candidate;
	private final long time = System.currentTimeMillis();

	public Vote(Candidate candidate) {
		this.candidate = candidate;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "Vote{" +
				"candidate=" + candidate +
				", time=" + time +
				'}';
	}
}