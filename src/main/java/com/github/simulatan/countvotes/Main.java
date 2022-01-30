package com.github.simulatan.countvotes;

import com.github.simulatan.countvotes.utils.Candidate;
import com.github.simulatan.countvotes.utils.LogPrintStream;
import com.github.simulatan.countvotes.utils.Vote;
import jcurses.system.Toolkit;
import jcurses.widgets.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.simulatan.countvotes.utils.VotesManager.*;

public class Main {

	private static Window window;
	private static List scores;

	public static void main(String[] args) {
		log("Starting...");
		System.setOut(new LogPrintStream(System.out));
		System.setErr(new LogPrintStream(System.err));

		int height = Toolkit.getScreenHeight() / 16 * 15;
		int width = Toolkit.getScreenWidth() / 16 * 15;

		window = new Window(width, height,true,"Count Votes");
		DefaultLayoutManager mgr = new DefaultLayoutManager();
		mgr.bindToContainer(window.getRootPanel());

		scores = new List() {
			@Override
			protected boolean isFocusable() {
				return false;
			}
		};
		scores.setSelectable(false);

		List recentActions = new List();
		recentActions.setSelectable(true);
		recentActions.addListener(event -> {
			PopUpMenu menu = new PopUpMenu(width - 8, height, "Delete Vote?");
			menu.add("Delete");
			menu.add("Show information");
			menu.add("Cancel");
			menu.show();
			if (menu.getSelectedItem().equals("Delete")) {
				// TODO: implement
				// removeVote(event.getItem());
				updateVoteCount();
			}
		});

		TextField searchCandidateField = new TextField();

		final Pattern newCandidatePattern = Pattern.compile("^.*\"(.+)\".*$", Pattern.MULTILINE);
		List searchResults = new List();
		searchResults.setSelectable(false);
		searchResults.add("Type something to search");
		searchResults.addListener(val -> {
			Matcher matcher;
			String candidateName;
			if (val.getItem() instanceof String s && (matcher = newCandidatePattern.matcher(s)).matches()) {
				candidateName = matcher.group(1);
				searchResults.add(candidateName);
				searchResults.remove(s);
			} else
				candidateName = (String) val.getItem();
			addVote(new Vote(Candidate.of(candidateName)));
			recentActions.add(0, candidateName);
			searchCandidateField.setText("");
			updateVoteCount();
		});

		searchCandidateField.getFocus();
		searchCandidateField.addListener(val -> {
			searchResults.clear();
			String newValue = ((TextField) val.getSource()).getText();
			votes.getCandidateStartingWith(newValue).forEach(searchResults::add);

			if (!newValue.isEmpty() && !votes.containsKey(Candidate.of(newValue)))
				searchResults.add("Add new candidate with name \"" + newValue + "\"");

			if (searchResults.getItemsCount() == 0) {
				searchResults.add("Type something to search");
				searchResults.setSelectable(false);
			} else {
				searchResults.setSelectable(true);
			}
			window.show();
		});

		mgr.addWidget(searchCandidateField, 30, 1, width - 67, height / 5, WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_CENTER);
		mgr.addWidget(searchResults, 30, 2, width - 67, height - 4, WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_CENTER);
		mgr.addWidget(scores, 0, height - 15, 30, 13, WidgetsConstants.ALIGNMENT_CENTER, WidgetsConstants.ALIGNMENT_LEFT);
		mgr.addWidget(recentActions, width - 37, 0, 35, height - 2, WidgetsConstants.ALIGNMENT_CENTER, WidgetsConstants.ALIGNMENT_RIGHT);
		window.show();
		Runtime.getRuntime().addShutdownHook(new Thread(window::close));
	}

	@SuppressWarnings("unchecked")
	private static void updateVoteCount() {
		scores.clear();
		scores.add("Total votes: " + votes.values().stream().mapToInt(java.util.List::size).sum());
		// add votes to scores list, sorted by votes
		votes
			.entrySet()
			.stream()
			.sorted(Comparator.comparingInt(e -> ((Map.Entry<Candidate, java.util.List<Vote>>) e).getValue().size()).reversed())
			.forEach(e -> scores.add(e.getKey().getName() + ": " + e.getValue().size()));
		scores.setSelectable(false);
		window.show();
		scores.setSelectable(false);
	}

	private static final Path LOGFILE;

	static {
		File LOGFILE_AS_FILE = new File("countvotes.log");
		LOGFILE = LOGFILE_AS_FILE.toPath();
		try {
			//noinspection ResultOfMethodCallIgnored
			LOGFILE_AS_FILE.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(Object o) {
		log(o.toString());
	}

	public static void log(String text) {
		try {
			Files.writeString(LOGFILE, text + "\n", StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}