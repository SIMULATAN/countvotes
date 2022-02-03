package com.github.simulatan.countvotes.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record Vote(Candidate candidate, long time, int id) {

	private static int nextId = 1;

	public Vote(Candidate candidate) {
		this(candidate, System.currentTimeMillis(), nextId++);
	}

	public String getTimeFormatted() {
		return DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(time));
	}

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withLocale(Locale.getDefault())
			.withZone(ZoneId.systemDefault());
	public String getFormatted() {
		return "#" + id + " " + candidate.getName() + " (" + getTimeFormatted() + ")";
	}

	@Override
	public String toString() {
		return "Vote{" +
				"candidate=" + candidate +
				", time=" + time +
				'}';
	}
}