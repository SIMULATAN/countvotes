package com.github.simulatan.countvotes.utils;

import java.util.HashMap;
import java.util.Optional;

public record Candidate(String name) {

	public Candidate(String name) {
		this.name = name;
		candidates.put(name, this);
	}

	public String getName() {
		return this.name;
	}

	private static final HashMap<String, Candidate> candidates = new HashMap<>();

	public static Candidate of(String name) {
		return Optional.ofNullable(candidates.get(name)).orElseGet(() -> new Candidate(name));
	}

	@Override
	public String toString() {
		return "Candidate{" +
				"name='" + name + '\'' +
				'}';
	}
}