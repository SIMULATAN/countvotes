package com.github.simulatan.countvotes.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withLocale(Locale.getDefault())
			.withZone(ZoneId.systemDefault());
	public String getFormatted() {
		return candidate.getName() + " (" + DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(time)) + ")";
	}

	@Override
	public String toString() {
		return "Vote{" +
				"candidate=" + candidate +
				", time=" + time +
				'}';
	}
}