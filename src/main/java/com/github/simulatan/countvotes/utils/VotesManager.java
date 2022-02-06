package com.github.simulatan.countvotes.utils;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.simulatan.countvotes.Main.GSON;
import static com.github.simulatan.countvotes.Main.log;

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

	private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
	static {
		scheduledExecutorService.scheduleAtFixedRate(VotesManager::saveVotes, 10, 10, TimeUnit.SECONDS);
		scheduledExecutorService.scheduleAtFixedRate(VotesManager::saveBackup, 0, 5, TimeUnit.MINUTES);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log("Autosaving...");
			saveVotes();
			saveBackup();
		}));
	}

	private static final File VOTES_FILE = new File("votes.json");
	public static synchronized void saveVotes() {
		try {
			Files.writeString(VOTES_FILE.toPath(), GSON.toJson(votes));
		} catch (Exception e) {
			log("Failed to save votes to file: " + e.getMessage());
		}
	}

	private static synchronized void saveBackup() {
		try {
			File backupFile = new File("backup/votes_" + System.currentTimeMillis() + ".json.backup");
			// noinspection ResultOfMethodCallIgnored
			backupFile.getParentFile().mkdirs();
			Files.writeString(backupFile.toPath(), GSON.toJson(votes));
		} catch (Exception e) {
			log("Failed to save votes backup to file: " + e.getMessage());
		}
	}

	/**
	 * Dummy
	 */
	private static final List<Vote> EMPTY_LIST = ImmutableList.of();
	public static List<Vote> getVotes(Candidate candidate) {
		return votes.getOrDefault(candidate, EMPTY_LIST);
	}

	public static void loadVotes() {
		try {
			votes.clear();
			recentVotes.clear();
			votes.putAll(GSON.fromJson(Files.readString(VOTES_FILE.toPath()), VotesMap.class));
		} catch (Exception e) {
			log("Failed to load votes from file: " + e.getMessage());
		}
	}
}